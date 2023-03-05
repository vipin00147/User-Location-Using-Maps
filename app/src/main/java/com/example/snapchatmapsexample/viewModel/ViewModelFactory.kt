package com.example.snapchatmapsexample.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.example.snapchatmapsexample.base.BaseActivity

class ViewModelFactory(private val baseActivity: BaseActivity<ViewBinding>) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            AuthViewModel(baseActivity) as T
        } else  if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            HomeViewModel(baseActivity) as T
        } else {
            throw IllegalArgumentException("Unknown class name")
        }
    }
}