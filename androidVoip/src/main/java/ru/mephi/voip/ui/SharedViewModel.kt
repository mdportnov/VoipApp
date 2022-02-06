package ru.mephi.voip.ui

import android.app.Application
import android.content.ComponentName
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.KoinComponent
import ru.mephi.shared.appContext
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.repository.CatalogRepository
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.call.SipBackgroundService
import ru.mephi.voip.ui.call.AbtoViewModel
import ru.mephi.voip.call.getAccountsList
import ru.mephi.voip.ui.eventbus.Event
import ru.mephi.voip.ui.utils.getCurrentUserNumber
import timber.log.Timber
import java.lang.ref.WeakReference

class SharedViewModel(
    app: Application,
    override var sp: SharedPreferences,
    private val repository: CatalogRepository,
) : AbtoViewModel(app, sp), KoinComponent {
    private var _displayName = MutableStateFlow<NameItem?>(null)
    val displayName: StateFlow<NameItem?> = _displayName

    private var _isSipEnabled =
        MutableStateFlow(sp.getBoolean(appContext.getString(R.string.sp_sip_enabled), false))
    val isSipEnabled: MutableStateFlow<Boolean> = _isSipEnabled

    private var _accountsCount = MutableStateFlow(0)
    val accountsCount: StateFlow<Int> = _accountsCount

    private val _mBinder = MutableLiveData<SipBackgroundService.SipBinder?>()
    val mBinder: LiveData<SipBackgroundService.SipBinder?> = _mBinder

    var sipService: WeakReference<SipBackgroundService>? = null
    var status: StateFlow<AccountStatus>? = null

    fun fetchStatus(accountStatus: AccountStatus? = null) {
        sipService?.get()?.fetchStatus(accountStatus)
    }

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder?) {
            Timber.d("onServiceConnected: connected to service")
            _mBinder.postValue(binder as SipBackgroundService.SipBinder)
            sipService = WeakReference(binder.getService())
            status = binder.getService().status
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Timber.d("onServiceDisconnected: disconnected from service")
            _mBinder.postValue(null)
            sipService = null
            status = null
        }
    }

    init {
        Timber.d("SharedViewModel Init")
        viewModelScope.launch {
            sipService?.get()?.status?.collect { newStatus ->
                if (newStatus == AccountStatus.REGISTERED)
                    fetchName()

                val list = getAccountsList()
                _accountsCount.value = list.size

                if (newStatus == AccountStatus.REGISTERED)
                    fetchName()
                else if (newStatus == AccountStatus.UNREGISTERED)
                    _displayName.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("SharedViewModel Cleared")
    }

    fun toggleSipStatus() {
        sp.edit().putBoolean("is_sip_enabled", !sp.getBoolean("is_sip_enabled", false)).apply()
        _isSipEnabled.value = !_isSipEnabled.value

        if (_isSipEnabled.value)
            enableAccount()
        else disableAccount()
    }

    fun fetchAccountsCount() {
        _accountsCount.value = getAccountsList().size
    }

    fun enableAccount() {
        EventBus.getDefault().post(Event.EnableAccount())
        phone.restartSip()
        fetchStatus()
    }

    fun disableAccount() {
        EventBus.getDefault().post(Event.DisableAccount())
        phone.stopForeground()
        phone.unregister()
        phone.destroy()
        fetchStatus(AccountStatus.UNREGISTERED)
    }

    private var fetchNameJob: Job? = null

    private fun fetchName() {
        val number = phone.getCurrentUserNumber()
        if (!number.isNullOrEmpty()) {
            fetchNameJob?.cancel()
            fetchNameJob = viewModelScope.launch(Dispatchers.IO) {
                repository.getInfoByPhone(number).onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let {
                                _displayName.value = it[0]
                            }
                        }
                        is Resource.Error -> {
                            _displayName.value = null
                        }
                        is Resource.Loading -> {
                        }
                    }
                }.launchIn(this)
            }
        }
    }
}