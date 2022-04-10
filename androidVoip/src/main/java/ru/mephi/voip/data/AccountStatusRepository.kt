package ru.mephi.voip.data

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.abtollc.sdk.AbtoPhone
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.KoinComponent
import ru.mephi.shared.appContext
import ru.mephi.shared.data.model.Account
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.abto.AbtoApp
import ru.mephi.voip.abto.decryptAccountJson
import ru.mephi.voip.abto.encryptAccountJson
import ru.mephi.voip.eventbus.Event
import ru.mephi.voip.ui.MainActivity
import timber.log.Timber

class AccountStatusRepository(
    app: Application, var sp: SettingsStore, private val repository: CatalogRepository
) : KoinComponent {
    private var phone: AbtoPhone = (app as AbtoApp).abtoPhone

    private val _accountsList: MutableStateFlow<List<Account>> = MutableStateFlow(listOf())
    val accountList: StateFlow<List<Account>> = _accountsList

    private var _displayName = MutableStateFlow<NameItem?>(null)
    val displayName: StateFlow<NameItem?> = _displayName

    private val _status = MutableStateFlow(AccountStatus.UNREGISTERED)
    val status: StateFlow<AccountStatus> = _status

    private var _accountsCount = MutableStateFlow(0)
    val accountsCount: StateFlow<Int> = _accountsCount

    private var _isSipEnabled = MutableStateFlow(sp.isSipEnabled())
    val isSipEnabled: StateFlow<Boolean> = _isSipEnabled

    private var _isBackGroundWork = MutableStateFlow(sp.isBackgroundWorkEnabled())
    val isBackgroundWork: StateFlow<Boolean> = _isBackGroundWork

    fun changeBackGroundWork(isWorking: Boolean) {
        _isBackGroundWork.value = isWorking
    }

    init {
        fetchStatus()
    }

    private fun updateAccountsList() {
        val jsonDecrypted = decryptAccountJson()
        Timber.d("AccountsListJSON: \n${jsonDecrypted ?: "empty"}")
        _accountsList.value = if (jsonDecrypted.isNullOrEmpty())
            mutableListOf()
        else Json.decodeFromString(jsonDecrypted)
        _accountsCount.value = _accountsList.value.size
    }

    fun getActiveAccount(): Account? = accountList.value.firstOrNull { it.isActive }

    private fun getAccountsJson(list: List<Account>?) = Json.encodeToJsonElement(list).toString()

    private fun saveAccounts(list: List<Account>) = encryptAccountJson(getAccountsJson(list))

    fun fetchStatus(newStatus: AccountStatus? = null, statusCode: String = "") {
        CoroutineScope(Dispatchers.Main).launch {
            _status.replayCache.lastOrNull()?.let { lastStatus ->
                Timber.d("Switching Status from: \"${lastStatus.status}\" to \"${newStatus?.status}\"")
            }

            _status.emit(AccountStatus.LOADING)

            updateAccountsList()

            if (accountsCount.value == 0) {
                _status.emit(AccountStatus.UNREGISTERED)
                _isSipEnabled.value = false
                return@launch
            }

            if (newStatus == null && phone.getSipProfileState(phone.currentAccountId)?.statusCode == 200) {
                // На случай, если активность была удалена, а AbtoApp активен и
                // statusCode аккаунт = 200 (зарегистрирован). Вызывается при отрисовке фрагмента
                _status.value = AccountStatus.REGISTERED
                fetchName()
            } else if (newStatus != null)
                _status.value = newStatus

            if (isSipEnabled.value)
                updateNotificationStatus(_status.value, statusCode)
            else
                _status.value = AccountStatus.UNREGISTERED

            if (newStatus == AccountStatus.REGISTERED)
                fetchName()
            else if (newStatus == AccountStatus.UNREGISTERED || newStatus == AccountStatus.REGISTRATION_FAILED)
                _displayName.emit(null)
        }
    }

    private val notificationManager =
        appContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    private val mNotificationId = 1

    private fun updateNotificationStatus(accountStatus: AccountStatus, statusCode: String) {
        val intent = Intent(appContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
            appContext, 0, intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mBuilder = NotificationCompat.Builder(appContext, MainActivity.CHANNEL_ID)
        mBuilder.setAutoCancel(false)
        mBuilder.setOngoing(true)
        mBuilder.setContentIntent(pendingIntent)
        mBuilder.setContentText(statusCode.ifEmpty { appContext.getString(R.string.notification_title) })
        mBuilder.setSubText(accountStatus.status)
        mBuilder.setSmallIcon(R.drawable.logo_voip)
        val notification = mBuilder.build()

        notificationManager.notify(mNotificationId, notification)
    }

    private var fetchNameJob: Job? = null

    fun getUserNumber() = when {
        phone.config.accountsCount == 0 -> null
        phone.config.getAccount(phone.currentAccountId).active ->
            phone.config.getAccount(phone.currentAccountId)?.sipUserName
        else -> null
    }

    private fun fetchName() {
        val number = getUserNumber()
        if (!number.isNullOrEmpty()) {
            fetchNameJob?.cancel()
            fetchNameJob = CoroutineScope(Dispatchers.IO).launch {
                repository.getInfoByPhone(number).onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let {
                                _displayName.emit(it[0])
                            }
                        }
                        is Resource.Error -> {
                            _displayName.emit(null)
                        }
                        is Resource.Loading -> {
                        }
                    }
                }
            }
        }
    }

    fun removeAccount(account: Account) {
        val list = accountList.value.toMutableList()
        list.removeAll { it.login == account.login }

        if (getActiveAccount() == account) // если активный аккаунт удаляется
            phone.unregister()

        _accountsCount.value = list.size

        saveAccounts(list)
    }

    fun addNewAccount(newLogin: String, newPassword: String) {
        val list = accountList.value.toMutableList()

        list.forEach { it.isActive = false }

        list.add(Account(newLogin, newPassword, true))

        _accountsCount.value = list.size

        saveAccounts(list)
        retryRegistration()
    }

    fun retryRegistration() {
        fetchStatus(AccountStatus.CHANGING)
        getActiveAccount()?.let {
            updateActiveAccount(it)
        }
    }

    fun updateActiveAccount(account: Account): String {
        val list = accountList.value
        fetchStatus(AccountStatus.CHANGING)

        list.forEach { it.isActive = false }
        list.forEach {
            if (account.login == it.login)
                it.isActive = true
        }

        saveAccounts(list)

        val acc = getActiveAccount()
        val username = acc?.login
        val password = acc?.password

        phone.config.addAccount(
            appContext.getString(R.string.sip_domain),
            "",
            username, password, null, "",
            300,
            true
        )

        phone.config.registerTimeout = 3000
        phone.restartSip()

        _accountsList.value = list
        _accountsCount.value = list.size

        return acc!!.login
    }

    fun toggleSipStatus() {
        sp.toggleSipStatus()
        _isSipEnabled.value = !_isSipEnabled.value

        if (_isSipEnabled.value)
            enableAccount()
        else disableAccount()
    }

    private fun enableAccount() {
        phone.restartSip()
        EventBus.getDefault().post(Event.EnableAccount())
        fetchStatus()
    }

    private fun disableAccount() {
        EventBus.getDefault().post(Event.DisableAccount())
        fetchStatus(AccountStatus.UNREGISTERED)
    }
}