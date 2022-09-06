package ru.mephi.voip.data

import android.app.Application
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.abtollc.sdk.AbtoPhone
import org.abtollc.sdk.AbtoPhoneCfg
import org.abtollc.sdk.OnInitializeListener
import org.abtollc.sdk.OnRegistrationListener
import org.abtollc.utils.codec.Codec
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.data.model.Account
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.repo.VoIPServiceRepository
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.shared.utils.appContext
import ru.mephi.voip.R
import ru.mephi.voip.abto.AbtoApp
import ru.mephi.voip.abto.decryptAccountJson
import ru.mephi.voip.abto.encryptAccountJson
import ru.mephi.voip.ui.settings.PreferenceRepository
import ru.mephi.voip.utils.NotificationHandler
import timber.log.Timber

class PhoneManager(
    app: Application,
    var settings: PreferenceRepository,
    private val notificationHandler: NotificationHandler
) : KoinComponent {
    private var phone: AbtoPhone = (app as AbtoApp).abtoPhone

    private val serviceRepo: VoIPServiceRepository by inject()

    private var _displayName = MutableStateFlow<NameItem?>(null)
    val displayName: StateFlow<NameItem?> = _displayName

    private val _status = MutableStateFlow(AccountStatus.UNREGISTERED)
    val status: StateFlow<AccountStatus> = _status

    private var _isSipEnabled = MutableStateFlow(false)
    val isSipEnabled: StateFlow<Boolean> = _isSipEnabled

    val isBackgroundWork = settings.isBackgroundModeEnabled

    private val _accountsList: MutableStateFlow<List<Account>> = MutableStateFlow(emptyList())
    val accountList: StateFlow<List<Account>> = _accountsList

    private var _accountsCount = MutableStateFlow(0)
    val accountsCount: StateFlow<Int> = _accountsCount

    var accId = -1L
    val currentAccount = MutableStateFlow(Account())
    val accountsList = MutableStateFlow(emptyList<Account>())
    val phoneStatus = MutableStateFlow(AccountStatus.UNREGISTERED)

    private var loginAccId = -1L
    var loginStatus = MutableStateFlow(LoginStatus.LOGIN_IN_PROGRESS)
    private var newAccount = Account()

    private var ignition = false
    private val scope = CoroutineScope(Dispatchers.IO)

    private var isForegroundAllowed = false

    init {
        scope.launch {
            Timber.e("PhoneManager: init")
            setPhoneStatus(AccountStatus.UNREGISTERED)
            getAccountsList().let { lst ->
                accountsList.value = lst
                lst.firstOrNull { it.isActive }.let {
                    if (it != null) {
                        currentAccount.value = it
                    } else {
                        Timber.e("No active accounts found!")
                    }
                }
            }
            scope.launch {
                settings.isSipEnabled.collect { enabled ->
                    _isSipEnabled.value = enabled
                    when (enabled) {
                        true -> {
                            initPhone()
                        }
                        false -> {
                            exitPhone()
                        }
                    }
                }
            }
            scope.launch {
                settings.isBackgroundModeEnabled.collect { enabled ->
                    isForegroundAllowed = enabled
                    if (!enabled && phone.isActive) {
                        phone.stopForeground()
                    } else if (enabled && phone.isActive && phoneStatus.value != AccountStatus.SHUTTING_DOWN) {
                        exitPhone()
                        waitForRestart()
                    }
                }
            }
        }
    }

    private fun waitForRestart() {
        scope.launch {
            phoneStatus.collect {
                if (it != AccountStatus.SHUTTING_DOWN) {
                    initPhone(); return@collect
                }
            }
        }
    }

    private fun setPhoneStatus(
        status: AccountStatus
    ) {
        Timber.e("setPhoneStatus: switching status, ${phoneStatus.value.name} => ${status.name}")
        phoneStatus.value = status
        when (status) {
            AccountStatus.REGISTERED,
            AccountStatus.NO_CONNECTION,
            AccountStatus.REGISTRATION_FAILED,
            AccountStatus.RECONNECTING,
            AccountStatus.CONNECTING -> {
                if (isForegroundAllowed && phone.isActive) {
                    notificationHandler.getDisplayedNotification(status)
                }
            }
            else -> { }
        }
    }

    private fun setLoginStatus(
        status: LoginStatus
    ) {
        Timber.e("setLoginStatus: switching status, ${loginStatus.value.name} => ${status.name}")
        loginStatus.value = status
        when(status) {
            LoginStatus.LOGIN_FAILURE -> {
                isLoginMode = false
                phone.unregister(loginAccId)
                loginAccId = -1L
                if (!isSipEnabled.value) {
                    exitPhone()
                }
            }
            LoginStatus.DATA_FETCH_FAILURE -> {
                isLoginMode = false
            }
            LoginStatus.LOGIN_IN_PROGRESS -> {
                isLoginMode = true
            }
            LoginStatus.LOGIN_SUCCESSFUL -> {
                isLoginMode = false
                setPhoneStatus(AccountStatus.REGISTERED)
                if (phone.config.accountsCount > 1) {
                    phone.unregister(accId)
                    accId = loginAccId
                    loginAccId = -1L
                }
                if (!isSipEnabled.value) {
                    scope.launch {
                        settings.enableSip(true)
                    }
                }
                addAccount(newAccount)
                newAccount = Account()
            }
        }
    }

    fun initPhone() {
        if (phone.isActive) {
            Timber.e("initPhone: failed, phone is already active!")
            return
        }
        setPhoneStatus(AccountStatus.STARTING_UP)
        Timber.e("initPhone: initialising")

        phone.setNetworkEventListener { connected, networkType ->
            Timber.e("setNetworkEventListener: network state changed, connected=$connected, networkType=$networkType")
            if (connected) {
                setPhoneStatus(AccountStatus.CONNECTING)
            } else {
                setPhoneStatus(AccountStatus.NO_CONNECTION)
            }
        }

        phone.setRegistrationStateListener(object : OnRegistrationListener {
            override fun onRegistered(accId: Long) {
                Timber.e("onRegistered: account registered, accId=$accId, count=${phone.config.accountsCount}")
                when (accId) {
                    this@PhoneManager.accId -> {
                        setPhoneStatus(AccountStatus.REGISTERED)
                    }
                    loginAccId -> {
                        setLoginStatus(LoginStatus.LOGIN_SUCCESSFUL)
                    }
                    else -> { }
                }
            }

            override fun onUnRegistered(accId: Long) {
                Timber.e("onUnRegistered: account unregistered, accId=$accId, count=${phone.config.accountsCount}")
                when (accId) {
                    this@PhoneManager.accId -> {
                        setPhoneStatus(AccountStatus.UNREGISTERED)
                        this@PhoneManager.accId = -1L
                    }
                    loginAccId -> { }
                    else -> { }
                }
            }

            override fun onRegistrationFailed(
                accId: Long,
                statusCode: Int,
                statusText: String?
            ) {
                Timber.e("onRegistrationFailed: account registration failed, accId=$accId, statusCode=$statusCode, count=${phone.config.accountsCount}")
                when (accId) {
                    this@PhoneManager.accId -> {
                        when (statusCode) {
                            100 -> { }
                            405 -> { setPhoneStatus(AccountStatus.NO_CONNECTION) }
                            408, 502 -> {
                                if (currentAccount.value.login.isNotEmpty()) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        setPhoneStatus(AccountStatus.RECONNECTING)
                                        delay(10000)
                                        retryRegistration()
                                    }
                                } else {
                                    setPhoneStatus(AccountStatus.REGISTRATION_FAILED)
                                }
                            }
                            else -> { setPhoneStatus(AccountStatus.REGISTRATION_FAILED) }
                        }
                    }
                    loginAccId -> {
                        when(statusCode) {
                            100 -> {  }
                            else -> {
                                setLoginStatus(LoginStatus.LOGIN_FAILURE)
                            }
                        }
                    }
                    else -> { }
                }
            }
        })

        phone.setNotifyEventListener { msg ->
            Timber.e("onReceivedSipNotifyMsg: $msg")
        }

        phone.setInitializeListener { state, message ->
            Timber.e("onInitializeState: state=${state.value}, message=${message ?: "null"}")
            when (state) {
                OnInitializeListener.InitializeState.SUCCESS -> {
                    when {
                        currentAccount.value.login.isNotEmpty() && !isLoginMode && accId == -1L -> {
                            setPhoneStatus(AccountStatus.CONNECTING)
                            accId = registerAccount(accId, currentAccount.value)
                        }
                    }
                }
                else -> { }
            }
        }

        with(phone.config) {
            for (c in Codec.values()) {
                when (c) {
                    Codec.PCMA -> setCodecPriority(c, 80.toShort())
                    Codec.PCMU -> setCodecPriority(c, 79.toShort())
                    Codec.H264 -> setCodecPriority(c, 220.toShort())
                    else -> setCodecPriority(c, 0.toShort())
                }
            }
            setSignallingTransport(AbtoPhoneCfg.SignalingTransportType.UDP)
            setKeepAliveInterval(AbtoPhoneCfg.SignalingTransportType.UDP, 15)
            isUseSRTP = false
            userAgent = phone.version()
            hangupTimeout = 5000
            registerTimeout = 3000
            enableSipsSchemeUse = false
            isSTUNEnabled = false
        }
        AbtoPhoneCfg.setLogLevel(7, true)

        if (isForegroundAllowed) {
            phone.initializeForeground(notificationHandler.getDisplayedNotification(phoneStatus.value))
        } else {
            phone.initialize()
        }
    }

    fun exitPhone() {
        if (!phone.isActive) {
            Timber.e("exitPhone: failed, phone is already dead!")
            return
        }
        setPhoneStatus(AccountStatus.SHUTTING_DOWN)
        accId = -1L
        loginAccId = -1L
        phone.unregister()
        phone.destroy()
        waitDeathJob = scope.launch {
            while (phone.isActive) delay(100)
            if (!ignition) setPhoneStatus(AccountStatus.UNREGISTERED)
        }
    }

    private var waitDeathJob: Job? = null

    fun getUserNumber() = when {
        phone.config.accountsCount == 0 -> null
        phone.config.getAccount(accId).active -> phone.config.getAccount(phone.currentAccountId)?.sipUserName
        else -> null
    }

    fun retryRegistration() {
        if (phoneStatus.value == AccountStatus.RECONNECTING) {
            setPhoneStatus(AccountStatus.CONNECTING)
            phone.register()
        }
    }

    fun setActiveAccount(account: Account) {
        val list = accountsList.value.toMutableList()
        list.forEach { v -> v.isActive = false }
        list.forEach { v -> if (v.login == account.login) v.isActive = true }
        setAccountsList(list)
        account.isActive = true
        currentAccount.value = account
        if (isSipEnabled.value) {
            accId.let {
                accId = -1L
                accId = registerAccount(it, account)
            }
        }
    }

    fun addAccount(account: Account) {
        currentAccount.value = account.copy(isActive = true)
        val list = accountsList.value.toMutableList()
        list.forEach { v -> v.isActive = false }
        list.add(account)
        setAccountsList(list)
    }

    fun removeAccount(account: Account) {
        if (account.isActive) {
            if (accId != -1L) {
                phone.unregister(accId)
            }
            scope.launch {
                settings.enableSip(false)
            }
            currentAccount.value = Account()
        }
        val list = accountsList.value.toMutableList()
        list.removeAll { v -> v.login == account.login}
        setAccountsList(list)
    }

    private fun setAccountsList(accounts: List<Account>) {
        accountsList.value = accounts.toMutableList()
        encryptAccountJson(Json.encodeToJsonElement(accounts).toString())
    }

    private fun getAccountsList() : List<Account> {
        decryptAccountJson().let {
            return if (it.isNullOrEmpty()) {
                emptyList()
            } else {
                Json.decodeFromString(it)
            }
        }
    }

    private fun registerAccount(
        accId: Long = -1L,
        account: Account
    ): Long {
        if (accId != -1L) {
            Timber.e("registerAccount: unregistering previous account, accId=$accId")
            phone.unregister(accId)
        }
        val ret = phone.config.addAccount(
            appContext.getString(R.string.sip_domain),
            "",
            account.login,
            account.password,
            null,
            "",
            300,
            false,
            this@PhoneManager.accId == -1L
        )
        phone.register()
        Timber.e("registerAccount: account registered, accId=$ret")
        return ret
    }

    private var isLoginMode = false

    fun startNewAccountLogin(
        account: Account
    ) {
        isLoginMode = true
        newAccount = Account()
        setLoginStatus(LoginStatus.LOGIN_IN_PROGRESS)
        if (loginAccId != -1L) {
            phone.unregister(loginAccId)
            loginAccId = -1L
        }
        scope.launch {
            serviceRepo.getInfoByPhone(account.login).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> { setLoginStatus(LoginStatus.LOGIN_IN_PROGRESS)  }
                    is Resource.Success -> {
                        resource.data?.let { lst ->
                            newAccount = Account(
                                login = account.login,
                                password = account.password,
                                displayedName = lst[0].display_name.ifEmpty { "User ${account.login}" },
                                isActive = true
                            )
                            initPhone()
                            while (!phone.isActive) {
                                delay(100)
                            }
                            loginAccId = registerAccount(accId = loginAccId, account = newAccount)
                        } ?: run { setLoginStatus(LoginStatus.DATA_FETCH_FAILURE) }
                    }
                    is Resource.Error.EmptyError -> { setLoginStatus(LoginStatus.DATA_FETCH_FAILURE) }
                    is Resource.Error.NotFoundError -> { setLoginStatus(LoginStatus.DATA_FETCH_FAILURE) }
                    is Resource.Error.NetworkError -> { setLoginStatus(LoginStatus.DATA_FETCH_FAILURE) }
                    is Resource.Error.ServerNotRespondError -> { setLoginStatus(LoginStatus.DATA_FETCH_FAILURE) }
                    is Resource.Error.UndefinedError -> { setLoginStatus(LoginStatus.DATA_FETCH_FAILURE) }
                }
            }.launchIn(this)
        }
    }
}

enum class LoginStatus {
    LOGIN_IN_PROGRESS, DATA_FETCH_FAILURE, LOGIN_FAILURE, LOGIN_SUCCESSFUL
}
