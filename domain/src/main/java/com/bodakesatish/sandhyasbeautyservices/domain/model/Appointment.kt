package com.bodakesatish.sandhyasbeautyservices.domain.model

data class Appointment(
    val id: Int,
    val customerId: Int,
    val appointmentDate: String,
    val appointmentTime: String,
    val appointmentPlannedTime: String,
    val appointmentCompletedTime: String,
    val totalBillAmount: Double,
    val appointmentStatus: String,
    val paymentMode: String
)