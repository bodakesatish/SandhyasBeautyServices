package com.bodakesatish.sandhyasbeautyservices.domain.model

data class ServiceDetail(
    val id: Int = 0,
    val customerId: Int,
    val appointmentId: Int,
    val serviceId: Int,
    val amount: Double,
    val discount: Int,
    val priceAfterDiscount: Double
)