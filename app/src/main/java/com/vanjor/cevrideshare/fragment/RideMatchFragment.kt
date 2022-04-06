package com.vanjor.cevrideshare.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.airbnb.lottie.LottieDrawable
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.vanjor.cevrideshare.MapsActivity
import com.vanjor.cevrideshare.Utility
import com.vanjor.cevrideshare.Utility.TYPE_DRIVER
import com.vanjor.cevrideshare.databinding.FragmentRideMatchBinding
import com.vanjor.cevrideshare.model.RideInfo
import com.vanjor.cevrideshare.model.UserProfile
import com.vanjor.cevrideshare.model.VehicleInfo
import com.vanjor.cevrideshare.util.CustomJsonRequest
import com.vanjor.cevrideshare.viewmodel.RideStatusViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class RideMatchFragment: Fragment() {
    private val TAG = "RideMatchFragment"

    private lateinit var mBinding: FragmentRideMatchBinding
    private val mRideStatusViewModel: RideStatusViewModel by activityViewModels()

    private var mHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentRideMatchBinding.inflate(inflater, container, false)
        mBinding.apply { viewmodel = mRideStatusViewModel}
        return mBinding.root
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacksAndMessages(null);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.confirmButton.setOnClickListener { bookTripWithCurrentDriver() }

        mBinding.backButton.setOnClickListener { onRideTerminated(false) }

        mBinding.skipButton.setOnClickListener { onRideTerminated(false) }
        mBinding.submitButton.setOnClickListener { onRideTerminated(true) }

        if (mRideStatusViewModel.appType == TYPE_DRIVER) {
            requestPassengers()
            mBinding.loadingAnimationView.repeatCount = LottieDrawable.INFINITE
        } else {
            if (mRideStatusViewModel.matchFound) {
                bookTripWithCurrentDriver()
            } else {
                bookTrip()
                mBinding.loadingAnimationView.repeatCount = LottieDrawable.INFINITE
            }
        }
    }

    fun requestPassengers() {
        val queue = Volley.newRequestQueue(activity)

        val vehicleStatusReqUrl = "http://192.168.0.13:25680/aevstatuses/" + mRideStatusViewModel.rideInfo!!.driverProfile!!.vehicleInfo!!.id
        val vehicleStatusRequest = JsonObjectRequest(
            Request.Method.GET, vehicleStatusReqUrl, null,
            { response ->
                Log.d(TAG, response.toString())
                mRideStatusViewModel.activeTripId = response.getLong("activeTripId")

                if (mRideStatusViewModel.activeTripId < 0) {
                    mHandler.postDelayed({ requestPassengers() }, 3000)
                } else {
                    val vehicleStatusReqUrl = "http://192.168.0.13:25680/trip/" + mRideStatusViewModel.activeTripId
                    val vehicleStatusRequest = JsonObjectRequest(
                        Request.Method.GET, vehicleStatusReqUrl, null,
                        { response ->
                            Log.d(TAG, response.toString())
                            parseTripResponse(response)
                        },
                        { error ->
                            Log.e(TAG, error.toString())
                            mHandler.postDelayed({ requestPassengers() }, 3000)
                        }
                    )
                    queue.add(vehicleStatusRequest)
                }
            },
            { error ->
                Log.e(TAG, error.toString())
                mHandler.postDelayed({ requestPassengers() }, 3000)
            }
        )
        queue.add(vehicleStatusRequest)
    }

    fun bookTripWithCurrentDriver() {
        mRideStatusViewModel.rideStatus.set(mRideStatusViewModel.STATUS_PENDING)

        val pickupObj = JSONObject()
        pickupObj.put("lng", mRideStatusViewModel.pickupPlace!!.latLng.longitude)
        pickupObj.put("lat", mRideStatusViewModel.pickupPlace!!.latLng.latitude)

        val destinationObj = JSONObject()
        destinationObj.put("lng", mRideStatusViewModel.dropoffPlace!!.latLng.longitude)
        destinationObj.put("lat", mRideStatusViewModel.dropoffPlace!!.latLng.latitude)

        val tripRequest = JSONObject()
        tripRequest.put("tripType", "BOOK")
        tripRequest.put("passengerId", mRideStatusViewModel.rideInfo!!.passengerProfile!!.userId)
        tripRequest.put("aevId", mRideStatusViewModel.rideInfo!!.driverProfile!!.vehicleInfo!!.id)
        tripRequest.put("aevType", (if(mRideStatusViewModel.seats < 4)  "SMALL" else "LARGE"))
        tripRequest.put("pickupLocation", pickupObj)
        tripRequest.put("endLocation", destinationObj)
        tripRequest.put("requestTime", null)
        tripRequest.put("preference", mRideStatusViewModel.preference)


        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(activity)
        val userProfileReqUrl = "http://192.168.0.13:25680/trip"

        val userProfileRequest = CustomJsonRequest(
            Request.Method.POST, userProfileReqUrl, tripRequest,
            { response ->
                Log.d(TAG, response.toString())
                parseTripResponse(response)
            },
            { error ->
                Log.e(TAG, error.toString())
            }
        )

        queue.add(userProfileRequest)

    }

    fun parseTripResponse(result: JSONObject?) {
        val temp = JSONArray()
        temp.put(result)
        parseTripResponse(temp)
    }

    fun parseTripResponse(result: JSONArray?) {
        if (result == null) return
        for (i in 0 until result.length()) {
            val response = result.getJSONObject(i)
            val aev = response.getJSONObject("aev")
            val driverProfile = response.getJSONObject("driverProfile")
            val passengerProfile = response.getJSONObject("passengerProfile")

            val pickUpInfo = response.getJSONObject("pickUp")
            val pickupLatLng = LatLng(pickUpInfo.getJSONObject("latlng").getDouble("lat"), pickUpInfo.getJSONObject("latlng").getDouble("lng"))
            val dropoffInfo = response.getJSONObject("destination")
            val dropoffLatLng = LatLng(dropoffInfo.getJSONObject("latlng").getDouble("lat"), dropoffInfo.getJSONObject("latlng").getDouble("lng"))

            val curVehicleInfo = VehicleInfo(
                aev.getLong("id"),
                aev.getString("vehicleVin"),
                aev.getString("plateNumber"),
                aev.getString("make"),
                aev.getString("model"),
                aev.getInt("numSeats"),
                aev.getString("vehicleColor"),
                aev.getString("vehicleType"),
                (aev.get("vehicleRating") as Double).toFloat()
            )

            val curPassengerProfile = UserProfile(
                passengerProfile.getLong("userId"),
                passengerProfile.getString("firstName"),
                passengerProfile.getString("lastName"),
                (passengerProfile.get("userRating") as Double).toFloat(),
                null
            )

            val curDriverProfile = UserProfile(
                driverProfile.getLong("userId"),
                driverProfile.getString("firstName"),
                driverProfile.getString("lastName"),
                (driverProfile.get("userRating") as Double).toFloat(),
                curVehicleInfo
            )

            val curRideInfo = RideInfo(
                if (mRideStatusViewModel.rideInfo?.passengerProfile == null) curPassengerProfile else mRideStatusViewModel.rideInfo?.passengerProfile,
                if (mRideStatusViewModel.rideInfo?.driverProfile == null) curDriverProfile else mRideStatusViewModel.rideInfo?.driverProfile,
                response.getString("cost"),
                if (mRideStatusViewModel.pickupDesc == null) pickUpInfo.getString("formattedAddress") else mRideStatusViewModel.pickupDesc,
                if (mRideStatusViewModel.pickupPlace == null) pickupLatLng else mRideStatusViewModel.pickupPlace!!.latLng!!,
                Utility.convertDate(response.getString("pickUpTime")),
                response.getString("pickUpDist"),
                if (mRideStatusViewModel.dropoffDesc == null) dropoffInfo.getString("formattedAddress") else mRideStatusViewModel.dropoffDesc,
                if (mRideStatusViewModel.dropoffPlace == null) dropoffLatLng else mRideStatusViewModel.dropoffPlace!!.latLng!!,
                Utility.convertDate(response.getString("dropOffTime")),
                response.getString("dropOffDist")
            )

            mRideStatusViewModel.rideInfo = curRideInfo
        }

        if (mRideStatusViewModel.appType == TYPE_DRIVER) {
            mRideStatusViewModel.rideStatus.set(mRideStatusViewModel.STATUS_PICKUP)
            (activity as MapsActivity).updateMarkers(mRideStatusViewModel.rideInfo!!.pickUpLatLng!!,
                Utility.PICKUP_MARKER, true)
            (activity as MapsActivity).updateMarkers(mRideStatusViewModel.rideInfo!!.dropOffLatLng!!,
                Utility.DROPOFF_MARKER, true)
            (activity as MapsActivity).getVehicleLocation()
        } else {
            mRideStatusViewModel.rideStatus.set(mRideStatusViewModel.STATUS_PENDING)

            mHandler.postDelayed({
                mRideStatusViewModel.rideStatus.set(mRideStatusViewModel.STATUS_PICKUP)
                (activity as MapsActivity).getVehicleLocation()
            }, 3000)
        }
        mBinding.invalidateAll()
    }

    fun bookTrip() {
        mRideStatusViewModel.rideStatus.set(mRideStatusViewModel.STATUS_MATCHING)

        val pickupObj = JSONObject()
        pickupObj.put("lng", mRideStatusViewModel.pickupPlace!!.latLng.longitude)
        pickupObj.put("lat", mRideStatusViewModel.pickupPlace!!.latLng.latitude)

        val destinationObj = JSONObject()
        destinationObj.put("lng", mRideStatusViewModel.dropoffPlace!!.latLng.longitude)
        destinationObj.put("lat", mRideStatusViewModel.dropoffPlace!!.latLng.latitude)

        val tripRequest = JSONObject()
        tripRequest.put("tripType", "BOOK")
        tripRequest.put("passengerId", mRideStatusViewModel.rideInfo!!.passengerProfile!!.userId)
        tripRequest.put("aevId", -1)
        tripRequest.put("aevType", (if(mRideStatusViewModel.seats < 5)  "SMALL" else "LARGE"))
        tripRequest.put("pickupLocation", pickupObj)
        tripRequest.put("endLocation", destinationObj)
        tripRequest.put("requestTime", null)
        tripRequest.put("preference", mRideStatusViewModel.preference)


        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(activity)
        val userProfileReqUrl = "http://192.168.0.13:25680/trip"

        val userProfileRequest = CustomJsonRequest(
            Request.Method.POST, userProfileReqUrl, tripRequest,
            { response ->
                Log.d(TAG, response.toString())
                if (response == null || response.length() == 0) {
                    mHandler.postDelayed({ requestPassengers() }, 3000)
                } else {
                    parseTripResponse(response)
                }
            },
            { error ->
                Log.e(TAG, error.toString())
                mHandler.postDelayed({ bookTrip() }, 3000)
            }
        )

        queue.add(userProfileRequest)
    }

    fun mockmMatchFound() {
        Log.d(TAG, "mockmMatchFound(), rideStatus=" + mRideStatusViewModel.rideStatus)
        val vehicleInfo_1 = VehicleInfo(4, "SACA1E1R", "CZX5122", "Honda", "Civic 2020", 2, "Silver", "Sedan", 3.46f)
        val driverProfile_1 = UserProfile(4, "May", "Weather", 4.8f, vehicleInfo_1)
        val passengerProfile_1 = UserProfile(5, "Wenjiu", "Wang", 5.0f, null)
        val rideInfo_1 = RideInfo(passengerProfile_1, driverProfile_1,"21.52", "456 King Edward Ave",
            Utility.DEFAULT_PICKUP, Date(System.currentTimeMillis() + 600000), "13.32", "300 Rideau Street",
            Utility.DEFAULT_DROPOFF, Date(System.currentTimeMillis() + 1100000), "25.42")

        mRideStatusViewModel.pickupDesc = "456 King Edward Ave"
        mRideStatusViewModel.dropoffDesc = "300 Rideau Street"
        mRideStatusViewModel.rideInfo = rideInfo_1
        mRideStatusViewModel.rideStatus.set(mRideStatusViewModel.STATUS_MATCH_FOUND)

        (activity as MapsActivity).updateMarkers(
            Utility.DEFAULT_PICKUP,
            Utility.PICKUP_MARKER, false)
        (activity as MapsActivity).updateMarkers(
            Utility.DEFAULT_DROPOFF,
            Utility.DROPOFF_MARKER, true)

        (activity as MapsActivity).animateVehicleMarker()

        mBinding.invalidateAll()

    }

    fun onRideTerminated(ifRate: Boolean) {
        val queue = Volley.newRequestQueue(activity)

        if (ifRate){
            val userId = if (mRideStatusViewModel.appType == TYPE_DRIVER) mRideStatusViewModel.rideInfo!!.passengerProfile!!.userId else mRideStatusViewModel.rideInfo!!.driverProfile!!.userId
            // Instantiate the RequestQueue.
            val userProfileReqUrl = "http://192.168.0.13:25680/userprofile/rate/" + userId + "/" + mBinding.ratingBar.rating.toInt()

            val userProfileRequest = CustomJsonRequest(
                Request.Method.POST, userProfileReqUrl, null,
                { response ->
                    Log.d(TAG, response.toString())
                },
                { error ->
                    Log.e(TAG, error.toString())
                }
            )
            queue.add(userProfileRequest)
        }
        (activity as MapsActivity).initFragment()
    }
}