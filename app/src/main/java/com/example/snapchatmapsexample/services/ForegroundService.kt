package com.example.snapchatmapsexample.services

import android.app.*
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.snapchatmapsexample.R
import com.example.snapchatmapsexample.activities.MainActivity
import com.example.snapchatmapsexample.base.BaseActivity
import com.example.snapchatmapsexample.model.UserLocation
import com.example.snapchatmapsexample.network.ApiConstants
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat

class ForegroundService : Service(), Callback<JsonElement> {

    val CHANNEL_ID = "ForegroundServiceChannel"
    var getCallUpdateLocation: Call<JsonElement>? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val input = intent.getStringExtra("inputExtra")

        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, FLAG_MUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        //Register Work Manager after 5 second of service start...

        //creating constraints
        /*val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            // you can add as many constraints as you want
            .build()

        //Work Request
        val workRequest = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .setConstraints(constraints)
            //.setInitialDelay(10, TimeUnit.SECONDS)
            .build()


        //Work Manager
        val workManager = WorkManager.getInstance()

        workManager.enqueue(workRequest)*/
        updateDeviceLocation()

        //stopSelf();

        return START_NOT_STICKY
    }

    private fun updateDeviceLocation() {
        ContextCompat.getMainExecutor(this).execute {
            object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {

                    // Used for formatting digit to be in 2 digits only
                    val f : NumberFormat = DecimalFormat("0")
                    val hour = millisUntilFinished / 3600000 % 24
                    val min = millisUntilFinished / 60000 % 60
                    val sec = millisUntilFinished / 1000 % 60

                    Log.e("outoutdatais", "$sec")

                }

                override fun onFinish() {
                    Log.e("outoutdatais", "On Finish")
                    BaseActivity.getLocationCallback?.getLocation()
                }
            }.start()
        }
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

    fun setUserCurrentLocation(data: UserLocation) {
        BaseActivity.isServiceEnded = true

        Log.d("outoutdatais", "setUserCurrentLocation()")

        GlobalScope.launch {

            val jsonObject = Gson().toJson(data)
            val jsonObjectString = jsonObject.toString()
            val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

            getCallUpdateLocation = ApiConstants.getApiServices().updateUserLocation(requestBody)
            getCallUpdateLocation!!.enqueue(this@ForegroundService)
        }
    }

    override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
        if (response.isSuccessful) {
            onSuccess(call, response.code(), response.body()!!.toString())
        }
        else {
            Log.e("outoutdatais", "Not Success")
        }
    }

    override fun onFailure(call: Call<JsonElement>, t: Throwable) {
        Log.e("outoutdatais", t.message.toString())
    }

    private fun onSuccess(call: Call<JsonElement>, code: Int, response: String) {

        if(call == getCallUpdateLocation) {
            Log.e("outoutdatais", "Success")

            //displayNotification("My Worker", "Location is Updated")

        }
    }
}