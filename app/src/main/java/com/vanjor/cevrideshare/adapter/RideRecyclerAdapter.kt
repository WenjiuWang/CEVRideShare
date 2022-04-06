package com.vanjor.cevrideshare.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vanjor.cevrideshare.Utility
import com.vanjor.cevrideshare.databinding.RecyclerItemRideInfoBinding
import com.vanjor.cevrideshare.model.RideInfo
import java.text.DecimalFormat
import java.util.*

class RideRecyclerAdapter(context: Context, recyclerType: Int) : RecyclerView.Adapter<RideRecyclerAdapter.RideViewHolder>() {
    private val TAG = "RideRecyclerAdapter"
    private var mResultList: ArrayList<RideInfo>? = ArrayList()
    private var mClickListener: ClickListener? = null
    private var mRecyclerType = recyclerType
    private var mClickedViewHolder: RideViewHolder? = null

    private lateinit var mRecyclerItemRideInfoBinding: RecyclerItemRideInfoBinding

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RideViewHolder {
        mRecyclerItemRideInfoBinding = RecyclerItemRideInfoBinding
            .inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return RideViewHolder(mRecyclerItemRideInfoBinding)
    }

    override fun onBindViewHolder(mRideViewHolder: RideViewHolder, i: Int) {
        with(mRideViewHolder){
            with(mResultList!![i]) {
                rootBinding.expandUserName.text = driverProfile?.firstName
                rootBinding.expandUserName.text =  driverProfile?.firstName
                rootBinding.expandPlateNumber.text = driverProfile?.vehicleInfo!!.licensePlate
                rootBinding.expandModelName.text = driverProfile?.vehicleInfo!!.model
                rootBinding.expandRatingValue.text = DecimalFormat("#.##").format(driverProfile?.userRating).toString()
                rootBinding.priceText.text = Utility.getCost(cost!!)
                rootBinding.locationInfoTime.text = Utility.getRemainingTime(pickUpTime!!)
                rootBinding.locationInfoDist.text = Utility.getRemaininDist(pickUpDistance!!)

                mRecyclerItemRideInfoBinding.recyclerUserName.text = driverProfile?.firstName
                mRecyclerItemRideInfoBinding.preferenceText.text = getPreferenceTextByType(i)
            }
        }
    }


    private fun getPreferenceTextByType(i: Int): String {
        return when(mRecyclerType) {
            Utility.PRICE_RECYCLER -> "$" + Utility.getCost(mResultList!![i].cost!!)
            Utility.COMFORT_RECYCLER -> mResultList!![i].driverProfile?.vehicleInfo!!.model
            Utility.PICKUP_RECYCLER -> Utility.getRemainingTime(mResultList!![i].pickUpTime!!)

            else -> {
                DecimalFormat("#.##").format(mResultList!![i].driverProfile?.userRating).toString()
            }
        }
    }


    override fun getItemCount() = mResultList!!.size

    inner class RideViewHolder(recyclerItemRideInfoBinding: RecyclerItemRideInfoBinding)
        : RecyclerView.ViewHolder(recyclerItemRideInfoBinding.root), View.OnClickListener {
        var rootBinding = recyclerItemRideInfoBinding

        init {
            recyclerItemRideInfoBinding.root.setOnClickListener(this)
            rootBinding.confirmButton.setOnClickListener {
                mClickListener?.onClickCallback(mResultList!![adapterPosition])
            }

            if (mRecyclerType == Utility.DEFAULT_RECYCLER) {
                recyclerItemRideInfoBinding.recyclerRatingIcon.visibility = View.VISIBLE
            } else {
                recyclerItemRideInfoBinding.recyclerRatingIcon.visibility = View.GONE
            }
        }

        override fun onClick(v: View) {
            Log.d(TAG, "onClick")
            mClickedViewHolder?.toggleFold()
            mClickedViewHolder = this
            this.toggleExpand()

        }

        fun toggleFold() {
            if (rootBinding.rideInfoCard.visibility == View.VISIBLE) {
                rootBinding.rideInfoCard.visibility = View.GONE
                rootBinding.recyclerViewHolder.visibility = View.VISIBLE
            }
        }

        fun toggleExpand() {
            if (rootBinding.rideInfoCard.visibility == View.GONE) {
                rootBinding.rideInfoCard.visibility = View.VISIBLE
                rootBinding.recyclerViewHolder.visibility = View.GONE
            }
        }
    }


    interface ClickListener {
        fun onClickCallback(rideInfo: RideInfo?)
    }

    fun setClickListener(clickListener: ClickListener) {
        mClickListener = clickListener
    }

    fun setResultList(newList: ArrayList<RideInfo>) {
        mResultList = newList
        notifyDataSetChanged()
    }

}