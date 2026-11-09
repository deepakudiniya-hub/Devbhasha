package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userSession = UserSession(applicationContext)
        val existingToken = userSession.getFcmToken()

        // Sync only if token is actually new
        if (token != existingToken) {
            userSession.setFcmToken(token)

            // Sync token with Firestore for logged in user
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: userSession.getUserId()
            if (currentUid.isNotBlank()) {
                UserManager.updateFcmToken(currentUid, token)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = notification?.title ?: data["title"] ?: "देवभाषा · Devbhasha"
        val body = notification?.body ?: data["body"] ?: ""
        val link = data["link"] ?: ""

        showGenericNotification(applicationContext, title, body, link)
    }

    companion object {
        const val CHANNEL_ID_GENERIC = "consultation_notifications"

        private fun showGenericNotification(context: Context, title: String, body: String, link: String = "") {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID_GENERIC,
                    "Consultation & App Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                if (link.isNotBlank()) {
                    putExtra("EXTRA_NOTIFICATION_LINK", link)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID_GENERIC)
                .setSmallIcon(R.drawable.ic_spiritual_om)
                .setContentTitle(title)
                .setContentText(body)
                .setColor(Color.parseColor("#D97706"))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notificationBuilder.build())
        }
    }
}
