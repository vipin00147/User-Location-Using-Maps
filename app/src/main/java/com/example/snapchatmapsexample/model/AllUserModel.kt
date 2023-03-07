package com.example.snapchatmapsexample.model

data class AllUserModel(val data: List<Data>)
data class Data(
    val Address: String,
    val Email: String,
    val Latitude: String,
    val Longitude: String
)