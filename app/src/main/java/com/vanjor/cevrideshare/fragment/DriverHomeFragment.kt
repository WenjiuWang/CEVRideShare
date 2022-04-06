package com.vanjor.cevrideshare.fragment

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.vanjor.cevrideshare.MapsActivity
import com.vanjor.cevrideshare.databinding.FragmentDriverHomeBinding
import com.vanjor.cevrideshare.viewmodel.RideStatusViewModel

class DriverHomeFragment : Fragment() {

    private lateinit var mBinding: FragmentDriverHomeBinding

    private val mRideStatusViewModel: RideStatusViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentDriverHomeBinding.inflate(inflater, container, false)
        mBinding.apply { viewmodel = mRideStatusViewModel}
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.tripRangeSlider.setLabelFormatter { value: Float ->
            return@setLabelFormatter "${value.toInt()} km"
        }

        mBinding.minRatingSlider.setLabelFormatter { value: Float ->
            return@setLabelFormatter "${value.toInt()} Star"
        }

        mBinding.startSharingButton.setOnClickListener {
            val builder = AlertDialog.Builder(this.context)
            builder
                .setTitle("Ride Sharing Confirmation")
                .setMessage("\nTrip Range: ${mBinding.tripRangeSlider.values[0]}KM - ${mBinding.tripRangeSlider.values[1]}KM\n\nMinimum Rating: ${mBinding.minRatingSlider.value}")
                .setPositiveButton("Yes") { dialog, id ->

                    val queue = Volley.newRequestQueue(activity)
                    val vehicleStatusReqUrl = "http://192.168.0.13:25680/aevstatuses/" + mRideStatusViewModel.rideInfo!!.driverProfile!!.vehicleInfo!!.id + "/updateModeType/IDLE"

                    val vehicleInfoRequest = JsonObjectRequest(
                        Request.Method.POST, vehicleStatusReqUrl, null,
                        { response ->
                            (activity as MapsActivity).launchRideMatchFragment()
                        },
                        { error ->
                            dialog.dismiss()
                        }
                    )
                    queue.add(vehicleInfoRequest)

                }
                .setNegativeButton("No") { dialog, id ->
                        dialog.dismiss()
                }
            builder.show()
        }
    }

}