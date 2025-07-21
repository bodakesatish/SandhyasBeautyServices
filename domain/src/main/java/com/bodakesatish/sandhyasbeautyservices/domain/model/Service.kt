package com.bodakesatish.sandhyasbeautyservices.domain.model

import java.io.Serializable

data class Service(
    val id: Int = 0,
    var categoryId: String = "",
    var serviceName: String = "",
    var serviceDescription: String = "",
    var servicePrice: Double = 0.0,
    var isSelected: Boolean = false // To track selection
) : Serializable