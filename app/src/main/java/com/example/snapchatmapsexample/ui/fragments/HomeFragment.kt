package com.example.snapchatmapsexample.ui.fragments

import android.app.ActivityManager
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.snapchatmapsexample.R
import com.example.snapchatmapsexample.base.BaseActivity.Companion.getLocationCallback
import com.example.snapchatmapsexample.base.BaseActivity.Companion.isServiceEnded
import com.example.snapchatmapsexample.base.BaseActivity.Companion.locationBackgroundCallback
import com.example.snapchatmapsexample.callbacks.GetLocationCallback
import com.example.snapchatmapsexample.databinding.FragmentHomeBinding
import com.example.snapchatmapsexample.model.AllUserModel
import com.example.snapchatmapsexample.model.UserLocation
import com.example.snapchatmapsexample.services.ForegroundService
import com.example.snapchatmapsexample.utils.changeFragment
import com.example.snapchatmapsexample.viewModel.HomeViewModel
import com.example.snapchatmapsexample.viewModel.ViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import java.util.*

class HomeFragment : LocationUpdateUtilityFragment<FragmentHomeBinding>(), OnMapReadyCallback,
    GetLocationCallback {

    private var viewModel : HomeViewModel?= null
    var supportMapFragment : SupportMapFragment ?= null
    var googleMap: GoogleMap ?= null

    var animateCount : Int = 0
    var count  = 0

    override fun updatedLatLng(lat: Double, lng: Double) {
        getBaseActivity().hideLoader()

        if(getBaseActivity().auth.currentUser?.email != null) {
            //updating location to firebase
            val data = UserLocation(
                lat,
                lng,
                getCompleteAddressString(lat, lng),
                getBaseActivity().auth.currentUser?.email.toString()
            )

            if(count == 0) {
                viewModel?.updateUserLocation(data)
                count++
            }

            if(!isServiceEnded) {
                locationBackgroundCallback?.setUserCurrentLocation(data)
            }

            setCurrentLocation(lat, lng)
        }




        stopLocationUpdates()
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMap()
        initViewModel()
        viewModelObserver()
        getLocationCallback = this
        getAllFriendsLocation()

        binding?.buttonStartService?.setOnClickListener{
            startService(it)
        }
        binding?.buttonStopService?.setOnClickListener {
            stopService(it)
        }

    }

    fun startService(view : View) {

        if(isLocationServiceRunning()) {
            Snackbar.make(view,"Service is Already Running", Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.RED)
                .setActionTextColor(Color.WHITE)
                .setAction("Dismiss",View.OnClickListener {})
                .show()
        }
        else {

            val serviceIntent = Intent(getBaseActivity(), ForegroundService::class.java)
            serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android")
            ContextCompat.startForegroundService(getBaseActivity(), serviceIntent)

            Snackbar.make(view,"Service Started", Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(getBaseActivity(), R.color.green))
                .setActionTextColor(Color.WHITE)
                .setAction("Dismiss",View.OnClickListener {})
                .show()

            getCurrentLocation()

        }
    }

    private fun getCurrentLocation() {
        // Get Device Location...

        if(getBaseActivity().isGpsEnabled()) {
            getBaseActivity().showLoader()
            getLiveLocation(getBaseActivity())
        }
        else {
            getBaseActivity().showSnackBar("Kindly enable GPS.")
        }
    }

    //Stop Running Service
    fun stopService(view : View) {

        val serviceIntent = Intent(getBaseActivity(), ForegroundService::class.java)

        if(isLocationServiceRunning()) {
            getBaseActivity().stopService(serviceIntent)

            Snackbar.make(view,"Service Stopped", Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(getBaseActivity(), R.color.green))
                .setActionTextColor(Color.WHITE)
                .setAction("Dismiss",View.OnClickListener {})
                .show()
        }
        else{
            Snackbar.make(view,"Service is Already Stopped", Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.RED)
                .setActionTextColor(Color.WHITE)
                .setAction("Dismiss",View.OnClickListener {})
                .show()
        }
    }

    //Check if Service is Running or Not
    private fun isLocationServiceRunning(): Boolean {
        val activityManager = getBaseActivity().getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        if (activityManager != null) {
            for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
                if (ForegroundService::class.java.name == service.service.className) {
                    if (service.foreground) {
                        return true
                    }
                }
            }
            return false
        }
        return false
    }

    private fun viewModelObserver() {
        viewModel?.getLogoutObserver()?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(it) {
                AuthFragment().changeFragment(R.id.mainContainer, getBaseActivity(), false)
            }
        })

        viewModel?.getLocationUpdateObserver()?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(it) {
                getBaseActivity().showSnackBar("Location Updated.")
            }
        })

        viewModel?.getAllUsersObserver()?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            Handler().postDelayed({
                setUserLOcationOMap(it)
            },2000)

        })
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, ViewModelFactory(getBaseActivity()))[HomeViewModel::class.java]
        binding?.model = viewModel
    }

    private fun initMap() {
        supportMapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?
        supportMapFragment?.getMapAsync(this)

    }

    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(getBaseActivity(), Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()

            } else {
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return strAdd
    }

    private fun setCurrentLocation(lat: Double, lng: Double) {

        val markerOptions = MarkerOptions()
        // Set position of marker
        markerOptions.position(LatLng(lat,lng))
        // Set title of marker
        markerOptions.title(getBaseActivity().auth.currentUser?.email.toString())
        // Remove all marker
        googleMap?.clear()

        if(animateCount == 0) {
            // Animating to zoom the marker only for the first time
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat,lng), 30f))
        }

        // Add marker on map
        googleMap?.addMarker(markerOptions)
        animateCount++

    }

    private fun getAllFriendsLocation() {
        viewModel?.getAllUsersLocation()
    }

    private fun setUserLOcationOMap(data: AllUserModel) {

        data.data.forEach {
            googleMap?.addMarker(MarkerOptions().position(LatLng(it.Latitude.toDouble(),  it.Longitude.toDouble())).title("${it.Email}"))
        }

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    override fun getLocation() {
        getCurrentLocation()
    }
}