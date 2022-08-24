package ru.mephi.voip.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.ui.MasterActivity
import ru.mephi.voip.utils.NotificationUtils.intentFlags
import ru.mephi.voip.utils.NotificationUtils.pendingIntentFlags

class NotificationHandler(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val mNotificationId = 1
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

    private var statusListener: Job? = null

    fun setStatusListener(
        status: Flow<AccountStatus>
    ) {
        statusListener?.cancel()
        statusListener = CoroutineScope(Dispatchers.IO).launch {
            status.collect { status ->
                val notification = with(mBuilder) {
                    setAutoCancel(false)
                    setOngoing(true)
                    setContentIntent(contentIntent)
                    setSmallIcon(R.drawable.logo_voip)
                    setContentTitle("Статус: ${status.status}")
                }.build()
                notificationManager.notify(mNotificationId, notification)
            }
        }
    }

    fun removeStatusListener() {
        statusListener?.cancel()
        notificationManager.cancel(mNotificationId)
    }
}

private object NotificationUtils {
    const val intentFlags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
    const val pendingIntentFlags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
}