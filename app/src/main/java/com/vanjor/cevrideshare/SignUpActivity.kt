package com.vanjor.cevrideshare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.vanjor.cevrideshare.fragment.UserCredentialFormFragment
import com.vanjor.cevrideshare.fragment.UserProfileFormFragment
import com.vanjor.cevrideshare.fragment.VehicleInfoFormFragment
import com.vanjor.cevrideshare.viewmodel.SignUpViewModel

private const val NUM_PAGES = 3
private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f

class SignUpActivity : FragmentActivity() {
    private val TAG = "SignUpActivity"
    private val mSignUpViewModel: SignUpViewModel by viewModels()

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        viewPager = findViewById(R.id.pager)

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.isUserInputEnabled = false
        viewPager.setPageTransformer(ZoomOutPageTransformer())
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            prevPager()
        }
    }

    fun prevPager() {
        val currPos: Int = viewPager.currentItem
        Log.d(TAG, currPos.toString() + ", " + mSignUpViewModel.needVehicle )
        if (currPos == 2 && !mSignUpViewModel.needVehicle) {
            viewPager.currentItem = currPos - 2
        } else {
            viewPager.currentItem = currPos - 1
        }
    }

    fun nextPager() {
        val currPos: Int = viewPager.currentItem
        Log.d(TAG, currPos.toString() + ", " + mSignUpViewModel.needVehicle )

        if (currPos == 0 && !mSignUpViewModel.needVehicle) {
            viewPager.currentItem = currPos + 2
        } else if ((currPos + 1) <= viewPager.adapter!!.itemCount) {
            viewPager.currentItem = currPos + 1
        }
    }

    fun launchMapsActivity() {
        val intent = Intent()
        intent.setClass(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            Log.d(TAG, "createFragment: pos= $position")
            return when(position) {
                0 -> UserProfileFormFragment()
                1 -> VehicleInfoFormFragment()
                else -> {
                    UserCredentialFormFragment()
                }
            }
        }
    }

    class ZoomOutPageTransformer : ViewPager2.PageTransformer {

        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                val pageHeight = height
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = 0f
                    }
                    position <= 1 -> { // [-1,1]
                        // Modify the default slide transition to shrink the page as well
                        val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
                        val vertMargin = pageHeight * (1 - scaleFactor) / 2
                        val horzMargin = pageWidth * (1 - scaleFactor) / 2
                        translationX = if (position < 0) {
                            horzMargin - vertMargin / 2
                        } else {
                            horzMargin + vertMargin / 2
                        }

                        // Scale the page down (between MIN_SCALE and 1)
                        scaleX = scaleFactor
                        scaleY = scaleFactor

                        // Fade the page relative to its size.
                        alpha = (MIN_ALPHA +
                                (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = 0f
                    }
                }
            }
        }
    }

}