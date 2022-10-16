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
import ru.mephi.shared.data.model.Account
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.repo.VoIPServiceRepository
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.shared.utils.appContext
import ru.mephi.voip.R
import ru.mephi.voip.abto.AbtoApp
import ru.mephi.voip.abto.decryptAccountJson
import ru.mephi.voip.abto.encryptAccountJson
import ru.mephi.voip.utils.NotificationHandler
import timber.log.Timber

class PhoneManager(
    app: Application,
    private var settingsRepo: SettingsRepository,
    private val notificationHandler: NotificationHandler,
    private val serviceRepo: VoIPServiceRepository
) : KoinComponent {
    private var phone: AbtoPhone = (app as AbtoApp).abtoPhone

    val accountManagementStatus = MutableStateFlow(AccountManagementStatus.LOADING)

    private var accId = PhoneUtils.NULL_ACC_ID
    val currentAccount = MutableStateFlow(Account())
    val accountsList = MutableStateFlow(emptyList<Account>())
    val phoneStatus = MutableStateFlow(AccountStatus.UNREGISTERED)

    private var loginAccId = PhoneUtils.NULL_ACC_ID
    var loginStatus = MutableStateFlow(LoginStatus.LOGIN_IN_PROGRESS)
    private var newAccount = Account()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var waitDeathJob: Job? = null
    private var loginFetchJob: Job? = null

    private var isForegroundAllowed = false
    private var isSipEnabled = false

    init {
        scope.launch {
            Timber.e("PhoneManager: init")
            getAccountsList().let { lst ->
                accountsList.value = lst
                if (lst.isEmpty()) {
                    accountManagementStatus.value = AccountManagementStatus.NO_SAVED_ACC
                } else {
                    lst.firstOrNull { it.isActive }.let {
                        if (it != null) {
                            currentAccount.value = it
                            accountManagementStatus.value = AccountManagementStatus.ACC_SELECTED
                        } else {
                            accountManagementStatus.value = AccountManagementStatus.ACC_NOT_SELECTED
                        }
                    }
                }
            }
            scope.launch {
                settingsRepo.isSipEnabled.collect { enabled ->
                    if (isSipEnabled != enabled) {
                        isSipEnabled = enabled
                        when (enabled) {
                            true -> { initPhone()
                            }
                            false -> { exitPhone(false) }
                        }
                    }
                }
            }
            scope.launch {
                settingsRepo.isBackgroundModeEnabled.collect { enabled ->
                    if (isForegroundAllowed != enabled) {
                        isForegroundAllowed = enabled
                        if (phone.isActive) {
                            if (!enabled) {
                                phone.stopForeground()
                            } else if (enabled && phoneStatus.value != AccountStatus.SHUTTING_DOWN) {
                                exitPhone(true)
                            }
                        }
                    }
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
                loginAccId = PhoneUtils.NULL_ACC_ID
                if (!isSipEnabled) {
                    exitPhone(false)
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
                    loginAccId = PhoneUtils.NULL_ACC_ID
                }
                if (!isSipEnabled) {
                    scope.launch {
                        settingsRepo.enableSip(true)
                    }
                }
                addAccount(newAccount)
                newAccount = Account()
            }
        }
    }

    private fun initPhone() {
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
                        this@PhoneManager.accId = PhoneUtils.NULL_ACC_ID
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
                    if (currentAccount.value.login.isNotEmpty() && !isLoginMode && accId == PhoneUtils.NULL_ACC_ID) {
                        setPhoneStatus(AccountStatus.CONNECTING)
                        accId = registerAccount(accId, currentAccount.value)
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

    private fun exitPhone(
        isRestart: Boolean
    ) {
        waitDeathJob?.cancel()
        if (!phone.isActive) {
            Timber.e("exitPhone: failed, phone is already dead!")
            return
        }
        when (isRestart) {
            true -> { setPhoneStatus(AccountStatus.RESTARTING) }
            false -> { setPhoneStatus(AccountStatus.SHUTTING_DOWN) }
        }
        accId = PhoneUtils.NULL_ACC_ID
        loginAccId = PhoneUtils.NULL_ACC_ID
        phone.unregister()
        try {
            phone.destroy()
        } catch(e: NullPointerException) {
            Timber.e("exitPhone: fatal, phone was already dead!")
        }
        waitDeathJob = scope.launch {
            while (phone.isActive) delay(100)
            when (isRestart) {
                true -> { initPhone() }
                false -> { setPhoneStatus(AccountStatus.UNREGISTERED) }
            }
        }
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
        accountManagementStatus.value = AccountManagementStatus.ACC_SELECTED
        if (isSipEnabled) {
            accId.let {
                accId = PhoneUtils.NULL_ACC_ID
                accId = registerAccount(it, account)
            }
        }
    }

    private fun addAccount(account: Account) {
        currentAccount.value = account.copy(isActive = true)
        val list = accountsList.value.toMutableList()
        list.forEach { v -> v.isActive = false }
        list.add(account)
        setAccountsList(list)
    }

    fun removeAccount(account: Account) {
        if (account.isActive) {
            if (accId != PhoneUtils.NULL_ACC_ID) {
                phone.unregister(accId)
            }
            scope.launch {
                settingsRepo.enableSip(false)
            }
            currentAccount.value = Account()
        }
        val list = accountsList.value.toMutableList()
        list.removeAll { v -> v.login == account.login}
        accountManagementStatus.value = when {
            list.isEmpty() -> AccountManagementStatus.NO_SAVED_ACC
            account.isActive -> AccountManagementStatus.ACC_NOT_SELECTED
            else -> AccountManagementStatus.ACC_SELECTED
        }
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
        accId: Long = PhoneUtils.NULL_ACC_ID,
        account: Account
    ): Long {
        if (accId != PhoneUtils.NULL_ACC_ID) {
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
            this@PhoneManager.accId == PhoneUtils.NULL_ACC_ID
        )
        phone.register()
        Timber.e("registerAccount: account registered, accId=$ret")
        return ret
    }

    fun getCurrentAccId() = accId

    private var isLoginMode = false

    fun startNewAccountLogin(
        account: Account
    ) {
        loginFetchJob?.cancel()
        isLoginMode = true
        newAccount = Account()
        setLoginStatus(LoginStatus.LOGIN_IN_PROGRESS)
        if (loginAccId != PhoneUtils.NULL_ACC_ID) {
            phone.unregister(loginAccId)
            loginAccId = PhoneUtils.NULL_ACC_ID
        }
        loginFetchJob = scope.launch {
            serviceRepo.getInfoByPhone(account.login).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> { setLoginStatus(LoginStatus.LOGIN_IN_PROGRESS)  }
                    is Resource.Success -> {
                        resource.data?.let { lst ->
                            newAccount = Account(
                                login = account.login,
                                password = account.password,
                                displayedName = lst[0].display_name.ifEmpty { "Пользователь ${account.login}" },
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
                    is Resource.Error.NotFoundError -> {
                        newAccount = Account(
                            login = account.login,
                            password = account.password,
                            displayedName = "Пользователь ${account.login}",
                            isActive = true
                        )
                        initPhone()
                        while (!phone.isActive) {
                            delay(100)
                        }
                        loginAccId = registerAccount(accId = loginAccId, account = newAccount)
                    }
                    is Resource.Error.NetworkError -> { setLoginStatus(LoginStatus.DATA_FETCH_FAILURE) }
                    is Resource.Error.ServerNotRespondError -> { setLoginStatus(LoginStatus.DATA_FETCH_FAILURE) }
                    is Resource.Error.UndefinedError -> { setLoginStatus(LoginStatus.DATA_FETCH_FAILURE) }
                }
            }.launchIn(this)
        }
    }
}

private object PhoneUtils {
    const val NULL_ACC_ID = -1L
}

enum class LoginStatus {
    LOGIN_IN_PROGRESS, DATA_FETCH_FAILURE, LOGIN_FAILURE, LOGIN_SUCCESSFUL
}

enum class AccountManagementStatus {
    ACC_SELECTED, ACC_NOT_SELECTED, NO_SAVED_ACC, LOADING
}
