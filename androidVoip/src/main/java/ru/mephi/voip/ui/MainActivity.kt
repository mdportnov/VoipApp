package ru.mephi.voip.ui

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.abtollc.sdk.AbtoApplication
import org.abtollc.sdk.AbtoPhone
import org.abtollc.sdk.AbtoPhoneCfg
import org.abtollc.sdk.OnInitializeListener.InitializeState
import org.abtollc.sdk.OnRegistrationListener
import org.abtollc.utils.codec.Codec
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import ru.mephi.shared.appContext
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.BuildConfig
import ru.mephi.voip.R
import ru.mephi.voip.abto.getSipUsername
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.eventbus.Event
import timber.log.Timber


class MainActivity : ComponentActivity(),
    EasyPermissions.PermissionCallbacks {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val accountRepository: AccountStatusRepository by inject()

    companion object {
        var phone: AbtoPhone = (appContext as AbtoApplication).abtoPhone
        private const val REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124
        const val CHANNEL_ID: String = "CHANNEL"
    }

    override fun onResume() {
        super.onResume()
        phone = (application as AbtoApplication).abtoPhone
        if (!accountRepository.isBackgroundWork.value) {
            if (accountRepository.isSipEnabled.value) {
                enableSip()
            }
        }
    }

    private var scaffoldState: ScaffoldState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            scaffoldState = rememberScaffoldState()
            App(scaffoldState = scaffoldState!!)
        }

        EventBus.getDefault().register(this)

        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)

        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)

        if (!hasPermissions())
            requestPermissions()

        if (accountRepository.isSipEnabled.value)
            enableSip()
    }


    override fun onDestroy() {
        super.onDestroy()
        // При полном закрытии приложения останавливаем Call-сервис
        if (!accountRepository.isBackgroundWork.value) {
            disableSip()
            EventBus.getDefault().unregister(this)
        }
        if (!accountRepository.isSipEnabled.value) {
            disableSip()
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe
    fun enableSip(messageEvent: Event.EnableAccount? = null) {
        initAccount()
        if (hasPermissions())
            initPhone()
    }

    @Subscribe
    fun disableSip(messageEvent: Event.DisableAccount? = null) {
        phone.unregister()
        phone.stopForeground()
        phone.destroy()
    }

    private fun initAccount() {
        if (phone.isActive)
            return

        val domain = appContext.getString(R.string.sip_domain)
        val account = accountRepository.getActiveAccount()
        val username = account?.login
        val password = account?.password

        if (account != null)
            phone.config.addAccount(
                domain,
                "",
                username, password, null, "",
                300,
                true
            )
    }

    private fun initPhone() {
        phone.setNetworkEventListener { connected, _ ->
            if (connected)
                accountRepository.fetchStatus(AccountStatus.LOADING)
            else
                accountRepository.fetchStatus(AccountStatus.NO_CONNECTION)
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
                    AccountStatus.REGISTRATION_FAILED,
                    "Ошибка $statusCode: $statusText"
                )

                if (statusCode == 405) {
                    accountRepository.fetchStatus(AccountStatus.NO_CONNECTION)
                }

                // 408 Request Timeout, 502 Bad Gateway
                if (statusCode == 408 || statusCode == 502) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(2000)
                        accountRepository.fetchStatus(
                            AccountStatus.CHANGING,
                            "Переподключение, ошибка $statusCode"
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
                InitializeState.FAIL -> AlertDialog.Builder(this@MainActivity)
                    .setTitle("Error")
                    .setMessage(message)
                    .setPositiveButton("Ok") { dlg, _ -> dlg.dismiss() }.create().show()
                InitializeState.SUCCESS -> {}
                else -> {}
            }
        }

        if (phone.isActive)
            return

        val config = phone.config
        for (c in Codec.values()) config.setCodecPriority(c, 0.toShort())
//        config.setCodecPriority(Codec.G729, 78.toShort())
        config.setCodecPriority(Codec.PCMA, 80.toShort())
        config.setCodecPriority(Codec.PCMU, 79.toShort())
        config.setCodecPriority(Codec.H264, 220.toShort())
        config.setCodecPriority(Codec.H263_1998, 0.toShort())
        config.setSignallingTransport(AbtoPhoneCfg.SignalingTransportType.UDP) //TCP);//TLS);
        config.setKeepAliveInterval(AbtoPhoneCfg.SignalingTransportType.UDP, 15)
//        config.sipPort = 0
        config.isUseSRTP = false
        config.userAgent = phone.version()
        config.hangupTimeout = 5000
        config.enableSipsSchemeUse = false
        config.isSTUNEnabled = false
        AbtoPhoneCfg.setLogLevel(7, true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_NONE
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            FLAG_CANCEL_CURRENT or FLAG_IMMUTABLE
        )

        val mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        mBuilder.setAutoCancel(false)
        mBuilder.setOngoing(true)
        mBuilder.setContentIntent(pendingIntent)
        mBuilder.setContentText(getString(R.string.notification_title))
        mBuilder.setSubText(accountRepository.status.value.status)
        mBuilder.setSmallIcon(R.drawable.logo_voip)
        val notification = mBuilder.build()

        phone.initialize(false) // start service in 'sticky' mode - when app removed from recent service will be restarted automatically
        phone.initializeForeground(notification) //start service in foreground mode
    }

    fun showSnackBar(text: String) {
        lifecycleScope.launch {
            scaffoldState?.snackbarHostState?.currentSnackbarData?.dismiss()
            scaffoldState?.snackbarHostState?.showSnackbar(text)
        }
    }

    fun hasPermissions(): Boolean =
        EasyPermissions.hasPermissions(
            this,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.USE_SIP
        )

    fun requestPermissions() {
        EasyPermissions.requestPermissions(
            this,
            "Это приложение требует разрешение на совершение звонков и использование микрофона",
            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.USE_SIP
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this@MainActivity, perms)) {
            SettingsDialog.Builder(this)
                .title("Запрос разрешений для SIP-звонков")
                .rationale("Это приложение требует разрешения на совершение звонков и использование микрофона")
                .negativeButtonText("Закрыть")
                .positiveButtonText("Предоставить")
                .build().show()
        } else
            requestPermissions()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}