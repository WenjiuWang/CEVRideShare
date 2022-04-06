package com.vanjor.cevrideshare.fragment

import android.animation.Animator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.vanjor.cevrideshare.R
import com.vanjor.cevrideshare.Utility
import com.vanjor.cevrideshare.adapter.RideRecyclerAdapter
import com.vanjor.cevrideshare.databinding.FragmentRideBrowseBinding
import com.vanjor.cevrideshare.model.RideInfo
import com.vanjor.cevrideshare.model.UserProfile
import com.vanjor.cevrideshare.model.VehicleInfo
import com.vanjor.cevrideshare.util.CustomJsonRequest
import com.vanjor.cevrideshare.viewmodel.RideStatusViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class RideBrowseFragment: Fragment(), RideRecyclerAdapter.ClickListener {
    private val TAG = "RideBrowseFragment"

    private lateinit var mBinding: FragmentRideBrowseBinding
    private val mRideStatusViewModel: RideStatusViewModel by activityViewModels()

    private lateinit var mRideRecyclerAdapter: RideRecyclerAdapter
    private var mHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentRideBrowseBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mRideRecyclerAdapter = RideRecyclerAdapter(this.requireContext(), mRideStatusViewModel.preference)
        mRideRecyclerAdapter.setClickListener(this)
        
        mBinding.ridesRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        mBinding.ridesRecyclerView.adapter = mRideRecyclerAdapter

        mBinding.loadingAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                if (mRideRecyclerAdapter.itemCount == 0) {
                    mBinding.loadingAnimationView.visibility = View.GONE
                    mBinding.loadingText.text = "No available ride found"

                } else {
                    mBinding.progressHolder.visibility = View.GONE
                    mBinding.ridesRecyclerView.visibility = View.VISIBLE
                }
            }
        })

        requestPlanTrip()
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacksAndMessages(null);
    }

    private fun getTripRequestJson(aevId: Long): JSONObject {
        val pickupObj = JSONObject()
        pickupObj.put("lng", mRideStatusViewModel.pickupPlace!!.latLng.longitude)
        pickupObj.put("lat", mRideStatusViewModel.pickupPlace!!.latLng.latitude)

        val destinationObj = JSONObject()
        destinationObj.put("lng", mRideStatusViewModel.dropoffPlace!!.latLng.longitude)
        destinationObj.put("lat", mRideStatusViewModel.dropoffPlace!!.latLng.latitude)

        val tripRequest = JSONObject()
        tripRequest.put("tripType", "PLAN")
        tripRequest.put("passengerId", mRideStatusViewModel.rideInfo!!.passengerProfile!!.userId)
        tripRequest.put("aevId", aevId)
        tripRequest.put("aevType", (if(mRideStatusViewModel.seats <= 4)  "SMALL" else "LARGE"))
        tripRequest.put("pickupLocation", pickupObj)
        tripRequest.put("endLocation", destinationObj)
        tripRequest.put("requestTime", null)
        tripRequest.put("preference", mRideStatusViewModel.preference)

        Log.d(TAG, tripRequest.toString())

        return tripRequest
    }

    private fun requestPlanTrip() {
        // Instantiate the RequestQueue.

        val queue = Volley.newRequestQueue(activity)
        val userProfileReqUrl = "http://192.168.0.13:25680/trip"

        val userProfileRequest = CustomJsonRequest(
            Request.Method.POST, userProfileReqUrl, getTripRequestJson(-1),
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


    fun parseTripResponse(result: JSONArray?) {
        if (result == null) return
        val browseList: ArrayList<RideInfo> = ArrayList()
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
                (driverProfile.get("userRating") as Double).toFloat(),
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
                if (mRideStatusViewModel.dropoffDesc == null) pickUpInfo.getString("formattedAddress") else mRideStatusViewModel.dropoffDesc,
                if (mRideStatusViewModel.dropoffPlace == null) dropoffLatLng else mRideStatusViewModel.dropoffPlace!!.latLng!!,
                Utility.convertDate(response.getString("dropOffTime")),
                response.getString("dropOffDist")
            )

            browseList.add(curRideInfo)
        }

        mRideRecyclerAdapter.setResultList(browseList)

    }


    override fun onClickCallback(rideInfo: RideInfo?) {
        mRideStatusViewModel.rideInfo = rideInfo!!
        mRideStatusViewModel.matchFound = true

        activity?.supportFragmentManager?.commit {
            setCustomAnimations(
                R.anim.slide_in_to_top,
                R.anim.slide_out_to_bottom
            )
            setReorderingAllowed(true)
            replace<RideMatchFragment>(R.id.fragment_container_view)
        }
    }
}