package com.vanjor.cevrideshare.model

data class VehicleInfo (
    val id: Long,
    var vehicleVin: String,
    var licensePlate: String,
    var make: String,
    var model: String,
    var numSeats: Int,
    var color: String,
    var vehicleType: String,
    var vehicleRating: Float,
) {
}