package com.example.snapchatmapsexample.utils

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

fun Fragment.changeFragment(containerId: Int, requireActivity: FragmentActivity, isAddToBakStack : Boolean) {

    var fragment = this
    val transaction = requireActivity.supportFragmentManager.beginTransaction()
    transaction.let {
        it.replace(containerId, fragment)
        if(isAddToBakStack) {
            it.addToBackStack(this.javaClass.canonicalName)
        }
        it.commit()
    }
}

inline fun <reified T : Activity> Activity.startIntentActivity(setFinish : Boolean) {
    startActivity(Intent(this, T::class.java))
    if(setFinish) {
        finish()
    }
}