package ru.mephi.voip.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.ui.MasterActivity

class NotificationHandler(private val context: Context) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val mNotificationId = 1
    private val CHANNEL_ID = "CHANNEL"
    private val title = context.getString(R.string.notification_title)

    fun updateNotificationStatus(
        accountStatus: AccountStatus, extraTitle: String = "", statusCode: String = ""
    ): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_NONE
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MasterActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
        val notification = with(mBuilder) {
            setAutoCancel(false)
            setOngoing(true)
            setContentIntent(pendingIntent)
            setContentText(statusCode.ifEmpty { if (extraTitle.isEmpty()) title else "$title: $extraTitle" })
            setSubText(accountStatus.status)
            setSmallIcon(R.drawable.logo_voip)
            setVisibility(VISIBILITY_PUBLIC)
        }.build()

        notificationManager.notify(mNotificationId, notification)

        return notification
    }
}