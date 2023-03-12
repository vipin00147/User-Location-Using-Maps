package com.example.snapchatmapsexample.model

import java.io.Serializable

data class UserLocation(val lat : Double, val lng : Double, val address : String, val email : String) :
    Serializable
