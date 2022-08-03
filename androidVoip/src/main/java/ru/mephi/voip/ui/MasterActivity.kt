@file:OptIn(ExperimentalAnimationApi::class)

package ru.mephi.voip.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.abtollc.sdk.AbtoApplication
import org.abtollc.sdk.AbtoPhone
import org.abtollc.sdk.AbtoPhoneCfg
import org.abtollc.sdk.OnInitializeListener.InitializeState
import org.abtollc.sdk.OnRegistrationListener
import org.abtollc.utils.codec.Codec
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import ru.mephi.shared.appContext
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.shared.vm.LogMessage
import ru.mephi.shared.vm.LogType
import ru.mephi.shared.vm.LoggerViewModel
import ru.mephi.shared.vm.UserNotifierViewModel
import ru.mephi.voip.BuildConfig
import ru.mephi.voip.R
import ru.mephi.voip.abto.getSipUsername
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.eventbus.Event
import ru.mephi.voip.ui.detailed.DetailedInfoScreen
import ru.mephi.voip.ui.home.HomeScreen
import ru.mephi.voip.ui.settings.SettingsScreen
import ru.mephi.voip.ui.theme.MasterTheme
import ru.mephi.voip.utils.NotificationHandler
import timber.log.Timber


class MasterActivity : AppCompatActivity(), KoinComponent {

    private val requiredPermission = listOf(Manifest.permission.USE_SIP, Manifest.permission.RECORD_AUDIO)

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val accountRepository: AccountStatusRepository by inject()
    private val notificationHandler: NotificationHandler by inject()

    private var isBackgroundWork: Boolean = false
    private var isSipEnabled = false

    companion object {
        var phone: AbtoPhone = (appContext as AbtoApplication).abtoPhone
    }

    override fun onResume() {
        super.onResume()
        phone = (application as AbtoApplication).abtoPhone
        if (!isBackgroundWork) {
            if (isSipEnabled) {
                enableSip()
            }
        }
    }

    private var scaffoldState: ScaffoldState? = null

