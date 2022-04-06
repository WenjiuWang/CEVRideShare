package com.vanjor.cevrideshare.viewmodel

import androidx.lifecycle.ViewModel

class SignUpViewModel: ViewModel() {
    var firstName: String? = null
    var lastName: String? = null
    var phone: String? = null

    var email: String? = null
    var password: String? = null
    var confirmPassword: String? = null
    
    var needVehicle: Boolean = false

    var vin: String? = null
    var make: String? = null
    var model: String? = null
    var year: String? = null
    var seats: String? = null
    var color: String? = null
    var licensePlate: String? = null

}