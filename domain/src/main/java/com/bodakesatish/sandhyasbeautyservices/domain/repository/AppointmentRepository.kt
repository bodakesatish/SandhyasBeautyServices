package com.bodakesatish.sandhyasbeautyservices.domain.repository

import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentCustomer
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentServices
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {

    suspend fun createNewAppointment(
        customer: Customer,
        appointment: Appointment,
        selectedServicesWithDetails: List<Int> // Service, Amount, Discount
    ) : Long //: Result<Long> // Returns the ID of the new appointment or an error

    suspend fun getServiceDetailsForAppointment(appointmentId: Int): Flow<List<Int>>

    fun getAllAppointments(): Flow<List<AppointmentCustomer>>

    fun getAppointmentDetail(appointmentId: Int): Flow<AppointmentServices?>

    suspend fun getAppointmentDetailUsingTerminalOperator(appointmentId: Int): Flow<Appointment?>

}