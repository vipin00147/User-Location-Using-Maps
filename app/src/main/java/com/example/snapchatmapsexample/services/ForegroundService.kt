package com.example.snapchatmapsexample.services

import android.os.Build
import android.content.Intent
import android.os.IBinder
import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import com.example.snapchatmapsexample.R
import android.os.Handler
import androidx.core.app.NotificationCompat
import androidx.annotation.Nullable
import androidx.work.*
import com.example.snapchatmapsexample.activities.MainActivity

class ForegroundService : Service() {

    val CHANNEL_ID = "ForegroundServiceChannel"

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val input = intent.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, FLAG_IMMUTABLE
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)


        //Register Work Manager after 5 second of service start...
        Handler().postDelayed({

            //creating constraints
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresCharging(true)
                // you can add as many constraints as you want
                .build()

            //Work Request
            val workRequest = OneTimeWorkRequest.Builder(MyWorker::class.java)
                .setConstraints(constraints)
                .build()

            //Work Manager
            val workManager = WorkManager.getInstance()

            workManager.enqueue(workRequest)

        }, 5000)



        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY
    }




    override fun onDestroy() {
        super.onDestroy()
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(serviceChannel)
        }
    }
}