package com.bodakesatish.sandhyasbeautyservices.domain.model

data class Service(
    val id: Int = 0,
    var categoryId: Int = 0,
    var serviceName: String = "",
    var serviceDescription: String = "",
    var servicePrice: Double = 0.0
)