package ru.mephi.voip.data

import android.app.Application
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
import ru.mephi.voip.ui.settings.PreferenceRepository
import ru.mephi.voip.utils.NotificationHandler
import timber.log.Timber

class AccountStatusRepository(
    app: Application,
    var settings: PreferenceRepository,
    private val repository: CatalogRepository,
    private val notificationHandler: NotificationHandler
) : KoinComponent {
    private var phone: AbtoPhone = (app as AbtoApp).abtoPhone

    private var _displayName = MutableStateFlow<NameItem?>(null)
    val displayName: StateFlow<NameItem?> = _displayName

    private val _status = MutableStateFlow(AccountStatus.UNREGISTERED)
    val status: StateFlow<AccountStatus> = _status

    private var _isSipEnabled = MutableStateFlow(false)
    val isSipEnabled: StateFlow<Boolean> = _isSipEnabled

    val isBackgroundWork = settings.isBackgroundModeEnabled

    private val _accountsList: MutableStateFlow<List<Account>> = MutableStateFlow(listOf())
    val accountList: StateFlow<List<Account>> = _accountsList

    private var _accountsCount = MutableStateFlow(0)
    val accountsCount: StateFlow<Int> = _accountsCount

    private var _activeAccount = MutableStateFlow<Account?>(null)
    val activeAccount: StateFlow<Account?> = _activeAccount

    val hasActiveAccount
        get() = activeAccount.value != null

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        updateAccountsList()
        _activeAccount.value = accountList.value.firstOrNull { it.isActive }
        scope.launch {
            settings.isSipEnabled.collect { enabled ->
                _isSipEnabled.value = enabled
                fetchStatus(_status.value)

                when (enabled) {
                    true -> {
                        enableAccount()
                    }
                    false -> {
                        disableAccount()
                    }
                }
            }
        }
    }

    private fun updateAccountsList() {
        val jsonDecrypted = decryptAccountJson()
        Timber.d("AccountsListJSON: \n${jsonDecrypted ?: "empty"}")
        _accountsList.value = if (jsonDecrypted.isNullOrEmpty()) mutableListOf()
        else Json.decodeFromString(jsonDecrypted)
        _accountsCount.value = _accountsList.value.size
    }

    fun fetchStatus(newStatus: AccountStatus? = null, statusCode: String = "") {
        CoroutineScope(Dispatchers.Main).launch {
            _status.replayCache.lastOrNull()?.let { lastStatus ->
                Timber.d("Switching Status from: \"${lastStatus.status}\" to \"${newStatus?.status}\"")
            }

            _status.emit(AccountStatus.LOADING)

            updateAccountsList()

            if (accountsCount.value == 0) {
                _status.emit(AccountStatus.UNREGISTERED)
                settings.enableSip(false)
                notificationHandler.updateNotificationStatus(
                    _status.value, statusCode, activeAccount.value?.login ?: ""
                )
                return@launch
            }

            if (newStatus == null && phone.getSipProfileState(phone.currentAccountId)?.statusCode == 200) {
                // ???? ????????????, ???????? ???????????????????? ???????? ??????????????, ?? AbtoApp ?????????????? ??
                // statusCode ?????????????? = 200 (??????????????????????????????). ???????????????????? ?????? ?????????????????? ??????????????????
                _status.value = AccountStatus.REGISTERED
                fetchName()
            } else if (newStatus != null) _status.value = newStatus

            if (newStatus == AccountStatus.REGISTERED) fetchName()
            else if (newStatus == AccountStatus.UNREGISTERED || newStatus == AccountStatus.REGISTRATION_FAILED) _displayName.emit(
                null
            )

            notificationHandler.updateNotificationStatus(
                _status.value, statusCode, activeAccount.value?.login ?: ""
            )
        }
    }

    private var fetchNameJob: Job? = null

    fun getUserNumber() = when {
        phone.config.accountsCount == 0 -> null
        phone.config.getAccount(phone.currentAccountId).active -> phone.config.getAccount(phone.currentAccountId)?.sipUserName
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

    private fun updateAccounts(list: List<Account>) {
        _accountsList.value = list
        _accountsCount.value = list.size
        _activeAccount.value = accountList.value.firstOrNull { it.isActive }
        val json = Json.encodeToJsonElement(list).toString()
        encryptAccountJson(jsonForEncrypt = json)
    }

    fun addNewAccount(newLogin: String, newPassword: String) {
        val list = accountList.value.toMutableList()
        list.forEach { it.isActive = false }

        list.add(Account(newLogin, newPassword, true))

        updateAccounts(list)
        retryRegistration()
    }

    fun removeAccount(account: Account) {
        val list = accountList.value.toMutableList()
        list.removeAll { it.login == account.login }

        if (activeAccount.value == account) {// ???????? ???????????????? ?????????????? ??????????????????
            phone.unregister()
            _activeAccount.value = null
            retryRegistration()
        }

        updateAccounts(list)
    }

    fun updateActiveAccount(account: Account): String {
        _activeAccount.value = accountList.value.firstOrNull { it.isActive }
        val list = accountList.value
        fetchStatus(AccountStatus.CHANGING)

        list.forEach { it.isActive = false }
        list.forEach {
            if (account.login == it.login) it.isActive = true
        }

        updateAccounts(list)

        val username = activeAccount.value?.login
        val password = activeAccount.value?.password

        phone.config.addAccount(
            appContext.getString(R.string.sip_domain), "", username, password, null, "", 300, true
        )

        phone.config.registerTimeout = 3000
        phone.restartSip()

        _accountsList.value = list
        _accountsCount.value = list.size

        return activeAccount.value!!.login
    }

    fun retryRegistration() {
        if (!hasActiveAccount) {
            fetchStatus(AccountStatus.UNREGISTERED)
            scope.launch {
                settings.enableSip(false)
            }
        } else {
            fetchStatus(AccountStatus.CHANGING)
            activeAccount.value?.let {
                updateActiveAccount(it)
            }
        }
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