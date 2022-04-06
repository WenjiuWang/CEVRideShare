package com.vanjor.cevrideshare.viewmodel

import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place
import com.vanjor.cevrideshare.Utility
import com.vanjor.cevrideshare.model.RideInfo

class RideStatusViewModel: ViewModel() {
    val TAG = "RideStatusViewModel"
    val STATUS_MATCHING = 0
    val STATUS_MATCH_FOUND = 1
    val STATUS_PENDING = 2
    val STATUS_PICKUP = 3
    val STATUS_ONGOING = 4
    val STATUS_COMPLETE = 5
    val rideStatusDesc = listOf("", "Match Found", "", "On the way to pickup", "Heading to the destination", "How was your trip?")

    var appType = Utility.TYPE_PASSENGER

    // ride search
    var isSwapped: Int = 0

    var pickupDesc: String? = null
    var pickupPlace: Place? = null

    var dropoffDesc: String? = null
    var dropoffPlace: Place? = null

    var seats: Int = -1
    var time: Int = 0
    var preference: Int = Utility.DEFAULT_RECYCLER

    // ride info & process
    var rideInfo: RideInfo? = null
    var matchFound: Boolean = false
    var rideStatus = ObservableInt(STATUS_MATCHING)
    var activeTripId: Long = -1


    // driver side vehicle status
    var distanceRemaining = "?"

    fun resetRideInfo() {
        rideStatus = ObservableInt(STATUS_MATCHING)
        matchFound = false
        rideInfo?.reset()
        isSwapped = 2
        activeTripId = -1
    }

    fun resetAll() {
        resetRideInfo()
        seats = -1
        time = 0
        preference = Utility.DEFAULT_RECYCLER
        pickupDesc = null
        dropoffDesc = null
        distanceRemaining = "?"
    }

    fun getLoadingDesc(): String {
        return when (appType) {
            Utility.TYPE_PASSENGER -> {
                return when (rideStatus.get()) {
                    STATUS_MATCHING -> "Finding Your Ride"
                    else -> "Confirming Your Ride"
                }
            }
            else -> {
                "Matching with a passenger"
            }
        }
    }
}