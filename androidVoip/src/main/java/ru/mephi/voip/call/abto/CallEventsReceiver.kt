package ru.mephi.voip.call.abto

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.provider.Settings
import androidx.core.app.NotificationCompat
import org.abtollc.sdk.AbtoPhone
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.voip.R
import ru.mephi.voip.ui.call.CallActivity
import timber.log.Timber

class CallEventsReceiver : BroadcastReceiver(), KoinComponent {
    lateinit var abtoApp: AbtoApp
    private val sp: SharedPreferences by inject()

    override fun onReceive(context: Context, intent: Intent) {
        abtoApp = context.applicationContext as AbtoApp
        val bundle = intent.extras ?: return
        when {
            // Входящий звонок
            bundle.getBoolean(AbtoPhone.IS_INCOMING, false) -> {
                Timber.d("INCOMING CALL")
                buildIncomingCallNotification(context, bundle)
            }

            // Отклонение звонка
            bundle.getBoolean(KEY_REJECT_CALL, false) -> {
                val callId = bundle.getInt(AbtoPhone.CALL_ID)
                cancelIncCallNotification(context, callId)
                try {
                    abtoApp.abtoPhone?.rejectCall(callId)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
            bundle.getInt(AbtoPhone.CODE) == -1 -> {
                // Cancel call
                val callId = bundle.getInt(AbtoPhone.CALL_ID)
                cancelIncCallNotification(context, callId)
            }
        }
    }

    private fun buildIncomingCallNotification(context: Context, bundle: Bundle) {
        val intent = Intent(context, CallActivity::class.java)
        intent.putExtras(bundle)

        // Проверка, что приложение сейчас в фоне и что есть разрешение на показ поверх других приложений
        // тогда открываем активность звонка и всё ок, иначе - уведомление для взаимодействия
        if (abtoApp.isAppInBackground && Settings.canDrawOverlays(context)) {
            if (sp.getBoolean(context.getString(R.string.call_screen_always_settings), false)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }
        }

        if (!abtoApp.isAppInBackground && sp.getBoolean(
                context.getString(R.string.call_screen_always_settings),
                false
            )
        ) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        }

        val title = "Входящий звонок"
        val remoteContact = bundle.getString(AbtoPhone.REMOTE_CONTACT)
        val callId = bundle.getInt(AbtoPhone.CALL_ID)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channelCall == null) {
            channelCall = NotificationChannel(
                CHANNEL_CALL_ID,
                context.getString(R.string.app_name) + "Call",
                NotificationManager.IMPORTANCE_HIGH
            )
            channelCall!!.description = context.getString(R.string.app_name)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelCall!!)
        }

        val notificationPendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Намерение для кнопки принятия звонка
        val pickUpAudioIntent = Intent(context, CallActivity::class.java)
        pickUpAudioIntent.putExtras(bundle)
        pickUpAudioIntent.putExtra(KEY_PICK_UP_AUDIO, true)
        val pickUpAudioPendingIntent = PendingIntent.getActivity(
            context, 2,
            pickUpAudioIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Намерение для кнопки отклонения звонка
        val rejectCallIntent = Intent()
        rejectCallIntent.setPackage(context.packageName)
        rejectCallIntent.action = AbtoPhone.ACTION_ABTO_CALL_EVENT
        rejectCallIntent.putExtra(AbtoPhone.CALL_ID, callId)
        rejectCallIntent.putExtra(KEY_REJECT_CALL, true)
        val pendingRejectCall = PendingIntent.getBroadcast(
            context, 4, rejectCallIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or FLAG_IMMUTABLE
        )

        // Style for popup notification
        val bigText = NotificationCompat.BigTextStyle()
        bigText.bigText(remoteContact)
        bigText.setBigContentTitle(title)

        // Create notification
        val builder = NotificationCompat.Builder(context, CHANNEL_CALL_ID)
        builder.setSmallIcon(R.drawable.ic_baseline_dialer_sip_24)
//            .setColor(-0xff0100)
            .setContentTitle(title)
            .setContentIntent(notificationPendingIntent)
            .setContentText(remoteContact)
            .setDefaults(Notification.DEFAULT_ALL)
            .setStyle(bigText)
            .setPriority(Notification.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setSmallIcon(R.drawable.logo_voip)
            .addAction(R.drawable.ic_baseline_call_end_24, "Отклонить", pendingRejectCall)
            .addAction(R.drawable.ic_baseline_volume_up_24, "Принять", pickUpAudioPendingIntent)
            .setFullScreenIntent(notificationPendingIntent, true)
        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = builder.build()
        mNotificationManager.notify(NOTIFICATION_INCOMING_CALL_ID + callId, notification)
    }

    companion object {
        const val CHANNEL_CALL_ID = "abto_phone_call"
        private var channelCall: NotificationChannel? = null
        const val KEY_PICK_UP_AUDIO = "KEY_PICK_UP_AUDIO"
        const val KEY_REJECT_CALL = "KEY_REJECT_CALL"
        private const val NOTIFICATION_INCOMING_CALL_ID = 1000

        fun cancelIncCallNotification(context: Context, callId: Int) {
            val mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.cancel(NOTIFICATION_INCOMING_CALL_ID + callId)
        }
    }
}