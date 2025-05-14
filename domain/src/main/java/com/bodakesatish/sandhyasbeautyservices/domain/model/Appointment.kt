package com.bodakesatish.sandhyasbeautyservices.domain.model

import java.util.Date

data class Appointment(
    var id: Int = 0,
    var customerId: Int = 0,
    var appointmentDate: Date = Date(),
    var appointmentTime: Date = Date(),
    var appointmentPlannedTime: Date = Date(),
    var appointmentCompletedTime: Date = Date(),
    var totalBillAmount: Double = 0.0,
    val appointmentStatus: String = "PENDING",
    var paymentMode: String = "ONLINE"
)