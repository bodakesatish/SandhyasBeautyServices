package com.bodakesatish.sandhyasbeautyservices.domain.model

data class Customer(
    val id: Int = 0,
    var firstName: String = "",
    var lastName: String = "",
    var phone: String = "",
    var age: Int = 0,
    var address: String = ""
)