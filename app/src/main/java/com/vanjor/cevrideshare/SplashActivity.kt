package com.vanjor.cevrideshare

import android.animation.Animator
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.airbnb.lottie.LottieAnimationView

class SplashActivity : AppCompatActivity() {

    private var handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        loadSharedPreference()

        val splashAnimationView = findViewById<LottieAnimationView>(R.id.animation_view)
        splashAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                launch()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    fun launch() {
        val intent = Intent()

        if (Utility.currentUserId == null) {
            intent.setClass(this, MapsActivity::class.java)
        } else {
            intent.setClass(this, WelcomeActivity::class.java)
        }
        startActivity(intent)
        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun loadSharedPreference() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        Utility.currentUserId = sharedPref.getLong(getString(R.string.saved_user_id), 99)
        Utility.setAppType(sharedPref.getInt(getString(R.string.saved_app_mode), 0))
    }
}
