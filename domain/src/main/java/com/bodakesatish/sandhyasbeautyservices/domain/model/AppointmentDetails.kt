package com.bodakesatish.sandhyasbeautyservices.domain.model

// Define this data class in your domain or data layer,
// depending on where you prefer to define data structures
// that combine multiple entities.
data class AppointmentDetails(
    val appointment: Appointment,
    val customer: Customer?, // Customer might be nullable if an appointment can exist without a customer
    val serviceDetailsWithServices: List<ServiceDetailWithService>
)