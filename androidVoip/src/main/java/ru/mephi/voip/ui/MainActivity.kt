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
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import org.abtollc.sdk.*
import org.abtollc.sdk.OnInitializeListener.InitializeState
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
import ru.mephi.voip.databinding.ActivityMainBinding
import ru.mephi.voip.eventbus.Event
import ru.mephi.voip.ui.catalog.CatalogViewModel
import ru.mephi.voip.utils.network.NetworkSensingBaseActivity
import ru.mephi.voip.utils.setupWithNavController
import ru.mephi.voip.utils.showSnackBar
import timber.log.Timber
import java.util.*


class MainActivity : NetworkSensingBaseActivity(), OnInitializeListener,
    EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityMainBinding
    private lateinit var currentNavController: LiveData<NavController>
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val accountStatusRepository: AccountStatusRepository by inject()
    private val catalogViewModel: CatalogViewModel by inject()

    companion object {
        var phone: AbtoPhone = (appContext as AbtoApplication).abtoPhone

        private const val REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124
        const val CHANNEL_ID: String = "CHANNEL"
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Теперь, когда BottomNavigationBar восстановил свое состояние экземпляра.
        // и его selectItemId, мы можем приступить к настройке
        // BottomNavigationBar с навигацией
        setupBottomNavigationBar()
    }

    override fun onResume() {
        super.onResume()
        phone = (application as AbtoApplication).abtoPhone
        if (!accountStatusRepository.isBackgroundWork.value) {
            if (accountStatusRepository.isSipEnabled.value) {
                enableSip()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)

        EventBus.getDefault().register(this)

        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)

        if (accountStatusRepository.isSipEnabled.value)
            enableSip()

        if (!hasPermissions())
            requestPermissions()

        if (savedInstanceState == null)
            setupBottomNavigationBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        // При полном закрытии приложения останавливаем Call-сервис
        if (!accountStatusRepository.isBackgroundWork.value) {
            disableSip()
            EventBus.getDefault().unregister(this)
        }
        if (!accountStatusRepository.isSipEnabled.value) {
            disableSip()
            EventBus.getDefault().unregister(this)
        }
    }

    private fun setupBottomNavigationBar() {
        val navGraphIds = listOf(
            R.navigation.caller,
            R.navigation.catalog,
            R.navigation.profile
        )

        val navController = binding.bottomNav.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent
        )

        binding.bottomNav.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.catalog -> if (catalogViewModel.catalogStack.size > 1) {
                    while (catalogViewModel.catalogStack.size > 1) {
                        catalogViewModel.goToStartPage()
                    }
                }
            }
        }

        currentNavController = navController
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController.value?.navigateUp() ?: false
    }

    @Subscribe
    fun enableSip(messageEvent: Event.EnableAccount? = null) {
        initAccount(phone)
        initPhone(phone)
    }

    @Subscribe
    fun disableSip(messageEvent: Event.DisableAccount? = null) {
        phone.stopForeground()
        phone.unregister()
        phone.destroy()
    }

    private fun initAccount(abtoPhone: AbtoPhone) {
        if (abtoPhone.isActive)
            return

        val domain = appContext.getString(R.string.sip_domain)
        val account = accountStatusRepository.getActiveAccount()
        val username = account?.login
        val password = account?.password

        if (account != null)
            abtoPhone.config.addAccount(
                domain,
                "",
                username, password, null, "",
                300,
                true
            )
    }

    private fun initPhone(abtoPhone: AbtoPhone) {
        abtoPhone.setNetworkEventListener { connected, _ ->
            if (connected)
                accountStatusRepository.fetchStatus(AccountStatus.LOADING)
            else
                accountStatusRepository.fetchStatus(AccountStatus.NO_CONNECTION)
        }

        //Switching| Registration Listener|REG
        Timber.d("Set Registration Listener")
        abtoPhone.setRegistrationStateListener(object : OnRegistrationListener {
            override fun onRegistered(accId: Long) {
                showSnackBar(binding.mainContainer, AccountStatus.REGISTERED.status)
                accountStatusRepository.fetchStatus(AccountStatus.REGISTERED)
                Timber.d("REG STATUS: ${abtoPhone.getSipProfileState(abtoPhone.currentAccountId).statusCode}")
            }

            override fun onUnRegistered(accId: Long) {
                showSnackBar(binding.mainContainer, "Регистрация аккаунта отменена")
                accountStatusRepository.fetchStatus(AccountStatus.UNREGISTERED)
            }

            override fun onRegistrationFailed(accId: Long, statusCode: Int, statusText: String?) {
                showSnackBar(
                    binding.mainContainer,
                    "Аккаунт \"${abtoPhone.getSipUsername(accId) ?: "null"}\" не зарегистрирован.\nПричина: $statusText"
                )
                accountStatusRepository.fetchStatus(
                    AccountStatus.REGISTRATION_FAILED,
                    "Code: $statusCode, Text: $statusText, ${Calendar.getInstance().time}"
                )
            }
        })

        abtoPhone.setInitializeListener(this)

        if (abtoPhone.isActive)
            return

        val config = abtoPhone.config
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
        config.userAgent = abtoPhone.version()
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
        mBuilder.setSubText(accountStatusRepository.status.value.status)
        mBuilder.setSmallIcon(R.drawable.logo_voip)
        val notification = mBuilder.build()

        abtoPhone.initialize(false) // start service in 'sticky' mode - when app removed from recent service will be restarted automatically
        abtoPhone.initializeForeground(notification) //start service in foreground mode
    }

    override fun onInitializeState(state: InitializeState?, message: String?) {
        when (state) {
            InitializeState.START, InitializeState.INFO, InitializeState.WARNING -> {}
            InitializeState.FAIL -> AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Ok") { dlg, _ -> dlg.dismiss() }.create().show()
            InitializeState.SUCCESS -> {
//                startNextActivity()
            }
            else -> {
            }
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