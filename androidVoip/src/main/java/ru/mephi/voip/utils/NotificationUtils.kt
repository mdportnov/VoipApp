package ru.mephi.voip.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.ui.MasterActivity
import ru.mephi.voip.data.PreferenceRepository
import ru.mephi.voip.utils.NotificationUtils.intentFlags
import ru.mephi.voip.utils.NotificationUtils.mNotificationId
import ru.mephi.voip.utils.NotificationUtils.pendingIntentFlags
import timber.log.Timber

class NotificationHandler(
    private val context: Context,
    private val notificationReciever: NotificationReciever
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val mChannelId = "MEPhI"
    private val mChannelName = context.getString(R.string.app_name)

    private val mBuilder by lazy {
        also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(mChannelId, mChannelName, NotificationManager.IMPORTANCE_NONE)
                notificationManager.createNotificationChannel(channel)
            }
        }
        NotificationCompat.Builder(context, mChannelId)
    }

    private val intent = Intent(context, MasterActivity::class.java).also { it.addFlags(intentFlags) }
    private val contentIntent = PendingIntent.getActivity(context, 0x0, intent, pendingIntentFlags)
    private val intentHelper = Intent(NotificationUtils.disableSipAction)
    private val actionIntent = PendingIntent.getBroadcast(context, 0x0, intentHelper, pendingIntentFlags)

    fun getDisplayedNotification(status: AccountStatus): Notification {
        notificationReciever.enable()
        with(mBuilder) {
            setAutoCancel(false)
            setOngoing(true)
            setContentIntent(contentIntent)
            setSmallIcon(R.drawable.ic_launcher_foreground)
            if (mActions.isEmpty()) {
                addAction(R.drawable.ic_outline_dialer_sip, "Выключить SIP", actionIntent)
            }
            setSubText("Фоновый режим")
            setContentTitle("Статус: ${status.status}")
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }.build().let {
            notificationManager.notify(mNotificationId, it)
            return it
        }
    }


}

class NotificationReciever(
    private val context: Context,
    private var preferenceRepo: PreferenceRepository
) : BroadcastReceiver() {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var mIsListening = false

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == NotificationUtils.disableSipAction) {
            notificationManager.cancel(mNotificationId)
            disable()
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    preferenceRepo.enableSip(false)
                }
            } catch (e: NullPointerException) { }
        }
    }

    fun enable() {
        if (mIsListening) return
        Timber.d("NotificationReciever: enabling!")
        context.registerReceiver(this, IntentFilter(NotificationUtils.disableSipAction))
        mIsListening = true
    }

    private fun disable() {
        if (!mIsListening) return
        Timber.d("NotificationReciever: disabling!")
        context.unregisterReceiver(this)
        mIsListening = false
    }

}

private object NotificationUtils {
    const val intentFlags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
    const val pendingIntentFlags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

    const val mNotificationId = 1

    const val disableSipAction = "ru.mephi.voip.EXIT_SIP_ACTION"
}
