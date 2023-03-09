package com.example.snapchatmapsexample.utils

import android.app.ActivityManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

object Helper {

    fun Context.isAppRunning(): Boolean {
        val packageName = "com.example.snapchatmapsexample"
        val activityManager = this.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        val procInfos = activityManager.runningAppProcesses
        if (procInfos != null) {
            for (processInfo in procInfos) {
                if (processInfo.processName == packageName) {
                    return true
                }
            }
        }
        return false
    }

}