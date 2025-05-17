package com.bodakesatish.sandhyasbeautyservices.domain.repository

import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentCustomer
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentServices
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.model.PaymentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetailWithService
import kotlinx.coroutines.flow.Flow

//// Define these enums in your DOMAIN module
//enum class AppointmentStatus {
//    SCHEDULED,
//    COMPLETED,
//    CANCELLED,
//    PENDING // Add all relevant statuses for your domain
//}

//enum class PaymentStatus {
//    PAID,
//    UNPAID,
//    PARTIALLY_PAID // Add all relevant statuses for your domain
//}

enum class AppointmentSortBy {
    DATE,
    CUSTOMER_NAME,
    STATUS
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}

interface AppointmentRepository {

    suspend fun createNewAppointment(
        customer: Customer,
        appointment: Appointment,
        selectedServicesWithDetails: List<Int> // Service, Amount, Discount
    ) : Long //: Result<Long> // Returns the ID of the new appointment or an error

    suspend fun getServiceDetailsForAppointment(appointmentId: Int): Flow<List<ServiceDetailWithService>>

    fun getAllAppointments(): Flow<List<AppointmentCustomer>>

    fun getAppointmentDetail(appointmentId: Int): Flow<AppointmentServices?>

    suspend fun getAppointmentDetailUsingTerminalOperator(appointmentId: Int): Flow<Appointment?>

    fun getAppointment(appointmentId: Int): Flow<Appointment>

    fun getAppointments(
        startDate: Long,
        endDate: Long,
        status: AppointmentStatus?,
        paymentStatus: PaymentStatus?,
        customerNameQuery: String?,
        sortBy: AppointmentSortBy,
        sortOrder: SortOrder
    ): Flow<List<AppointmentCustomer>>
}