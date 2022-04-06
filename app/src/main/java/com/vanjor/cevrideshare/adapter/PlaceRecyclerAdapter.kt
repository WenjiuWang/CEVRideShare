package com.vanjor.cevrideshare.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.*
import com.vanjor.cevrideshare.adapter.PlacesRecyclerAdapter.PlaceViewHolder
import java.util.*
import android.widget.Toast

import com.google.android.libraries.places.api.model.TypeFilter

import com.google.android.libraries.places.api.net.FetchPlaceResponse

import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.vanjor.cevrideshare.R


class PlacesRecyclerAdapter(context: Context, placesClient: PlacesClient) : RecyclerView.Adapter<PlaceViewHolder>(), Filterable  {
    private var mResultList: ArrayList<AutocompletePrediction>? = ArrayList()
    private var STYLE_BOLD: CharacterStyle? = StyleSpan(Typeface.BOLD)
    private var STYLE_NORMAL: CharacterStyle? = StyleSpan(Typeface.NORMAL)
    private val mContext = context
    private val mPlacesClient = placesClient
    private var mClickListener: ClickListener? = null

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results = FilterResults()
                mResultList = getPredictions(constraint)
                if (mResultList != null) {
                    results.values = mResultList
                    results.count = mResultList!!.size
                }
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                if (results.count > 0) {
                    notifyDataSetChanged()
                }
            }
        }
    }

    private fun getPredictions(constraint: CharSequence): ArrayList<AutocompletePrediction>? {
        val result: ArrayList<AutocompletePrediction> = arrayListOf()
        val token = AutocompleteSessionToken.newInstance()

        val request =
            FindAutocompletePredictionsRequest.builder()
                .setCountries("CA")
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(constraint.toString())
                .build()

        mPlacesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                result.addAll(response.autocompletePredictions)
                notifyDataSetChanged()
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: " + exception.statusCode)
                }
            }
        return result
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PlaceViewHolder {
        val layoutInflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val convertView = layoutInflater.inflate(R.layout.recycler_item_places, viewGroup, false)
        return PlaceViewHolder(convertView)
    }

    override fun onBindViewHolder(mPlaceViewHolder: PlaceViewHolder, i: Int) {
        mPlaceViewHolder.mPlaceName.text = mResultList!![i].getPrimaryText(STYLE_BOLD)
        mPlaceViewHolder.mPlaceDetail.text = mResultList!![i].getSecondaryText(STYLE_NORMAL)
    }

    override fun getItemCount(): Int {
        return mResultList!!.size
    }

    inner class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val mPlaceName: TextView
        val mPlaceDetail: TextView

        init {
            mPlaceName = view.findViewById(R.id.place_item_name)
            mPlaceDetail = view.findViewById(R.id.place_item_detail)
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val item = mResultList!![adapterPosition]
            val placeFields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )
            val request = FetchPlaceRequest.builder(item.placeId, placeFields).build()
            mPlacesClient.fetchPlace(request)
                .addOnSuccessListener(OnSuccessListener<FetchPlaceResponse> { response ->
                    val place = response.place
                    mClickListener?.onClickCallback(place)
                }).addOnFailureListener(OnFailureListener { exception ->
                    if (exception is ApiException) {
                        Toast.makeText(mContext, exception.message + "", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    companion object {
        private const val TAG = "PlacesAutoAdapter"
    }

    interface ClickListener {
        fun onClickCallback(place: Place?)
    }

    fun setClickListener(clickListener: ClickListener) {
        mClickListener = clickListener
    }

}