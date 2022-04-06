package com.vanjor.cevrideshare.util

import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.ColorInt

class PolylineAnimation(
    private var mMap: GoogleMap,
    private var points: List<LatLng>,
    private var polylineOptions: PolylineOptions = PolylineOptions(),
    private var duration: Long = 2000,
    private var interpolator: TimeInterpolator? = null,
    private val animatorListenerAdapter: AnimatorListenerAdapter? = null) : ValueAnimator.AnimatorUpdateListener {
    private var lineColor: Int = -1
    private var currentAnimatedPolyline: Polyline? = null
    private lateinit var sections: List<Double>
    private var totalPathDistance: Double = 0.0
    private var animator: ValueAnimator = ValueAnimator.ofFloat(0f, 100f)

    init {
        animator.duration = duration
        interpolator?.let {
            animator.interpolator = it
        }
        animator.addUpdateListener(this)
        animatorListenerAdapter?.let {
            animator.addListener(it)
        }
        animator.repeatCount = ValueAnimator.INFINITE
    }

    fun start() {
        sections = getSectionLengths(points)
        totalPathDistance = sections.sum()
        animatorListenerAdapter?.let {
            if (animator.listeners == null || !animator.listeners.contains(it)) {
                animator.addListener(it)
                animator.addUpdateListener(this)
            }
        }
        animator.start()
    }

    fun remove() {
        animator.removeUpdateListener(this)
        animatorListenerAdapter?.let {
            animator.removeListener(it)
        }
        animator.cancel()
        currentAnimatedPolyline?.remove()
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onAnimationUpdate(animation: ValueAnimator?) {
        val fraction = animator.animatedValue as Float
        val pathSection = totalPathDistance * fraction / 100
        val undonePolylineOptions = getCurrentPolylineOptions(points, sections, pathSection, copyPolyLineOptions(polylineOptions, fraction / 100))

        val newPolyline = mMap.addPolyline(undonePolylineOptions)
        currentAnimatedPolyline?.remove()
        currentAnimatedPolyline = newPolyline

    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun copyPolyLineOptions(o: PolylineOptions, alphaVal: Float): PolylineOptions {
        if (lineColor == -1) {
            lineColor = o.color
        }
        return PolylineOptions()
            .color(adjustAlpha(o.color, alphaVal))
            .width(o.width)
            .startCap(o.startCap)
            .endCap(o.endCap)
            .clickable(o.isClickable)
            .jointType(o.jointType)
            .visible(o.isVisible)
            .pattern(o.pattern) 
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @ColorInt
    fun adjustAlpha(@ColorInt color: Int, alphaVal: Float): Int {
        return (color and 0x00ffffff) or ((255 - Math.round(255 * alphaVal)) shl 24)
    }

    fun getCurrentPolylineOptions(path: List<LatLng>, sections: List<Double>, pathSection: Double, polylineOptions: PolylineOptions): PolylineOptions {
        var sectionSum = 0.0
        var pastSections = 0.0

        for ((index, value) in path.withIndex()) {
            polylineOptions.add(value)

            if (index < path.size - 1) {
                val to = path[index + 1]
                val currentSection = sections[index]
                sectionSum += currentSection
                if (pathSection < sectionSum) {
                    val fraction = (pathSection - pastSections) / currentSection
                    val pointToBeAdded = SphericalUtil.interpolate(value, to, fraction)
                    polylineOptions.add(pointToBeAdded)
                    return polylineOptions
                } else {
                    pastSections += currentSection
                }
            }
        }
        polylineOptions.add(path.last())
        return polylineOptions
    }

    fun getSectionLengths(path: List<LatLng>): List<Double> {
        val sections = mutableListOf<Double>()
        for ((index, value) in path.withIndex()) {
            if (index < path.size - 1) {
                val to = path[index + 1]
                val curLength = SphericalUtil.computeDistanceBetween(value, to)
                sections.add(curLength)
            }
        }
        return sections
    }
}