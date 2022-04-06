package com.vanjor.cevrideshare.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.vanjor.cevrideshare.Utility
import com.vanjor.cevrideshare.util.PolylineAnimation

class MapsViewModel: ViewModel() {
    // locations
    var currentDeviceLocation: Location? = null

    var currentVehicleLocation: Location? = null
    var previousVehicleLocation: Location? = null

    // places


    // markers
    var markerMap: MutableMap<Int, Marker> = mutableMapOf()

    // polylines

    lateinit var pickUpWalkPolyLine: Polyline
    lateinit var pickUpVehiclePolyline: Polyline

    lateinit var dropOffVehiclePolyline: Polyline
    lateinit var dropOffWalkPolyLine: Polyline
    lateinit var animatedDropOffPolyline: PolylineAnimation

    fun clearAllRouteByType(type: Int) {
        if (type == Utility.TYPE_PASSENGER) {
            if (this::pickUpWalkPolyLine.isInitialized) {
                pickUpWalkPolyLine.remove()
            }
            if (this::dropOffVehiclePolyline.isInitialized) {
                dropOffVehiclePolyline.remove()
            }
            if (this::dropOffWalkPolyLine.isInitialized) {
                dropOffWalkPolyLine.remove()
            }
            if (this::animatedDropOffPolyline.isInitialized) {
                animatedDropOffPolyline.remove()
            }
        } else {
            if (this::pickUpVehiclePolyline.isInitialized) {
                pickUpVehiclePolyline.remove()
            }
        }
    }

    fun clearPickUpRoutes() {
        if (this::pickUpWalkPolyLine.isInitialized) {
            pickUpWalkPolyLine.remove()
        }
        if (this::pickUpVehiclePolyline.isInitialized) {
            pickUpVehiclePolyline.remove()
        }
        if (this::pickUpVehiclePolyline.isInitialized) {
            pickUpVehiclePolyline.remove()
        }
    }

    fun clearDropOffRoutes() {
        if (this::dropOffVehiclePolyline.isInitialized) {
            dropOffVehiclePolyline.remove()
        }
        if (this::dropOffWalkPolyLine.isInitialized) {
            dropOffWalkPolyLine.remove()
        }
        markerMap.forEach { (key, value) ->
            if (key != Utility.VEHICLE_MARKER) {
                value.remove()
            }
        }
    }

    fun clearAll() {
        clearPickUpRoutes()
        clearDropOffRoutes()
        markerMap.forEach { (key, value) ->
            value.remove()
        }
        markerMap.clear()
    }

}