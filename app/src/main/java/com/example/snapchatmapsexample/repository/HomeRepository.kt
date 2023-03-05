package com.example.snapchatmapsexample.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.viewbinding.ViewBinding
import com.example.snapchatmapsexample.R
import com.example.snapchatmapsexample.activities.MainActivity
import com.example.snapchatmapsexample.model.UserLocation
import com.example.snapchatmapsexample.network.ApiConstants
import com.example.snapchatmapsexample.utils.ConnectivityReceiver
import com.example.snapchatmapsexample.network.responseAndErrorHandle.ApiResponse
import com.google.gson.JsonElement
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeRepository : Callback<JsonElement>, ApiResponse {

    constructor()

    private var userLocationUpdated : MutableLiveData<Boolean> = MutableLiveData()
    lateinit var baseContainer : MainActivity<ViewBinding>
    lateinit var apiResponse : ApiResponse

    var callUpdateUser : Call<JsonElement> ?= null

    fun initContext(baseActivity: MainActivity<ViewBinding>) {
        this.baseContainer = baseActivity
    }

    companion object {
        private const val TAG = "ProductRepository"
        val homeRepository = HomeRepository()
    }


    private fun hitApi(call: Call<JsonElement>?, showProgress: Boolean, context: Context, listener: ApiResponse) {

        if (ConnectivityReceiver().isConnectedOrConnecting(context)) {
            if(showProgress) {
                baseContainer.showLoader()
            }
            call?.enqueue(this)
            apiResponse = listener
        } else {
            baseContainer.showSnackBar(baseContainer.resources.getString(R.string.no_internet_available))
        }
    }


    fun updateUserLocation(data: UserLocation, userLocationUpdated: MutableLiveData<Boolean>) {
        this.userLocationUpdated = userLocationUpdated

        val jsonObject = JSONObject()
        jsonObject.put("lat", data.lat)
        jsonObject.put("lng", data.lng)
        jsonObject.put("address", data.address)
        jsonObject.put("email", data.email)

        val jsonObjectString = jsonObject.toString()

        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        callUpdateUser = ApiConstants.getApiServices().updateUserLocation(requestBody)
        hitApi(callUpdateUser, true, baseContainer, this)

        ApiConstants.getApiServices().updateUserLocation(requestBody).enqueue(this)
    }

    override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
        baseContainer.hideLoader()
        if (response.isSuccessful) {
            apiResponse.onSuccess(call, response.code(), response.body()!!.toString())
        } else {
            baseContainer.showSnackBar("Request Failed")
        }
    }

    override fun onFailure(call: Call<JsonElement>, t: Throwable) {
        baseContainer.hideLoader()
        baseContainer.showSnackBar(t.message.toString())
    }

    override fun onSuccess(call: Call<JsonElement>, responseCode: Int, response: String) {
        if(call == callUpdateUser) {
            userLocationUpdated.value = true
        }
    }

    override fun onError(call: Call<JsonElement>, errorCode: Int, errorMsg: String) {
        baseContainer.hideLoader()
        baseContainer.showSnackBar(errorMsg)
    }

}