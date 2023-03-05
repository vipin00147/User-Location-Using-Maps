package com.example.snapchatmapsexample.base

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.io.*
import java.util.*


abstract class BaseFragment<V : ViewBinding> : Fragment() {

    protected var binding: V? = null
    lateinit var baseActivity : BaseActivity<ViewBinding>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        if(binding?.root == null) {
            binding = onCreateBinding(inflater, container, savedInstanceState)
        }

        return binding?.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            baseActivity = context as BaseActivity<ViewBinding>

        } catch (ex: Exception) {
        }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    abstract fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): V

    @JvmName("getBaseActivity1")
    fun getBaseActivity() : BaseActivity<ViewBinding> {
        return baseActivity
    }

    fun View.hideView() {
        this.visibility = View.GONE
    }

    fun View.showView() {
        this.visibility = View.VISIBLE
    }

}