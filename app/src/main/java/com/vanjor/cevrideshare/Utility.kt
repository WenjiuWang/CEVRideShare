package com.vanjor.cevrideshare

import android.graphics.Typeface
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


object Utility {
    val DEMO_MODE = false

    const val DEFAULT_RECYCLER = 0
    const val PICKUP_RECYCLER = 1
    const val PRICE_RECYCLER = 2
    const val COMFORT_RECYCLER = 3

    const val TYPE_PASSENGER = 0
    const val TYPE_DRIVER = 1
    const val MAPS_API_KEY = BuildConfig.MAPS_API_KEY

    const val CURRENT_MARKER = 0
    const val VEHICLE_MARKER = 1
    const val PICKUP_MARKER = 2
    const val DROPOFF_MARKER = 3

    const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    const val KEY_CAMERA_POSITION = "camera_position"
    const val KEY_LOCATION = "location"
    const val DEFAULT_ZOOM = 15
    const val DIRECTIONS_API_BASE_URL = "https://maps.googleapis.com/maps/api/directions/json?"

    val DEFAULT_PICKUP = LatLng(45.427160, -75.684920)
    val DEFAULT_DROPOFF = LatLng(45.428890, -75.685310)

    var APP_TYPE = TYPE_PASSENGER

    var currentUserId: Long? = null

    fun getSavedUserId(): Long? {
        return currentUserId
    }

    fun getAppType() : Int {
        return APP_TYPE
    }

    fun setAppType(t: Int) {
        APP_TYPE = t
    }

    fun getDirectionRequestString(pickUp: LatLng, dropOff: LatLng): String {
        val result = DIRECTIONS_API_BASE_URL + "origin=" +
                pickUp.latitude + "," + pickUp.longitude + "&destination=" +
                dropOff.latitude + "," + dropOff.longitude + "&key=" + MAPS_API_KEY
        Log.d("Util", result)
        return result

    }

    fun convertDate(s: String): Date {
        var dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        return dateFormat.parse(s)!!
    }

    fun getRemainingTime(date: Date): String {
        val diff =  date.time - System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toMinutes(diff).toString() + "Min"
    }

    fun getCost(cost: String): String {
        return String.format("%.2f", cost.toFloat());
    }

    fun getRemaininDist(dist: String): String {
        var distFloat = dist.toFloat() / 1000
        return String.format("%.2f", distFloat) + "KM"
    }

    // Use Haversine formula
    fun getDistanceBetween(a: LatLng, b: LatLng): Double {
        val R = 6378137.0
        val latDistance = Math.toRadians(b.latitude - a.latitude)
        val lonDistance = Math.toRadians(b.longitude - a.longitude)
        val v = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + (Math.cos(Math.toRadians(a.latitude)) * Math.cos(Math.toRadians(b.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)))
        val c = 2 * Math.atan2(Math.sqrt(v), Math.sqrt(1 - v))
        val d = R * c
        Log.d("Utility", "getDistanceBetween(), d=$d")
        return d
    }

    fun Snackbar.changeFont()
    {
        val tv = view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        val font = Typeface.createFromAsset(context.assets, "gilroy_semibold.ttf")
        tv.typeface = font
    }

    fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

}