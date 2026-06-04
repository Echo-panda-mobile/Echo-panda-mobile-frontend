package com.example.echo_panda_mobile.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.echo_panda_mobile.MainActivity
import com.example.echo_panda_mobile.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class EchoPandaMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            showNotification(it.title ?: "Echo-Panda", it.body ?: "New update available!")
        } ?: run {
            val type = remoteMessage.data["type"]
            val title = remoteMessage.data["title"] ?: "Echo-Panda"
            val body = remoteMessage.data["message"] ?: "New notification"
            
            val formattedTitle = when(type) {
                "follower" -> "New Follower! 👤"
                "comment" -> "New Comment! 💬"
                "milestone" -> "New Milestone! 🎉"
                "new_release" -> "New Song Release! 🎵"
                else -> title
            }
            showNotification(formattedTitle, body)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to your backend if you want to target specific users
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "new_releases_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "New Song Releases",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Use app icon
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(0, notificationBuilder.build())
    }
}
