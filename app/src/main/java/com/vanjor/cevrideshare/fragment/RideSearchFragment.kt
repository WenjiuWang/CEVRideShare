package com.vanjor.cevrideshare.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.vanjor.cevrideshare.MapsActivity
import com.vanjor.cevrideshare.Utility
import com.vanjor.cevrideshare.adapter.PlacesRecyclerAdapter
import com.vanjor.cevrideshare.databinding.FragmentRideSearchBinding
import com.vanjor.cevrideshare.viewmodel.RideStatusViewModel

class RideSearchFragment : Fragment(), PlacesRecyclerAdapter.ClickListener {
    private val TAG = "RideSearchFragment"

    private lateinit var mBinding: FragmentRideSearchBinding
    private val mRideStatusViewModel: RideStatusViewModel by activityViewModels()

    private lateinit var mPlacesRecyclerViewAdapter: PlacesRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentRideSearchBinding.inflate(inflater, container, false)
        mBinding.apply { viewmodel = mRideStatusViewModel }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Origin and Destination search listener
        
        mBinding.pickupSearch.addTextChangedListener(filterTextWatcher)
        mBinding.destinationSearch.addTextChangedListener(filterTextWatcher)

        mPlacesRecyclerViewAdapter = PlacesRecyclerAdapter(this.context!!, (activity as MapsActivity).mPlacesClient)
        mPlacesRecyclerViewAdapter.setClickListener(this)

        mBinding.placesRecyclerView.layoutManager = LinearLayoutManager(this.context!!)
        mBinding.placesRecyclerView.adapter = mPlacesRecyclerViewAdapter

        mBinding.swapAddressButton.setOnClickListener {
            onSwapClicked()
        }

        if (mRideStatusViewModel.preference != -1) {
            mBinding.preferenceChipGroup.findViewWithTag<Chip>(mRideStatusViewModel.preference.toString()).isChecked = true
        }

        mBinding.preferenceChipGroup.setOnCheckedChangeListener { group, checkedId ->
            Log.d(TAG, group.findViewById<Chip>(checkedId).tag as String)
            mRideStatusViewModel.preference = group.findViewById<Chip>(checkedId).tag.toString().toInt()
        }

        mPlacesRecyclerViewAdapter.notifyDataSetChanged();

        mBinding.browseButton.setOnClickListener {
            if (validateSearchCondition()) {
                (activity as MapsActivity).launchRideBrowseFragment()
            }
        }

        mBinding.matchButton.setOnClickListener {
            if (validateSearchCondition()) {
                (activity as MapsActivity).launchRideMatchFragment()
            }
        }

        mBinding.seatsRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            mRideStatusViewModel.seats = mBinding.seatsRadioGroup.indexOfChild(mBinding.seatsRadioGroup.findViewById(i)) + 2
        }
    }


    private fun validateSearchCondition(): Boolean {
        if (mBinding.pickupSearch.text == null || mBinding.pickupSearch.text.isEmpty()) {
            Snackbar.make(mBinding.root, "Please choose a pick up point!", Snackbar.LENGTH_SHORT)
                .setAnchorView(mBinding.backButton)
                .show()
            return false
        }
        if (mBinding.destinationSearch.text == null || mBinding.destinationSearch.text.isEmpty()) {
            Snackbar.make(mBinding.root, "Please choose a drop off point!", Snackbar.LENGTH_SHORT)
                .setAnchorView(mBinding.backButton)
                .show()
            return false
        }
        if (mRideStatusViewModel.seats == -1) {
            Snackbar.make(mBinding.root, "Please select required seat number!", Snackbar.LENGTH_SHORT)
                .setAnchorView(mBinding.backButton)
                .show()
            return false
        }
        if (mRideStatusViewModel.time == 0) {
            Snackbar.make(mBinding.root, "Please select required time!", Snackbar.LENGTH_SHORT)
                .setAnchorView(mBinding.backButton)
                .show()
            return false
        }
        return true
    }


    private val filterTextWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            Log.d(TAG, s.toString())
            if (s.toString() != "") {
                if (mRideStatusViewModel.isSwapped == 0) {
                    mPlacesRecyclerViewAdapter.filter.filter(s.toString())
                    if (mBinding.placesRecyclerView.visibility == View.GONE) {
                        mBinding.placesRecyclerView.visibility = View.VISIBLE
                    }
                } else {
                    mRideStatusViewModel.isSwapped -= 1
                }

            } else {
                if (mBinding.placesRecyclerView.visibility == View.VISIBLE) {
                    mBinding.placesRecyclerView.visibility =View.GONE
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    override fun onClickCallback(place: Place?) {
        if (place != null) {
            Toast.makeText(
                this.context,
                place.address + ", " + place.latLng.latitude + place.latLng.longitude,
                Toast.LENGTH_SHORT
            ).show()
        }
        // Complete the text search and update marker

        if (mBinding.pickupSearch.isFocused) {
            mBinding.pickupSearch.setText(place!!.address!!.toString())
            mRideStatusViewModel.pickupPlace = place
            (activity as MapsActivity).updateMarkers(place.latLng!!, Utility.PICKUP_MARKER, true)
        } else {
            mRideStatusViewModel.dropoffPlace = place
            mBinding.destinationSearch.setText(place!!.address!!.toString());
            (activity as MapsActivity).updateMarkers(place.latLng!!, Utility.DROPOFF_MARKER, true)
        }
        mBinding.placesRecyclerView.visibility = View.GONE
    }

    fun onSwapClicked() {
        if (mRideStatusViewModel.dropoffPlace == null && mRideStatusViewModel.pickupPlace == null) return

        mRideStatusViewModel.isSwapped = 2;
        val temp = mBinding.pickupSearch.text
        mBinding.pickupSearch.text = mBinding.destinationSearch.text
        mBinding.destinationSearch.text = temp

        if (mRideStatusViewModel.dropoffPlace == null) {
            mRideStatusViewModel.dropoffPlace = mRideStatusViewModel.pickupPlace
            (activity as MapsActivity).updateMarkers(mRideStatusViewModel.dropoffPlace!!.latLng!!,
                Utility.DROPOFF_MARKER, true)
        } else if ( mRideStatusViewModel.pickupPlace == null) {
            mRideStatusViewModel.pickupPlace = mRideStatusViewModel.dropoffPlace
            (activity as MapsActivity).updateMarkers(mRideStatusViewModel.pickupPlace!!.latLng!!,
                Utility.PICKUP_MARKER, true)
        } else {
            val tempPlace = mRideStatusViewModel.dropoffPlace
            mRideStatusViewModel.dropoffPlace = mRideStatusViewModel.pickupPlace
            mRideStatusViewModel.pickupPlace = tempPlace
            (activity as MapsActivity).updateMarkers(mRideStatusViewModel.pickupPlace!!.latLng!!,
                Utility.PICKUP_MARKER, false)
            (activity as MapsActivity).updateMarkers(mRideStatusViewModel.dropoffPlace!!.latLng!!,
                Utility.DROPOFF_MARKER, true)
        }
    }

}