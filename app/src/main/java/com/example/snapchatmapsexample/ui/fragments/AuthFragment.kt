package com.example.snapchatmapsexample.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.snapchatmapsexample.R
import com.example.snapchatmapsexample.databinding.FragmentAuthBinding
import com.example.snapchatmapsexample.utils.changeFragment
import com.example.snapchatmapsexample.viewModel.AuthViewModel
import com.example.snapchatmapsexample.viewModel.ViewModelFactory

class AuthFragment : LocationUpdateUtilityFragment<FragmentAuthBinding>() {

    private var viewModel : AuthViewModel ?= null

    override fun updatedLatLng(lat: Double, lng: Double) {
        getBaseActivity().hideLoader()
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentAuthBinding {
        return FragmentAuthBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()
        viewModelObserver()
        getLiveLocation(getBaseActivity())
    }

    private fun viewModelObserver() {

        viewModel?.loginObserver()?.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                when(it.first) {
                    true -> {
                        //getBaseActivity().startIntentActivity<MapsActivity<ViewBinding>>(true)
                        HomeFragment().changeFragment(R.id.mainContainer,getBaseActivity(), false)
                    }
                    false -> {
                        getBaseActivity().showSnackBar("${it.second}")
                    }
                }
                viewModel?.loginSuccess?.value = null
            }
        })

    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, ViewModelFactory(getBaseActivity()))[AuthViewModel::class.java]
        binding?.model = viewModel
    }
}