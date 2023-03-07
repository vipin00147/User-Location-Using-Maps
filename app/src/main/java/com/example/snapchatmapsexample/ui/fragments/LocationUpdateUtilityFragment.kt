package com.example.snapchatmapsexample.ui.fragments

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.example.snapchatmapsexample.BuildConfig.APPLICATION_ID
import com.example.snapchatmapsexample.R
import com.example.snapchatmapsexample.base.BaseFragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*


abstract class LocationUpdateUtilityFragment<T : ViewDataBinding> : BaseFragment<T>() {

    private val TAG = "LocationUpdateUtilityfragment"
    private lateinit var mActivity: Activity
    private var isLocationDialogVisible : Boolean = false
    private var locationRequest: LocationRequest? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

           if (permissions.isNotEmpty()) {

               Handler().postDelayed({
                   getBaseActivity().showLoader()
               },1000)

               permissions.entries.forEach {
                   Log.d(TAG, "${it.key} = ${it.value}")
               }

               val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION]
               val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION]

               if (fineLocation==true && coarseLocation==true) {
                   Log.e(TAG, "Permission Granted Successfully")
                  checkGpsOn()
               } else {
                   getBaseActivity().showLoader()
                   Log.e(TAG, "Permission not granted")
                   checkPermissionDenied(permissions.keys.first())
               }
           }
        }

    private val gpsOnLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                Log.e(TAG, "GPS Turned on successfully")
                startLocationUpdates()
            } else if (result.resultCode == RESULT_CANCELED) {
                Log.e(TAG, "GPS Turned on failed")
                locAlertDialogMethod()
            }
        }

    private fun locAlertDialogMethod() {
//        val locationDialog = Dialog(requireContext(), R.style.Theme_Dialog)
//        locationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        locationDialog.setContentView(R.layout.location_alert)
//
//        locationDialog.window!!.setLayout(
//            WindowManager.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.WRAP_CONTENT
//        )
//        locationDialog.setCancelable(true)
//        locationDialog.setCanceledOnTouchOutside(true)
//        locationDialog.window!!.setGravity(Gravity.CENTER)
//
//        locationDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//        var btnTryAgain=locationDialog.findViewById<Button>(R.id.btnTryAgain)
//
//        btnTryAgain.setOnClickListener{
//            locationDialog.dismiss()
//            checkGpsOn()
//        }
//        locationDialog.show()
        checkGpsOn()
    }

    open fun getLiveLocation(activity: Activity) {

        mActivity = activity

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity)

        checkLocationPermissions()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {

                    // Update UI with location data

                    Log.e(
                        TAG, "==========" + location.latitude.toString() + ", " +
                                location.longitude + "========="
                    )

                    if (ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            getBaseActivity().hideLoader()

                            Log.e(
                                "mylocatioattributeis",
                                "${location?.latitude}, ${location?.longitude}"
                            )
                        }
                    //updatedLatLng(location.latitude, location.longitude)
                }
            }
        }
    }

    fun checkLocationPermissions() {
        if (hasPermissions(permissions)) {
            Log.e(TAG, "Permissions Granted")
           // getLiveLocation(requireActivity())
            checkGpsOn()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            checkPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            checkPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            Log.e(TAG, "Request for Permissions")
            requestPermission()
        }
    }

    // util method
    private fun hasPermissions(permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(mActivity, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestMultiplePermissions.launch(permissions)
    }

    private fun checkPermissionDenied(permission: String) {
        if (shouldShowRequestPermissionRationale(permission)) {

            if(!isLocationDialogVisible) {
                Log.e(TAG, "Permissions Denied")
                val mBuilder = AlertDialog.Builder(mActivity)
                val dialog: AlertDialog =
                    mBuilder.setTitle("")
                        .setMessage("Location permission needed for core functionality")
                        .setPositiveButton(
                            "ok"
                        ) { dialog, which ->
                            // Request permission
                            requestPermission()
                            isLocationDialogVisible = false
                        }.create()
                dialog.setOnShowListener {
                    isLocationDialogVisible = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                        ContextCompat.getColor(
                            mActivity, R.color.black
                        )
                    )
                }
                dialog.show()
                isLocationDialogVisible = true
            }



        } else {
            val builder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                builder.setTitle("")
                    .setMessage("Permission was denied, but is needed for core functionality.")
                    .setCancelable(
                        false
                    )
                    .setPositiveButton("Settings") { dialog, which ->
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            APPLICATION_ID,
                            null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        mActivity, R.color.black
                    )
                )
            }
            dialog.show()
//            locAlertDialogMethod()

        }
    }

    private fun locationPermission(permissions: Array<String>): Boolean {
        return ActivityCompat.checkSelfPermission(
            mActivity,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            mActivity,
            permissions[1]
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun checkGpsOn() {
        locationRequest = LocationRequest.create()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.interval = 5000
        locationRequest?.fastestInterval = 2000
        locationRequest?.isWaitForAccurateLocation = true

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest!!)
        builder.setAlwaysShow(true)


        val result = LocationServices.getSettingsClient(mActivity).checkLocationSettings(builder.build())
        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                Log.e(TAG, "==========GPS is ON=============")

                startLocationUpdates()
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        val resolvableApiException = e as ResolvableApiException
                        gpsOnLauncher.launch(
                            IntentSenderRequest.Builder(resolvableApiException.resolution).build()
                        )

                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                    }
                }
            }
        }

    }

    //call startLocationUpdates() method for start live location update
    fun startLocationUpdates() {
        getBaseActivity().hideLoader()
        if (ActivityCompat.checkSelfPermission(
                mActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            hasPermissions(permissions)
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest!!,
            locationCallback,
            Looper.getMainLooper()
        )


        /*fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                Log.e("mylocatioattributeis", "${location?.latitude}, ${location?.longitude}")
            }*/




        Log.e(TAG, "Get Live Location Start")
    }


    //call stopLocationUpdates() method for stop live location update
    fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Log.e(TAG, "Get Live Location Stop")
        } catch (e: Exception) {
           e.printStackTrace()
        }
    }

    abstract fun updatedLatLng(lat: Double, lng: Double)
}