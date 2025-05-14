package com.bodakesatish.sandhyasbeautyservices.domain.model

import java.io.Serializable
import java.util.Date

data class Customer(
    val id: Int = 0,
    var firstName: String = "",
    var lastName: String = "",
    var phone: String = "",
    var dob: Date = Date(),
    var address: String = ""
) : Serializable