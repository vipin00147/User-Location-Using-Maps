package com.example.snapchatmapsexample.services

import android.app.Notification
import android.app.Notification.DEFAULT_SOUND
import android.app.Notification.DEFAULT_VIBRATE
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.snapchatmapsexample.R
import com.example.snapchatmapsexample.activities.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data: String? = message.data["data"]
        Log.e("onMessageReceived", "Message Received")
        showNotification("title", "message")
    }

    private fun showNotification(title: String, body: String) {

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val resultIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val resultPendingIntent = PendingIntent.getActivity(
            this,
            101,
            resultIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val contentView = RemoteViews(packageName, R.layout.layout_notifiction)
        val notificationLayoutExpanded = RemoteViews(packageName, R.layout.notification_large)

        if (Build.VERSION.SDK_INT >= 26) {
            //When sdk version is larger than26
            val id = "myFirebaseChannel"
            val description = "143"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(id, description, importance)
            manager.createNotificationChannel(channel)


            val notification = NotificationCompat.Builder(this, id)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent( resultPendingIntent)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            //    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(contentView)
                .setCustomBigContentView(notificationLayoutExpanded)
                .build()

            manager.notify(1, notification)
        } else {
            // When sdk version is less than26
            val notification = NotificationCompat.Builder(this)
                .setContentIntent( resultPendingIntent)
                .setDefaults(DEFAULT_SOUND or DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(contentView)
                .setCustomBigContentView(notificationLayoutExpanded)
                .build()
            manager.notify(1, notification)
        }
    }
}