    init {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val lVM: LoggerViewModel = get()
                lVM.log.collectLatest {
                    if (it.logMessage.isEmpty()) return@collectLatest
                    when(it.logType) {
                        LogType.VERBOSE -> Timber.v(it.logMessage)
                        LogType.DEBUG -> Timber.d(it.logMessage)
                        LogType.INFO -> Timber.i(it.logMessage)
                        LogType.WARNING -> Timber.w(it.logMessage)
                        LogType.ERROR -> Timber.e(it.logMessage)
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val unVM: UserNotifierViewModel = get()
                unVM.notifyMsg.collectLatest {
                    if (it.isNotEmpty()) {
                        Toast.makeText(this@MasterActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            scaffoldState = rememberScaffoldState()
            MasterTheme {
                MasterNavCtl()
            }
        }

//        if (AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(this)) {
//            AutoStartPermissionHelper.getInstance().getAutoStartPermission(this, newTask = true)
//        }

        lifecycleScope.launch {
            accountRepository.isSipEnabled.collect {
                isSipEnabled = it
            }
        }

        lifecycleScope.launch {
            accountRepository.isBackgroundWork.collect {
                isBackgroundWork = it
            }
        }

        EventBus.getDefault().register(this)

        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)

        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)

        checkNonGrantedPermissions()

        if (isSipEnabled) enableSip()
    }


    override fun onDestroy() {
        super.onDestroy()
        // При полном закрытии приложения останавливаем Call-сервис
        if (!isBackgroundWork) {
            disableSip()
            EventBus.getDefault().unregister(this)
        }
        if (!isSipEnabled) {
            disableSip()
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe
    fun enableSip(messageEvent: Event.EnableAccount? = null) {
        initAccount()
        if (checkNonGrantedPermissions()) initPhone()
    }

    @Subscribe
    fun disableSip(messageEvent: Event.DisableAccount? = null) {
        phone.unregister()
//        phone.stopForeground()
        phone.destroy()
    }

    val activeAccount = accountRepository.activeAccount

    private fun initAccount() {
        if (phone.isActive) return

        val domain = appContext.getString(R.string.sip_domain)
//        val account =
        val username = activeAccount.value?.login
        val password = activeAccount.value?.password

        if (activeAccount.value != null) phone.config.addAccount(
            domain, "", username, password, null, "", 300, true
        )
    }

    private fun initPhone() {
        phone.setNetworkEventListener { connected, _ ->
            if (connected) accountRepository.fetchStatus(AccountStatus.LOADING)
            else accountRepository.fetchStatus(AccountStatus.NO_CONNECTION)
        }

        //Switching| Registration Listener|REG
        Timber.d("Set Registration Listener")
        phone.setRegistrationStateListener(object : OnRegistrationListener {
            override fun onRegistered(accId: Long) {
                showSnackBar(AccountStatus.REGISTERED.status)
                accountRepository.fetchStatus(AccountStatus.REGISTERED)
                Timber.d("REG STATUS: ${phone.getSipProfileState(phone.currentAccountId).statusCode}")
            }

            override fun onUnRegistered(accId: Long) {
                showSnackBar("Регистрация аккаунта отменена")
                accountRepository.fetchStatus(AccountStatus.UNREGISTERED)
            }

            override fun onRegistrationFailed(accId: Long, statusCode: Int, statusText: String?) {
                showSnackBar(
                    "Аккаунт \"${phone.getSipUsername(accId)}\" не зарегистрирован.\nПричина: $statusText"
                )

                accountRepository.fetchStatus(
                    AccountStatus.REGISTRATION_FAILED, "Ошибка $statusCode: $statusText"
                )

                if (statusCode == 405) {
                    accountRepository.fetchStatus(AccountStatus.NO_CONNECTION)
                }

                if (accountRepository.hasActiveAccount) // 408 Request Timeout, 502 Bad Gateway
                    if (statusCode == 408 || statusCode == 502) {
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(2000)
                            accountRepository.fetchStatus(
                                AccountStatus.CHANGING, "Переподключение, ошибка $statusCode"
                            )
                            delay(15000)
                            accountRepository.retryRegistration()
                        }
                    }
            }
        })

        phone.setInitializeListener { state, message ->
            when (state) {
                InitializeState.START, InitializeState.INFO, InitializeState.WARNING -> {}
                InitializeState.FAIL -> AlertDialog.Builder(this@MasterActivity).setTitle("Error")
                    .setMessage(message).setPositiveButton("Ok") { dlg, _ -> dlg.dismiss() }
                    .create().show()
                InitializeState.SUCCESS -> {}
                else -> {}
            }
        }

        if (phone.isActive) return

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
        config.enableSipsSchemeUse = false
        config.isSTUNEnabled = false
        AbtoPhoneCfg.setLogLevel(7, true)

        val notification =
            notificationHandler.updateNotificationStatus(accountRepository.status.value)

        phone.initialize(false) // start service in 'sticky' mode - when app removed from recent service will be restarted automatically
        phone.initializeForeground(notification) //start service in foreground mode
    }

    fun showSnackBar(text: String) {
        lifecycleScope.launch {
            scaffoldState?.snackbarHostState?.currentSnackbarData?.dismiss()
            scaffoldState?.snackbarHostState?.showSnackbar(text)
        }
    }

    fun checkNonGrantedPermissions(
        permissions: List<String> = requiredPermission
    ): Boolean {
        permissions.filter { p -> !isPermissionGranted(p) }.let {
            if (it.isEmpty()) return true else {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(150)
                    showPermissionsRequestDialog(it)
                }
                return false
            }
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return this.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        checkNonGrantedPermissions(permissions.filter { p ->
            grantResults[permissions.indexOf(p)] != PackageManager.PERMISSION_GRANTED
        })
    }

    private fun showPermissionsRequestDialog(
        permissions: List<String>
    ) {
        MaterialAlertDialogBuilder(this).also {
            it.setTitle("Необходимые разрешения")
            if (permissions.contains(Manifest.permission.USE_SIP) && permissions.contains(Manifest.permission.RECORD_AUDIO)) {
                it.setMessage("Для полноценной работы приложения необходимо предоставить разрешения на совершения звонков и использования микрофона")
            } else if (permissions.contains(Manifest.permission.USE_SIP)) {
                it.setMessage("Для полноценной работы приложения необходимо предоставить разрешения на совершения звонков")
            } else if (permissions.contains(Manifest.permission.RECORD_AUDIO)) {
                it.setMessage("Для полноценной работы приложения необходимо предоставить разрешения на использования микрофона")
            } else {
                it.setMessage("Для полноценной работы приложения необходимо предоставить разрешения")
                Timber.e("dialog for ${permissions.joinToString(", ")} not implemented yet")
            }
            it.setNeutralButton("Отмена") { _, _ -> onRequestDialogCancellation() }
            it.setOnCancelListener { onRequestDialogCancellation() }
            it.setPositiveButton("Предоставить") { _, _ ->  this.requestPermissions(permissions.toTypedArray(), 0x1)}
        }.show()
    }

    private fun onRequestDialogCancellation() {
        Toast.makeText(this, "¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show()
    }

    @Composable
    private fun MasterNavCtl() {
        val lVM: LoggerViewModel = get()
        lVM.log.collectAsState(initial = LogMessage(LogType.VERBOSE, ""))
        val navController = rememberAnimatedNavController()
        AnimatedNavHost(navController = navController, startDestination = MasterScreens.HomeScreen.route) {
            composable(
                route = MasterScreens.HomeScreen.route
            ) {
                HomeScreen(masterNavController = navController)
            }
            composable(
                route = MasterScreens.DetailedInfoScreen.route
            ) {
                DetailedInfoScreen {
                    navController.popBackStack()
                }
            }
            composable(
                route = MasterScreens.AccountManagerScreen.route
            ) {

            }
            composable(
                route = MasterScreens.SettingsScreen.route
            ) {
                SettingsScreen(navController)
            }
        }
    }

}
