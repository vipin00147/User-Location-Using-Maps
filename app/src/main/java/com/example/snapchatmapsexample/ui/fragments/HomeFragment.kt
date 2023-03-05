package com.example.snapchatmapsexample.ui.fragments

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.snapchatmapsexample.R
import com.example.snapchatmapsexample.databinding.FragmentHomeBinding
import com.example.snapchatmapsexample.model.UserLocation
import com.example.snapchatmapsexample.utils.changeFragment
import com.example.snapchatmapsexample.viewModel.HomeViewModel
import com.example.snapchatmapsexample.viewModel.ViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*


class HomeFragment : LocationUpdateUtilityFragment<FragmentHomeBinding>() {

    private var viewModel : HomeViewModel?= null
    var supportMapFragment : SupportMapFragment ?= null

    var animateCount : Int = 0

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

            viewModel?.updateUserLocation(data)

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
        if(getBaseActivity().isGpsEnabled()) {
            getBaseActivity().showLoader()
            getLiveLocation(getBaseActivity())
        }
        else {
            getBaseActivity().showSnackBar("Kindly enable GPS.")
        }

        getAllFriendsLocation()
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
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, ViewModelFactory(getBaseActivity()))[HomeViewModel::class.java]
        binding?.model = viewModel
    }

    private fun initMap() {
        supportMapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?

        // Async map
        supportMapFragment?.getMapAsync { googleMap ->
            // When map is loaded
            googleMap.setOnMapClickListener { latLng ->
                // Initialize marker options
                val markerOptions = MarkerOptions()
                // Set position of marker
                markerOptions.position(latLng)
                // Set title of marker
                markerOptions.title(latLng.latitude.toString() + " : " + latLng.longitude)
                // Remove all marker
                googleMap.clear()
                // Animating to zoom the marker
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                // Add marker on map
                googleMap.addMarker(markerOptions)
            }
        }
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
        supportMapFragment?.getMapAsync { googleMap ->
            val markerOptions = MarkerOptions()
            // Set position of marker
            markerOptions.position(LatLng(lat,lng))
            // Set title of marker
            markerOptions.title(getBaseActivity().auth.currentUser?.email.toString())
            // Remove all marker
            googleMap.clear()

            if(animateCount == 0) {
                // Animating to zoom the marker only for the first time
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat,lng), 30f))
            }

            // Add marker on map
            googleMap.addMarker(markerOptions)
            animateCount++
        }
    }

    private fun getAllFriendsLocation() {

    }


}