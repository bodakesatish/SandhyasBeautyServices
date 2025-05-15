package com.bodakesatish.sandhyasbeautyservices.domain.model

data class AppointmentServices(val customer: Customer, val appointment: Appointment, val services: List<ServiceDetailWithService>)