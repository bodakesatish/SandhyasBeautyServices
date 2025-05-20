package com.bodakesatish.sandhyasbeautyservices.domain.model

data class ServiceDetail(
    val id: Int = 0,
    val customerId: Int = 0,
    val appointmentId: Int = 0,
    val serviceId: Int = 0,
    val originalAmount: Double = 0.0,
    val discount: Double = 0.0,
    val discountPercentage: Double = 0.0,
    val priceAfterDiscount: Double = 0.0,
    val serviceSummary: String = ""
)