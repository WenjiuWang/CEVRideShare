package com.vanjor.cevrideshare

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

import com.google.android.libraries.places.api.Places;

import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.viewModels
import com.google.android.libraries.places.api.net.PlacesClient
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.vanjor.cevrideshare.Utility.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.vanjor.cevrideshare.Utility.PICKUP_MARKER
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.vanjor.cevrideshare.Utility.DROPOFF_MARKER
import com.vanjor.cevrideshare.viewmodel.RideStatusViewModel
import com.google.android.gms.maps.model.Gap

import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.vanjor.cevrideshare.Utility.TYPE_DRIVER
import com.vanjor.cevrideshare.Utility.TYPE_PASSENGER
import com.vanjor.cevrideshare.Utility.VEHICLE_MARKER
import com.vanjor.cevrideshare.fragment.*
import com.vanjor.cevrideshare.model.RideInfo
import com.vanjor.cevrideshare.model.UserProfile
import com.vanjor.cevrideshare.model.VehicleInfo
import com.vanjor.cevrideshare.util.PolylineAnimation
import com.vanjor.cevrideshare.viewmodel.MapsViewModel
import org.json.JSONObject
import java.lang.Exception


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG = "MapsActivity"

    private val mRideStatusViewModel: RideStatusViewModel by viewModels()
    private val mMapsViewModel: MapsViewModel by viewModels()

    private var mHandler = Handler(Looper.getMainLooper())

    // Maps
    private lateinit var mMap: GoogleMap

    private var mIsLocationPermissionGranted = false
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    lateinit var mPlacesClient: PlacesClient

    private lateinit var mFragmentContainer: FragmentContainerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mRideStatusViewModel.appType = Utility.getAppType()
        initMaps()
        loadUserProfile()
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Utility.getSavedUserId() != null) {
            val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putLong(getString(R.string.saved_user_id), Utility.currentUserId!!)
                apply()
            }
        }

        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putInt(getString(R.string.saved_app_mode), Utility.getAppType())
            apply()
        }

    }

    private fun loadUserProfile() {
        Log.d(TAG,"loadUserProfile(), type=" + mRideStatusViewModel.appType)
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val userProfileReqUrl = "http://192.168.0.13:25680/userprofile/" + Utility.currentUserId
        val userProfileRequest = JsonObjectRequest(
            Request.Method.GET, userProfileReqUrl, null,
            { response ->
                Log.d(TAG, response.toString())
                parseUserProfile(response)

                if (mRideStatusViewModel.appType == TYPE_DRIVER) {
                    loadUserVehicleInfoAndStatus()
                } else {
                    initFragment()
                }
            },
            { error ->
                Log.e(TAG, error.toString())
            }
        )
        queue.add(userProfileRequest)
    }

    private fun parseUserProfile(result: JSONObject) {
        val userProfile = UserProfile(
            result.getLong("userId"),
            result.getString("firstName"),
            result.getString("lastName"),
            (result.get("userRating") as Double).toFloat(),
            null,
        )
        if (mRideStatusViewModel.appType == TYPE_DRIVER) {
            mRideStatusViewModel.rideInfo = RideInfo(
                null, userProfile, null, null, null, null,
                null, null, null, null, null)
        } else {
            mRideStatusViewModel.rideInfo = RideInfo(
                userProfile, null, null, null, null, null,
                null, null, null, null, null)
        }
    }

    private fun loadUserVehicleInfoAndStatus() {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val vehicleInfoReqUrl = "http://192.168.0.13:25680/aevinfo/owner/" + Utility.currentUserId
        val vehicleInfoRequest = JsonObjectRequest(
            Request.Method.GET, vehicleInfoReqUrl, null,
            { response ->
                Log.d(TAG, response.toString())
                val vehicleInfo = VehicleInfo(
                    response.getLong("id"),
                    response.getString("vehicleVin"),
                    response.getString("plateNumber"),
                    response.getString("make"),
                    response.getString("model"),
                    response.getInt("numSeats"),
                    response.getString("vehicleColor"),
                    response.getString("vehicleType"),
                    response.getDouble("vehicleRating").toFloat(),
                    )
                mRideStatusViewModel.rideInfo!!.driverProfile!!.vehicleInfo = vehicleInfo

                val vehicleStatusReqUrl = "http://192.168.0.13:25680/aevstatuses/" + mRideStatusViewModel.rideInfo!!.driverProfile!!.vehicleInfo!!.id
                val vehicleStatusRequest = JsonObjectRequest(
                    Request.Method.GET, vehicleStatusReqUrl, null,
                    { response ->
                        Log.d(TAG, response.toString())
                        mRideStatusViewModel.distanceRemaining = Utility.getRemaininDist(response.getString("distanceRemaining"))
                        initFragment()
                    },
                    { error ->
                        Log.e(TAG, error.toString())

                    }
                )
                queue.add(vehicleStatusRequest)
            },
            { error ->
                Log.e(TAG, error.toString())
                mRideStatusViewModel.rideInfo?.driverProfile?.vehicleInfo = null
            }
        )
        queue.add(vehicleInfoRequest)

    }

    private fun initMaps() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // init Places API
        Places.initialize(this, Utility.MAPS_API_KEY);
        mPlacesClient = Places.createClient(this)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

    }

    fun initFragment() {
        //clear models
        if (mRideStatusViewModel.appType == TYPE_DRIVER) {
            mMapsViewModel.clearPickUpRoutes()
            mMapsViewModel.clearDropOffRoutes()
        } else {
            mMapsViewModel.clearAll()
        }
        mRideStatusViewModel.resetRideInfo()
        clearHandler()

        // Root CardView
        mFragmentContainer = findViewById(R.id.fragment_container_view)

        // Choose initial fragment to inflate depends on APP MODE
        when (Utility.getAppType()) {
            Utility.TYPE_DRIVER -> supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<DriverHomeFragment>(R.id.fragment_container_view)
            }
            Utility.TYPE_PASSENGER -> supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<PassengerHomeFragment>(R.id.fragment_container_view)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mMap?.let { map ->
            outState.putParcelable(Utility.KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(Utility.KEY_LOCATION, mMapsViewModel.currentDeviceLocation)
        }
        super.onSaveInstanceState(outState)
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mIsLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        mIsLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mIsLocationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    private fun updateLocationUI() {
        try {
            Log.d(TAG, "updateLocationUI, mIsLocationPermissionGranted=" + mIsLocationPermissionGranted)
            if (mIsLocationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            } else {
                mMap?.isMyLocationEnabled = false
                mMap?.uiSettings?.isMyLocationButtonEnabled = false
                mMapsViewModel.currentDeviceLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getDeviceLocation() {
        try {
            if (mIsLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Current location is ${task.result}")
                        mMapsViewModel.currentDeviceLocation = task.result

                        if (mRideStatusViewModel.appType == TYPE_DRIVER) {
                            mMapsViewModel.currentVehicleLocation = mMapsViewModel.currentDeviceLocation
                            // draw marker if not drawn yet
                            if (!mMapsViewModel.markerMap.containsKey(VEHICLE_MARKER)) drawVehicleMarker()
                        }

                        if (mRideStatusViewModel.rideStatus.get() == mRideStatusViewModel.STATUS_MATCHING) {
                            setDefaultCamera()
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(Utility.DEFAULT_PICKUP, Utility.DEFAULT_ZOOM.toFloat()))
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun setDefaultCamera() {
        if (mMapsViewModel.currentDeviceLocation != null) {
            moveCamera(LatLng(mMapsViewModel.currentDeviceLocation!!.latitude, mMapsViewModel.currentDeviceLocation!!.longitude))
            animateCamera(LatLng(mMapsViewModel.currentDeviceLocation!!.latitude, mMapsViewModel.currentDeviceLocation!!.longitude))
        }
    }

    private fun moveCamera(latLng: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    fun animateCamera(latLng: LatLng) {
        val cameraPosition = CameraPosition.Builder().target(latLng).zoom(15.5f).build()
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }


    fun updateMarkers(latLng: LatLng, type: Int, drawRoute: Boolean) {
        Log.d(TAG, "updateMarkers, latlng=$latLng ,type=$type, draw=$drawRoute")
        if (mMapsViewModel.markerMap.contains(type)) {
            mMapsViewModel.markerMap[type]!!.position = latLng;
        } else {
            val newMaker = MarkerOptions()
                .position(latLng)
                .flat(true)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmap(type)))
                .draggable(type == PICKUP_MARKER)
                .anchor(0.5f, 0.5f)
            mMapsViewModel.markerMap[type] = mMap.addMarker(newMaker)!!
        }
        // Draw trip route if feasible
        if (mMapsViewModel.markerMap.contains(PICKUP_MARKER) &&
            mMapsViewModel.markerMap.contains(DROPOFF_MARKER) && drawRoute) {
            getRoute(mMapsViewModel.markerMap[PICKUP_MARKER]!!.position, mMapsViewModel.markerMap[DROPOFF_MARKER]!!.position, Utility.TYPE_PASSENGER)
        }
        if (mMapsViewModel.markerMap.contains(PICKUP_MARKER) &&
            mMapsViewModel.markerMap.contains(VEHICLE_MARKER) && drawRoute) {
            getRoute(mMapsViewModel.markerMap[VEHICLE_MARKER]!!.position, mMapsViewModel.markerMap[PICKUP_MARKER]!!.position, Utility.TYPE_DRIVER)
        }
    }

    fun getMarkerBitmap(type: Int): Bitmap {

        val height = 30
        val width = 30
        val bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.argb(255,255,255,255))
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        when(type) {
            DROPOFF_MARKER -> canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)
            PICKUP_MARKER -> canvas.drawOval(0F, 0F, 30f, 30f, paint)
            VEHICLE_MARKER -> return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.ic_car), 50, 100, false)
        }
        paint.color = Color.WHITE
        canvas.drawCircle(15f, 15f, 5f, paint)
        return bitmap
    }


    fun getRoute(fromPosition: LatLng, toPosition: LatLng, type: Int) {
        Log.d(TAG, "getRoute, from=$fromPosition ,to=$toPosition, type=$type")

        val requestUrl = Utility.getDirectionRequestString(fromPosition, toPosition)
        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, requestUrl, null,
            { response ->
                Log.d(TAG, response.toString())
                val legs = response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                val vehiclePolylineOptions = getVehicleRouteOptions(type)

                for (i in 0 until legs.length()) {
                    val steps = legs.getJSONObject(i).getJSONArray("steps");
                    for (j in 0 until steps.length()) {
                        val step = steps.getJSONObject(j)
                        val points = step.getJSONObject("polyline").getString("points")
                        val decodedPoints = PolyUtil.decode(points)

                        for (point in decodedPoints) {
                            vehiclePolylineOptions.add(point)
                        }
                    }
                }

                mMapsViewModel.clearAllRouteByType(type)

                if (type == TYPE_DRIVER) {
                    // Update Vehicle & PickUp Markers according to route
                    mMapsViewModel.markerMap[VEHICLE_MARKER]!!.position = vehiclePolylineOptions.points.first()
                    mMapsViewModel.markerMap[PICKUP_MARKER]!!.position = vehiclePolylineOptions.points.last()

                    mRideStatusViewModel.rideInfo?.pickUpLatLng = vehiclePolylineOptions.points.last()

                    // Line for vehicle pickup route
                    mMapsViewModel.pickUpVehiclePolyline = mMap.addPolyline(vehiclePolylineOptions)

                } else {
                    // Update PickUp & DropOff Markers according to route
                    mMapsViewModel.markerMap[PICKUP_MARKER]!!.position = vehiclePolylineOptions.points.first()
                    mMapsViewModel.markerMap[DROPOFF_MARKER]!!.position = vehiclePolylineOptions.points.last()

                    mRideStatusViewModel.rideInfo?.pickUpLatLng = vehiclePolylineOptions.points.first()
                    mRideStatusViewModel.rideInfo?.dropOffLatLng = vehiclePolylineOptions.points.last()

                    if (mRideStatusViewModel.appType == Utility.TYPE_PASSENGER) {
                        // Line for walk to pickup
                        val pickupWalkPolylineOptions = getWalkRouteOptions()
                        pickupWalkPolylineOptions.add(LatLng(mMapsViewModel.currentDeviceLocation!!.latitude, mMapsViewModel.currentDeviceLocation!!.longitude))
                        pickupWalkPolylineOptions.add(vehiclePolylineOptions.points.first())
                        mMapsViewModel.pickUpWalkPolyLine = mMap.addPolyline(pickupWalkPolylineOptions)
                    }

                    // Line for vehicle dropoff
                    mMapsViewModel.dropOffVehiclePolyline = mMap.addPolyline(vehiclePolylineOptions)

                    mMapsViewModel.animatedDropOffPolyline = PolylineAnimation(
                        mMap,
                        mMapsViewModel.dropOffVehiclePolyline.points,
                        polylineOptions = getVehicleAnimationOptions())

                    mMapsViewModel.animatedDropOffPolyline.start()
                }
                animateCameraAllMarkers()
            },
            { error ->
                Log.e(TAG, error.toString())
            }
        )

        queue.add(jsonObjectRequest)

    }

    fun getVehicleRouteOptions(type: Int): PolylineOptions {
        val polylineOptions = PolylineOptions()
        polylineOptions.width(10f)
        polylineOptions.geodesic(true)
        polylineOptions.color(Color.BLACK)
        if (type == Utility.TYPE_PASSENGER) {
            polylineOptions.endCap(SquareCap())
            polylineOptions.startCap(RoundCap())
        } else {
            val pattern = listOf(Dash(30F), Gap(20F))
            polylineOptions.pattern(pattern)
        }
        return polylineOptions
    }

    fun getVehicleAnimationOptions(): PolylineOptions {
        return PolylineOptions()
            .width(16f)
            .color(ContextCompat.getColor(this, R.color.light_gray))

    }

    fun getWalkRouteOptions(): PolylineOptions {
        val polylineOptions = PolylineOptions()

        val pattern = listOf(
            Dot(), Gap(20F), Dash(30F), Gap(20F)
        )
        polylineOptions.pattern(pattern)
        polylineOptions.width(10f)
        polylineOptions.geodesic(true)
        polylineOptions.color(Color.BLACK)
        return polylineOptions
    }

    fun getWalkAnimationOptions(): PolylineOptions {
        val polylineOptions = PolylineOptions()
        val pattern = listOf(
            Dot(), Gap(20F), Dash(30F), Gap(20F)
        )
        polylineOptions.pattern(pattern)
        polylineOptions.width(10f)
        polylineOptions.color(R.color.light_gray)
        return polylineOptions
    }

    fun animateCameraAllMarkers() {
        val builder = LatLngBounds.Builder()
        for (marker in mMapsViewModel.markerMap.values) {
            builder.include(marker.position)
        }
        val bounds = builder.build()
        val padding = 200

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
    }

    fun animateCameraPickUp() {
        val builder = LatLngBounds.Builder()

        if (mMapsViewModel.markerMap.contains(VEHICLE_MARKER)) {
            builder.include(mMapsViewModel.markerMap[VEHICLE_MARKER]!!.position)
        }

        if (mMapsViewModel.markerMap.contains(PICKUP_MARKER)) {
            builder.include(mMapsViewModel.markerMap[PICKUP_MARKER]!!.position)
        }

        val bounds = builder.build()
        val padding = 200

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
    }

    fun launchRideSearchFragment(view: View) {
        mRideStatusViewModel.resetRideInfo()
        clearHandler()
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in_to_top,
                R.anim.slide_out_to_bottom
            )
            setReorderingAllowed(true)
            replace<RideSearchFragment>(R.id.fragment_container_view)
        }
        supportFragmentManager.popBackStack()
    }

    fun launchPassengerHomeFragment(view: View) {
        mRideStatusViewModel.resetAll()
        mMapsViewModel.clearAll()
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in_to_top,
                R.anim.slide_out_to_bottom
            )
            setReorderingAllowed(true)
            replace<PassengerHomeFragment>(R.id.fragment_container_view)
        }
    }

    fun launchRideMatchFragment() {
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in_to_top,
                R.anim.slide_out_to_bottom
            )
            setReorderingAllowed(true)
            replace<RideMatchFragment>(R.id.fragment_container_view)
        }
    }

    fun launchRideBrowseFragment() {
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in_to_top,
                R.anim.slide_out_to_bottom
            )
            setReorderingAllowed(true)
            replace<RideBrowseFragment>(R.id.fragment_container_view)
        }
    }

    fun getRotation(start: LatLng, end: LatLng): Float {
        val latDifference: Double = Math.abs(start.latitude - end.latitude)
        val lngDifference: Double = Math.abs(start.longitude - end.longitude)
        if (latDifference < 0.00001 && lngDifference < 0.00001) {
            return Float.NaN
        }
        var rotation = -1F
        when {
            start.latitude < end.latitude && start.longitude < end.longitude -> {
                rotation = Math.toDegrees(Math.atan(lngDifference / latDifference)).toFloat()
            }
            start.latitude < end.latitude && start.longitude >= end.longitude -> {
                rotation =
                    (360 - Math.toDegrees(Math.atan(lngDifference / latDifference))).toFloat()
            }
            start.latitude >= end.latitude && start.longitude >= end.longitude -> {
                rotation = (180 + Math.toDegrees(Math.atan(lngDifference / latDifference))).toFloat()
            }
            start.latitude >= end.latitude && start.longitude < end.longitude -> {
                rotation = (180 - Math.toDegrees(Math.atan(lngDifference / latDifference))).toFloat()
            }
        }
        return rotation
    }


    fun carAnimator(): ValueAnimator {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 3000
        valueAnimator.interpolator = LinearInterpolator()
        return valueAnimator
    }

    fun checkAndUpdateRideStatus() {
        var requestMode: String? = null
        if (mMapsViewModel.currentVehicleLocation != null && mRideStatusViewModel.rideInfo!!.pickUpLatLng != null) {
            if (Utility.getDistanceBetween(LatLng(mMapsViewModel.currentVehicleLocation!!.latitude, mMapsViewModel.currentVehicleLocation!!.longitude),
                        mRideStatusViewModel.rideInfo!!.pickUpLatLng!!) <= 10) {
                mMapsViewModel.clearPickUpRoutes()
                requestMode = "TRIP_TODROPOFF"
                mHandler.postDelayed({
                    mRideStatusViewModel.rideStatus.set(mRideStatusViewModel.STATUS_ONGOING)
                }, 3000)
            }
        }

        if (mMapsViewModel.currentVehicleLocation != null && mRideStatusViewModel.rideInfo!!.dropOffLatLng != null) {
            Log.d(TAG, "dropOffLatLng=" + mRideStatusViewModel.rideInfo!!.dropOffLatLng)
            if (Utility.getDistanceBetween(LatLng(mMapsViewModel.currentVehicleLocation!!.latitude, mMapsViewModel.currentVehicleLocation!!.longitude),
                    mRideStatusViewModel.rideInfo!!.dropOffLatLng!!) <= 10) {
                requestMode = "DISABLED"
                mHandler.postDelayed({
                    mRideStatusViewModel.rideStatus.set(mRideStatusViewModel.STATUS_COMPLETE)
                }, 3000)
            }
        }

        if (mRideStatusViewModel.rideStatus.get() == mRideStatusViewModel.STATUS_PICKUP) {
            requestMode = "TRIP_TOPICKUP"
        }

        if (requestMode != null) {
            Log.d(TAG, "new modetype = $requestMode")

            val queue = Volley.newRequestQueue(this)
            val vehicleStatusReqUrl = "http://192.168.0.13:25680/aevstatuses/" + mRideStatusViewModel.rideInfo!!.driverProfile!!.vehicleInfo!!.id + "/updateModeType/" +requestMode

            val vehicleInfoRequest = JsonObjectRequest(
                Request.Method.POST, vehicleStatusReqUrl, null,
                { response ->
                    Log.d(TAG, response.toString())

                },
                { error ->
                    Log.e(TAG, error.toString())
                }
            )
            queue.add(vehicleInfoRequest)
        }
    }


    fun getVehicleLocation() {
        val queue = Volley.newRequestQueue(this)
        if (mRideStatusViewModel.appType == TYPE_PASSENGER) {
            val vehicleStatusReqUrl = "http://192.168.0.13:25680/aevstatuses/" + mRideStatusViewModel.rideInfo?.driverProfile?.vehicleInfo?.id
            val vehicleInfoRequest = JsonObjectRequest(
                Request.Method.GET, vehicleStatusReqUrl, null,
                { response ->
                    Log.d(TAG, response.toString())
                    val location = response.getJSONObject("location")
                    if (mMapsViewModel.currentVehicleLocation == null) {
                        mMapsViewModel.currentVehicleLocation = Location(mMapsViewModel.currentDeviceLocation)
                    }

                    mMapsViewModel.currentVehicleLocation!!.latitude = location.getDouble("lat")
                    mMapsViewModel.currentVehicleLocation!!.longitude = location.getDouble("lng")
                    drawVehicleMarker()
                    checkAndUpdateRideStatus()
                    animateVehicleMarker()

                },
                { error ->
                    Log.e(TAG, error.toString())
                    animateVehicleMarker()
                }
            )
            queue.add(vehicleInfoRequest)

        } else {
            getDeviceLocation()
            val queue = Volley.newRequestQueue(this)
            Log.d(TAG, "update loc to server, " + mMapsViewModel.currentDeviceLocation!!.longitude + ", " + mMapsViewModel.currentDeviceLocation!!.latitude)
            // upload location to server
            val vehicleStatusReqUrl = "http://192.168.0.13:25680/aevstatuses/" + mRideStatusViewModel.rideInfo!!.driverProfile!!.vehicleInfo!!.id + "/updateLoc"
            val locationJson = JSONObject()
            locationJson.put("lng", mMapsViewModel.currentDeviceLocation!!.longitude)
            locationJson.put("lat", mMapsViewModel.currentDeviceLocation!!.latitude)

            val vehicleInfoRequest = JsonObjectRequest(
                Request.Method.POST, vehicleStatusReqUrl, locationJson,
                { response ->
                    Log.d(TAG, response.toString())
                    animateVehicleMarker()
                    checkAndUpdateRideStatus()
                }
            ) { error ->
                Log.e(TAG, error.toString())
                animateVehicleMarker()
                checkAndUpdateRideStatus()
            }
            queue.add(vehicleInfoRequest)
        }
    }

    fun drawVehicleMarker() {
        if (mMapsViewModel.currentVehicleLocation != null && mMapsViewModel.previousVehicleLocation == null) {
            mMapsViewModel.previousVehicleLocation = Location(mMapsViewModel.currentVehicleLocation)
            mMapsViewModel.markerMap[VEHICLE_MARKER]?.position = LatLng(mMapsViewModel.currentVehicleLocation!!.latitude, mMapsViewModel.currentVehicleLocation!!.longitude)
            updateMarkers(LatLng(mMapsViewModel.currentVehicleLocation!!.latitude,
                mMapsViewModel.currentVehicleLocation!!.longitude), VEHICLE_MARKER, true)

            if (Utility.getAppType() != TYPE_DRIVER) moveCamera(mMapsViewModel.markerMap[VEHICLE_MARKER]?.position!!)
        }
    }

    fun animateVehicleMarker() {
        val valueAnimator = carAnimator()

        valueAnimator.addUpdateListener { va ->
            if (mMapsViewModel.currentVehicleLocation != null && mMapsViewModel.previousVehicleLocation != null) {
                try{
                    val multiplier = va.animatedFraction
                    val prevLocation = LatLng(mMapsViewModel.currentVehicleLocation!!.latitude, mMapsViewModel.currentVehicleLocation!!.longitude)
                    val newLocation = LatLng(
                        multiplier * mMapsViewModel.currentVehicleLocation!!.latitude + (1 - multiplier) * mMapsViewModel.previousVehicleLocation!!.latitude,
                        multiplier * mMapsViewModel.currentVehicleLocation!!.longitude + (1 - multiplier) * mMapsViewModel.previousVehicleLocation!!.longitude
                    )
                    mMapsViewModel.markerMap[VEHICLE_MARKER]?.position = newLocation
                    val rotation = getRotation(prevLocation, newLocation)
                    if (!rotation.isNaN()) {
                        mMapsViewModel.markerMap[VEHICLE_MARKER]?.rotation = rotation
                    }
                    animateCamera(mMapsViewModel.markerMap[VEHICLE_MARKER]?.position!!)
                } catch (e: Exception) {}
            }
        }
        valueAnimator.start()

        mHandler.postDelayed({ ->
            if (mRideStatusViewModel.rideStatus.get() != mRideStatusViewModel.STATUS_COMPLETE) {
                mMapsViewModel.previousVehicleLocation!!.longitude = mMapsViewModel.currentVehicleLocation!!.longitude
                mMapsViewModel.previousVehicleLocation!!.latitude = mMapsViewModel.currentVehicleLocation!!.latitude
                getVehicleLocation()
            }
        }, 3000)
    }

    fun clearHandler() {
        mHandler.removeCallbacksAndMessages(null)
    }

}