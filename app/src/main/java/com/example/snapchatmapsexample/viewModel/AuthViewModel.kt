package com.example.snapchatmapsexample.viewModel

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.example.snapchatmapsexample.base.BaseActivity
import com.example.snapchatmapsexample.repository.AuthRepository.Companion.repository

class AuthViewModel(val baseActivity: BaseActivity<ViewBinding>) : ViewModel() {

    val email = ObservableField("")
    val password = ObservableField("")
    val loginSuccess = MutableLiveData<Pair<Boolean, String>>()

    fun login() {

        if(email.get().toString().isNotEmpty() && password.get().toString().isNotEmpty()) {
            repository.loginWithEmail(email.get().toString(), password.get().toString(), loginSuccess)
        }
        else {
            baseActivity.showSnackBar("All Fields are required.")
        }
    }

    fun loginObserver() : MutableLiveData<Pair<Boolean, String>> = loginSuccess
}