package com.example.snapchatmapsexample.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.snapchatmapsexample.R
import com.example.snapchatmapsexample.activities.MainActivity
import com.example.snapchatmapsexample.base.BaseActivity
import com.example.snapchatmapsexample.base.BaseActivity.Companion.getLocationCallback
import com.example.snapchatmapsexample.base.BaseActivity.Companion.isServiceEnded
import com.example.snapchatmapsexample.base.BaseActivity.Companion.locationBackgroundCallback
import com.example.snapchatmapsexample.callbacks.LocationBackgroundCallback
import com.example.snapchatmapsexample.model.UserLocation
import com.example.snapchatmapsexample.network.ApiConstants
import com.example.snapchatmapsexample.repository.HomeRepository.Companion.homeRepository
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat


class MyWorker: Worker, Callback<JsonElement>, LocationBackgroundCallback {

    var getCallUpdateLocation: Call<JsonElement>? = null
    var context: Context ?= null

    constructor(context: Context, workerParams: WorkerParameters) : super(context, workerParams) {

    }

    override fun doWork(): Result {
        this.context = applicationContext
        locationBackgroundCallback = this
        getLocationCallback?.getLocation()
        return Result.success()
    }

    private fun displayNotification(title: String, s: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                "simplifiedcoding",
                "simplifiedcoding",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val bundle = Bundle()
        bundle.putString("data", "Main Activity is Launched")
        intent.putExtras(bundle)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        val activity = PendingIntent.getActivity(applicationContext, 0, intent,
            PendingIntent.FLAG_MUTABLE
        )

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val notification: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, "simplifiedcoding")
                .setAutoCancel(false)
                .setContentTitle("Location")
                .setContentText("Location Updated")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(activity)
                .setStyle(NotificationCompat.BigTextStyle().bigText("Location Updated"))
        notificationManager.notify(1, notification.build())

    }



    override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
        if (response.isSuccessful) {

            onSuccess(call, response.code(), response.body()!!.toString())
        }
        else{
            Log.e("outoutdatais", "Not Success")
        }
    }

    private fun onSuccess(call: Call<JsonElement>, code: Int, response: String) {

        if(call == getCallUpdateLocation) {
            Log.e("outoutdatais", "Success")

            displayNotification("My Worker", "Location is Updated")

        }

    }

    override fun onFailure(call: Call<JsonElement>, t: Throwable) {
        Log.e("outoutdatais", t.message.toString())
    }

    override fun setUserCurrentLocation(data: UserLocation) {
        isServiceEnded = true

        Log.d("outoutdatais", "setUserCurrentLocation()")

        /*CoroutineScope(Dispatchers.IO).launch {
            CoroutineScope(Dispatchers.Main).launch {
                homeRepository.updateUserLocation(data, MutableLiveData(true))
            }
        }*/


        GlobalScope.launch {


            //val jsonObject = Gson().toJson(UserLocation(0.0, 0.0, "", ""))
            val jsonObject = Gson().toJson(data)
            val jsonObjectString = jsonObject.toString()
            val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

            getCallUpdateLocation = ApiConstants.getApiServices().updateUserLocation(requestBody)
            getCallUpdateLocation!!.enqueue(this@MyWorker)
        }
    }
}