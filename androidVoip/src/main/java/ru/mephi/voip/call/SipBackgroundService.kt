package ru.mephi.voip.call

import android.app.*
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.abtollc.sdk.*
import org.abtollc.utils.codec.Codec
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import ru.mephi.shared.appContext
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.call.utils.getActiveAccount
import ru.mephi.voip.ui.MainActivity
import ru.mephi.voip.ui.eventbus.Event
import timber.log.Timber

class SipBackgroundService : Service(), OnInitializeListener {
    private val sp: SharedPreferences by inject()

    private val _status = MutableStateFlow(AccountStatus.UNREGISTERED)
    val status: StateFlow<AccountStatus> = _status

    companion object {
        private const val CHANNEL_ID: String = "CHANNEL"
        private var phone: AbtoPhone? = null
    }

    inner class SipBinder : Binder() {
        fun getService(): SipBackgroundService = this@SipBackgroundService
    }

    override fun onBind(intent: Intent): IBinder {
//        if (!sp.getBoolean(getString(R.string.background_work_settings), false)) {
        if (sp.getBoolean(getString(R.string.sp_sip_enabled), false)) {
            phone = (application as AbtoApplication).abtoPhone
            enableSip()
        }
//        }

        return SipBinder()
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Sip Service Destroyed")
        EventBus.getDefault().unregister(this)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        // При полном закрытии приложения останавливаем Call-сервис
        if (!sp.getBoolean(getString(R.string.background_work_settings), false)) {
            // abtoPhone.stopForeground() // Убираем уведомления, но abto сервис ещё работает
            disableSip()
        }
        return super.onUnbind(intent)
    }

    @Subscribe
    fun enableSip(messageEvent: Event.EnableAccount? = null) {
        phone = (application as AbtoApplication).abtoPhone
        phone?.let {
            initAccount(it)
            initPhone(it)
        }
    }

    @Subscribe
    fun disableSip(messageEvent: Event.DisableAccount? = null) {
        phone?.let {
            it.unregister()
            it.destroy()
        }
    }

    private fun initAccount(abtoPhone: AbtoPhone) {
        if (abtoPhone.isActive)
            return

        val domain = appContext.getString(R.string.sip_domain)
        val account = getActiveAccount()
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
                fetchStatus(AccountStatus.LOADING)
            else
                fetchStatus(AccountStatus.NO_CONNECTION)
        }

        //Switching| Registration Listener|REG
        Timber.d("Set Registration Listener")
        abtoPhone.setRegistrationStateListener(object : OnRegistrationListener {
            override fun onRegistered(accId: Long) {
//                showSnackBar(binding.mainContainer, AccountStatus.REGISTERED.status)
                fetchStatus(AccountStatus.REGISTERED)
                Timber.d("REG STATUS: ${abtoPhone.getSipProfileState(abtoPhone.currentAccountId).statusCode}")
            }

            override fun onUnRegistered(accId: Long) {
//                showSnackBar(binding.mainContainer, "Регистрация аккаунта отменена")
                fetchStatus(AccountStatus.UNREGISTERED)
            }

            override fun onRegistrationFailed(accId: Long, statusCode: Int, statusText: String?) {
//                showSnackBar(
//                    binding.mainContainer,
//                    "Аккаунт \"${abtoPhone.getSipUsername(accId) ?: "null"}\" не зарегистрирован.\nПричина: $statusText"
//                )
                fetchStatus(AccountStatus.REGISTRATION_FAILED)
            }
        })

        abtoPhone.setInitializeListener(this)

        if (abtoPhone.isActive)
            return

        val config = abtoPhone.config
        for (c in Codec.values()) config.setCodecPriority(c, 0.toShort())
        config.setCodecPriority(Codec.G729, 78.toShort())
        config.setCodecPriority(Codec.PCMA, 80.toShort())
        config.setCodecPriority(Codec.PCMU, 79.toShort())
        config.setCodecPriority(Codec.H264, 220.toShort())
        config.setCodecPriority(Codec.H263_1998, 0.toShort())
        config.setSignallingTransport(AbtoPhoneCfg.SignalingTransportType.UDP) //TCP);//TLS);
        config.setKeepAliveInterval(AbtoPhoneCfg.SignalingTransportType.UDP, 30)
        config.sipPort = 0

//        config.setLicenseUserId("{Trial5216aded467d-BB77-DD90-AA3B618C-E998-45F5-B963-AE652D04101E}")
//        config.setLicenseKey("{olRZLmHtd5P2rtWeUrTwl5zCii9dej80R5TfiEIHbBBZQGP1e1oZ1WG682DsCCRlNy7zeGmZzMZFwChnpiErOw==}")

        config.isUseSRTP = false
        config.userAgent = abtoPhone.version()
        config.hangupTimeout = 3000
        config.enableSipsSchemeUse = false
        config.isSTUNEnabled = false
        AbtoPhoneCfg.setLogLevel(7, true)

        val mNotificationId = 1

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_NONE
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        abtoPhone.initialize(false) // start service in 'sticky' mode - when app removed from recent service will be restarted automatically

        // Обновление sip-статуса из профиля, подписка на viewModel
        CoroutineScope(Dispatchers.IO).launch {
            status.collect {
                val mBuilder = NotificationCompat.Builder(
                    this@SipBackgroundService,
                    CHANNEL_ID
                ).apply {
                    setAutoCancel(false)
                    setOngoing(true)
                    setContentIntent(pendingIntent)
                    setContentText(getString(R.string.notification_title) + ": ${it.status}")
                    setSubText(getString(R.string.notification_message))
                    setSmallIcon(R.drawable.logo_voip)
                }

                val notification = mBuilder.build()

                notificationManager.notify(mNotificationId, notification)
            }
        }

        // Первое уведомление для инициализации abtoPhone с его помощью
        val mBuilder = NotificationCompat.Builder(this@SipBackgroundService, CHANNEL_ID).apply {
            setAutoCancel(false)
            setOngoing(true)
            setWhen(0L)
            setContentIntent(pendingIntent)
            setContentText(getString(R.string.notification_title) + ": ${AccountStatus.LOADING.status}")
            setSubText(getString(R.string.notification_message))
            setSmallIcon(R.drawable.logo_voip)
        }

        val notification = mBuilder.build()

        abtoPhone.initializeForeground(notification) //start service in foreground mode
    }

    fun fetchStatus(newStatus: AccountStatus? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            _status.emit(AccountStatus.LOADING)

            status.replayCache.lastOrNull()?.let { lastStatus ->
                Timber.d("Switching Status from: \"${lastStatus}\" to \"${newStatus?.status}\"")
            }

            phone?.let {
                if (newStatus == null && phone!!.getSipProfileState(phone!!.currentAccountId)?.statusCode == 200) {
                    // На случай, если активность была удалена, а AbtoApp активен и
                    // statusCode аккаунт = 200 (зарегистрирован). Вызывается при отрисовке фрагмента
                    _status.emit(AccountStatus.REGISTERED)
                } else if (newStatus != null)
                    _status.emit(newStatus)
            }
        }

    }

    override fun onInitializeState(state: OnInitializeListener.InitializeState?, message: String?) {
        when (state) {
            OnInitializeListener.InitializeState.START, OnInitializeListener.InitializeState.INFO, OnInitializeListener.InitializeState.WARNING -> {
            }
            OnInitializeListener.InitializeState.FAIL -> AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Ok") { dlg, _ -> dlg.dismiss() }.create().show()
            OnInitializeListener.InitializeState.SUCCESS -> {
//                startNextActivity()
            }
            else -> {
            }
        }
    }

}