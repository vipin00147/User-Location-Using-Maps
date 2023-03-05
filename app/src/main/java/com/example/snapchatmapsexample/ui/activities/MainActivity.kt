package com.example.snapchatmapsexample.activities

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.example.snapchatmapsexample.R
import com.example.snapchatmapsexample.ui.fragments.AuthFragment
import com.example.snapchatmapsexample.ui.fragments.HomeFragment
import com.example.snapchatmapsexample.base.BaseActivity
import com.example.snapchatmapsexample.databinding.ActivityMainBinding
import com.example.snapchatmapsexample.repository.AuthRepository.Companion.repository
import com.example.snapchatmapsexample.repository.HomeRepository.Companion.homeRepository
import com.example.snapchatmapsexample.utils.changeFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity<T> : BaseActivity<ActivityMainBinding>() {

    override fun createBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLoaderDialogView()
        repository.initContext(this as MainActivity<ViewBinding>)
        homeRepository.initContext(this as MainActivity<ViewBinding>)
        changeTopBarColor(resources.getColor(R.color.white))
        changeStatusBarIconColorToBlack(binding.root)
        auth = Firebase.auth
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null) {
            //startIntentActivity<MapsActivity<ViewBinding>>(true)
            HomeFragment().changeFragment(R.id.mainContainer,this, false)
        }
        else {
            AuthFragment().changeFragment(R.id.mainContainer, this, false)
        }
    }
}