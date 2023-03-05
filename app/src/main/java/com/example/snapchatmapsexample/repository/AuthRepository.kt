package com.example.snapchatmapsexample.repository

import androidx.lifecycle.MutableLiveData
import androidx.viewbinding.ViewBinding
import com.example.snapchatmapsexample.activities.MainActivity

class AuthRepository {

    constructor()

    lateinit var baseContainer : MainActivity<ViewBinding>
    private var loginSuccess : MutableLiveData<Pair<Boolean, String>> = MutableLiveData()

   fun initContext(mainActivity: MainActivity<ViewBinding>) {
       this.baseContainer = mainActivity
   }

    companion object {
        private const val TAG = "ProductRepository"
        val repository = AuthRepository()
    }

    fun loginWithEmail(email: String, password: String, loginSuccess: MutableLiveData<Pair<Boolean, String>>) {
       baseContainer.showLoader()
        this.loginSuccess = loginSuccess
        baseContainer.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                baseContainer.hideLoader()
                when(task.isSuccessful) {
                    true -> {
                        loginSuccess.value = Pair(true, "Success")
                    }
                    false -> {
                        loginSuccess.value = Pair(false, task.exception?.message.toString())
                    }
                }
            }
    }

}