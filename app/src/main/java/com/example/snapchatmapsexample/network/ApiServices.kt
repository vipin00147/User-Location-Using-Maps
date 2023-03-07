package com.example.snapchatmapsexample.network

import com.google.gson.JsonElement
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiServices {

    @POST("/get_all_user_location/")
    fun getAllUsersLocation(@Body requestBody: RequestBody): Call<JsonElement>

    @POST("/update_user_location/")
    fun updateUserLocation(@Body requestBody: RequestBody): Call<JsonElement>

}