package com.vanjor.cevrideshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.vanjor.cevrideshare.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.driverRegisterButton.setOnClickListener { launchDriver() }
        mBinding.passengerRegisterButton.setOnClickListener { launchPassenger() }
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    fun launchDriver() {
        Utility.setAppType(Utility.TYPE_DRIVER)
        Utility.currentUserId = 1
        launchMapsActivity()
    }

    fun launchPassenger() {
        Utility.setAppType(Utility.TYPE_PASSENGER)
        Utility.currentUserId = 0
        launchMapsActivity()
    }

    fun launchMapsActivity() {
        val intent = Intent()
        intent.setClass(this, MapsActivity::class.java)
        startActivity(intent)
    }

    fun launchLoginActivity(v: View) {
        val intent = Intent()
        intent.setClass(this, LoginActivity::class.java)
        startActivity(intent)
    }
}