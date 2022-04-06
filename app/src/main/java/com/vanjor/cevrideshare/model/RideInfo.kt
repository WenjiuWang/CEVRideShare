package com.vanjor.cevrideshare.model

import com.google.android.gms.maps.model.LatLng
import com.vanjor.cevrideshare.Utility
import java.util.*

data class RideInfo (
    var passengerProfile: UserProfile?,
    var driverProfile: UserProfile?,
    var cost: String?,
    var pickUpLocDesc: String?,
    var pickUpLatLng: LatLng?,
    var pickUpTime: Date?,
    var pickUpDistance: String?,
    var dropOffLocDesc: String?,
    var dropOffLatLng: LatLng?,
    var dropOffTime: Date?,
    var dropOffDistance: String?
    ) {

    fun getRemainingPickUpDist(): String {
        if (pickUpTime == null) return ""
        return Utility.getRemaininDist(pickUpDistance!!)
    }

    fun getRemainingDropOffDist(): String {
        if (dropOffTime == null) return ""
        return Utility.getRemaininDist(dropOffDistance!!)
    }

    fun getRemainingPickUpTime(): String {
        if (pickUpTime == null) return ""
        return Utility.getRemainingTime(pickUpTime!!)
    }

    fun getRemainingDropOffTime(): String {
        if (dropOffTime == null) return ""
        return Utility.getRemainingTime(dropOffTime!!)
    }

    fun reset() {
        when (Utility.getAppType()) {
            Utility.TYPE_PASSENGER -> {
                driverProfile = null
            }
            Utility.TYPE_DRIVER -> {
                passengerProfile = null
            }
        }
    }

    fun getShortLocationDesc(s :String): String {
        return s.split(",")[0]
    }

    fun getShortPickupDesc(): String {
        if (pickUpLocDesc == null) return ""
        return getShortLocationDesc(pickUpLocDesc!!)
    }

    fun getShortDropOffDesc(): String {
        if (dropOffLocDesc == null) return ""
        return getShortLocationDesc(dropOffLocDesc!!)
    }
}