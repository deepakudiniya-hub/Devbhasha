package com.example.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM entry point for Devbhasha.
 *
 * Handles both notification and data messages and shows a local notification.
 * The channel is created lazily, so a push that arrives before the user has
 * ever opened the app still renders.
 *
 * Note: on Android 13+ (API 33) showing a notification also needs the runtime
 * POST_NOTIFICATIONS permission (declared in the manifest). The in-app request
 * prompt is a follow-up — see the PR description.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: persist this token against the signed-in user (Firestore users/{uid})
        // so the backend can target pushes. Left as a hook until the send path exists.
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data["body"] ?: return
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val ctx = applicationContext
        ensureChannel(ctx)

        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            ctx,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(ctx)
                .notify(System.currentTimeMillis().toInt(), notif)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS not granted on Android 13+ — drop silently.
        }
    }

    companion object {
        const val CHANNEL_ID = "devbhasha_default"
        const val CHANNEL_NAME = "Devbhasha"

        /** Creates the default notification channel once (no-op on API < 26). */
        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mgr = context.getSystemService(NotificationManager::class.java) ?: return
                if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply { description = "Devbhasha notifications" }
                    mgr.createNotificationChannel(channel)
                }
            }
        }
    }
}
