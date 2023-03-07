package com.example.snapchatmapsexample.callbacks

import com.example.snapchatmapsexample.model.UserLocation

interface LocationBackgroundCallback {

    fun setUserCurrentLocation(data: UserLocation)
}