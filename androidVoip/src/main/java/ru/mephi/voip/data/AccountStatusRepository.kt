package ru.mephi.voip.data

import android.app.Application
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.shared.utils.appContext
import ru.mephi.shared.vm.SavedAccountsViewModel
import ru.mephi.voip.R
import ru.mephi.voip.abto.AbtoApp
import ru.mephi.voip.abto.decryptAccountJson
import ru.mephi.voip.abto.encryptAccountJson
import ru.mephi.voip.ui.MasterActivity
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

    private val saVM: SavedAccountsViewModel by inject()

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

    // OLD
    private var _activeAccount = MutableStateFlow<Account?>(null)
    val activeAccount: StateFlow<Account?> = _activeAccount

    private var accId = -1L
    val currentAccount = MutableStateFlow(AccountUtils.dummyAccount)
    val accountsList = MutableStateFlow(emptyList<Account>())
    val phoneStatus = MutableStateFlow(AccountStatus.UNREGISTERED)

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        Timber.e("AccountStatusRepository: init")
        getAccountsList().let { lst ->
            accountsList.value = lst
            saVM.sipList.value = lst.map { v -> v.login }
            lst.firstOrNull { it.isActive }.let {
                if (it != null) {
                    currentAccount.value = it
                    saVM.setCurrentAccount(it.login)
                } else {
                    Timber.e("No active accounts found!")
                    phoneStatus.value = AccountStatus.UNREGISTERED
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
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
    }

    fun initPhone(
        setActive: Boolean = true
    ) {
        if (MasterActivity.phone.isActive) {
            Timber.e("initPhone: failed, phone is already active!")
            return
        }
        Timber.e("initPhone: initialising")

        MasterActivity.phone.setNetworkEventListener { connected, networkType ->
            Timber.e("setNetworkEventListener: network state changed, connected=$connected, networkType=$networkType")
            if (connected) {
                phoneStatus.value = AccountStatus.LOADING
            } else {
                phoneStatus.value = AccountStatus.NO_CONNECTION
            }
        }

        MasterActivity.phone.setRegistrationStateListener(object : OnRegistrationListener {
            override fun onRegistered(accId: Long) {
                Timber.e("onRegistered: account registered, accId=$accId")
                phoneStatus.value = AccountStatus.REGISTERED
            }

            override fun onUnRegistered(accId: Long) {
                Timber.e("onUnRegistered: account unregistered, accId=$accId")
                phoneStatus.value = AccountStatus.UNREGISTERED
            }

            override fun onRegistrationFailed(accId: Long, statusCode: Int, statusText: String?) {
                Timber.e("onRegistrationFailed: account registration failed, accId=$accId, statusCode=$statusCode")

                when(statusCode) {
                    405 -> { phoneStatus.value = AccountStatus.NO_CONNECTION }
                    408, 502 -> {
                        if (currentAccount.value.login.isNotEmpty()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(2000)
                                phoneStatus.value = AccountStatus.CHANGING
                                delay(15000)
                                retryRegistration()
                            }
                        }
                    }
                    else -> { phoneStatus.value = AccountStatus.REGISTRATION_FAILED }
                }
            }
        })

        phone.setNotifyEventListener {
            Timber.e("setNotifyEventListener: $it")
        }

        phone.setInitializeListener { state, message ->
            Timber.e("setInitializeListener: state=$state, message=$message")
            when (state) {
                OnInitializeListener.InitializeState.START, OnInitializeListener.InitializeState.INFO, OnInitializeListener.InitializeState.WARNING -> {}
//                OnInitializeListener.InitializeState.FAIL -> AlertDialog.Builder(this@MasterActivity).setTitle("Error")
//                    .setMessage(message).setPositiveButton("Ok") { dlg, _ -> dlg.dismiss() }
//                    .create().show()
                OnInitializeListener.InitializeState.SUCCESS -> {}
                else -> {}
            }
        }

        val config = phone.config
        for (c in Codec.values()) config.setCodecPriority(c, 0.toShort())
//        config.setCodecPriority(Codec.G729, 78.toShort())
        config.setCodecPriority(Codec.PCMA, 80.toShort())
        config.setCodecPriority(Codec.PCMU, 79.toShort())
        config.setCodecPriority(Codec.H264, 220.toShort())
        config.setCodecPriority(Codec.H263_1998, 0.toShort())
        config.setSignallingTransport(AbtoPhoneCfg.SignalingTransportType.UDP) //TCP);//TLS);
        config.setKeepAliveInterval(AbtoPhoneCfg.SignalingTransportType.UDP, 15)
        config.isUseSRTP = false
        config.userAgent = phone.version()
        config.hangupTimeout = 5000
        config.registerTimeout = 3000
        config.enableSipsSchemeUse = false
        config.isSTUNEnabled = false
        AbtoPhoneCfg.setLogLevel(7, true)

        val notification =
            notificationHandler.updateNotificationStatus(status.value)

        phone.initialize(false) // start service in 'sticky' mode - when app removed from recent service will be restarted automatically
        phone.initializeForeground(notification) //start service in foreground mode

        if (setActive) {
            setActiveAccount(currentAccount.value)
        }
    }

    fun exitPhone() {
        if (accId != -1L) {
            phone.unregister()
        }
        phone.destroy()
        phoneStatus.value = AccountStatus.UNREGISTERED
    }

    fun getUserNumber() = when {
        phone.config.accountsCount == 0 -> null
        phone.config.getAccount(accId).active -> phone.config.getAccount(phone.currentAccountId)?.sipUserName
        else -> null
    }

    fun retryRegistration() {
        if (currentAccount.value.login.isEmpty()) {
            phoneStatus.value = AccountStatus.UNREGISTERED
            scope.launch {
                settings.enableSip(false)
            }
        } else {
            phoneStatus.value = AccountStatus.CHANGING
            setActiveAccount(currentAccount.value)
        }
    }

    fun setActiveAccount(account: Account) {
        Timber.e("setActiveAccount: called!")
        phoneStatus.value = AccountStatus.LOADING

        var isAdded = true
        if (accountsList.value.firstOrNull { v -> v.login == account.login } == null) {
            Timber.e("setActiveAccount: account doesn't exists in list, assuming it's first login")
            isAdded = false
        }
        if (accId != -1L) {
            Timber.e("setActiveAccount: unregistering previous account, accId=$accId")
            phone.unregister(accId)
        }

        val list = accountsList.value.toMutableList()
        list.forEach { v -> v.isActive = false }

        if (!phone.isActive) {
            Timber.e("setActiveAccount: phone is not active, enabling!")
            initPhone(false)
        }
        accId = phone.config.addAccount(
            appContext.getString(R.string.sip_domain),
            "",
            account.login,
            account.password,
            null,
            "",
            300,
            true
        )
        phone.register()
        Timber.e("setActiveAccount: added new account, accId=$accId")

        list.forEach { v -> if (v.login == account.login) v.isActive = true; currentAccount.value = v }
        Timber.e("setActiveAccount: old list = ${accountsList.value}, new list = $list")
        if (isAdded) {
            saVM.setCurrentAccount(account.login)
            setAccountsList(list)
        }
    }

    fun addAccount(account: Account) {
        currentAccount.value = account.copy(isActive = false)
        val list = accountsList.value.toMutableList()
        list.add(account)
        setAccountsList(list)
        setActiveAccount(account)
    }

    fun removeAccount(account: Account) {
        if (account.isActive) {
            if (accId != -1L) {
                phone.unregister(accId)
            }
            currentAccount.value = AccountUtils.dummyAccount
        }
        val list = accountsList.value.toMutableList()
        list.removeAll { v -> v.login == account.login}
        setAccountsList(list)
    }

    private fun setAccountsList(accounts: List<Account>) {
        saVM.sipList.value = accounts.map { it.login }
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
}

private object AccountUtils {
    val dummyAccount = Account(
        login = "Без Аккаунта",
        password = "",
        isActive = false
    )
}
