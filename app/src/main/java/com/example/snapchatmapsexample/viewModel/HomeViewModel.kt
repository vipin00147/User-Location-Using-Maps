package com.example.snapchatmapsexample.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.example.snapchatmapsexample.base.BaseActivity
import com.example.snapchatmapsexample.model.AllUserModel
import com.example.snapchatmapsexample.model.UserLocation
import com.example.snapchatmapsexample.repository.HomeRepository.Companion.homeRepository

class HomeViewModel(private val baseActivity: BaseActivity<ViewBinding>) : ViewModel() {
    private val logout : MutableLiveData<Boolean> = MutableLiveData()
    private val userLocationUpdated : MutableLiveData<Boolean> = MutableLiveData()
    private val allUsers : MutableLiveData<AllUserModel> = MutableLiveData()

    fun logout() {
        baseActivity.auth.signOut()
        logout.value = true
    }

    fun getLogoutObserver() = logout
    fun getLocationUpdateObserver() = userLocationUpdated

    fun getAllUsersObserver() = allUsers
    fun updateUserLocation(data: UserLocation) {
        homeRepository.updateUserLocation(data, userLocationUpdated)
    }

    fun getAllUsersLocation() {
        homeRepository.getAllUsersLocation(allUsers)
    }
}