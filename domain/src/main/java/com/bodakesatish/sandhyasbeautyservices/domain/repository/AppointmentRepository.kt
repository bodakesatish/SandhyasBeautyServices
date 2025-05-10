package com.bodakesatish.sandhyasbeautyservices.domain.repository

import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetail
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {

    suspend fun createNewAppointment(
        customer: Customer,
        appointment: Appointment,
        selectedServicesWithDetails: List<Int> // Service, Amount, Discount
    ) : Long //: Result<Long> // Returns the ID of the new appointment or an error

    suspend fun getServiceDetailsForAppointment(appointmentId: Int): Flow<Result<List<ServiceDetail>>>

    fun getAllAppointments(): Flow<List<Appointment>>
}