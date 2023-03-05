package com.example.snapchatmapsexample.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

open class ConnectivityReceiver : BroadcastReceiver() {
    var connectivityReceiverListener: ConnectivityReceiverListener? = null
    override fun onReceive(context: Context, intent: Intent) {
        if (connectivityReceiverListener != null) {
            connectivityReceiverListener!!.onNetworkConnectionChanged(
                isConnectedOrConnecting(
                    context
                )
            )
        }
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean?)
    }

    fun isConnectedOrConnecting(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        if (activeNetwork != null) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                return true
            } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                return true
            }
        } else {
            return false
        }
        return false
    }
}