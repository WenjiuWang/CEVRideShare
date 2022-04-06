package com.vanjor.cevrideshare.model

data class UserProfile(
    val userId: Long,
    var firstName: String,
    var lastName: String,
    var userRating: Float,
    var vehicleInfo: VehicleInfo?,
)