package com.bodakesatish.sandhyasbeautyservices.domain.repository

import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.CustomerAppointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentServices
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.model.PaymentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetail
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
        appointment: Appointment,
        selectedServicesWithDetails: List<Int> // Service, Amount, Discount
    ) : Long //: Result<Long> // Returns the ID of the new appointment or an error

    suspend fun saveSelectedServices(
        appointmentId: Int,
        selectedServicesWithDetails: List<Int>, // Service, Amount, Discount
        totalPrice: Double
    ) : Long //: Result<Long> // Returns the ID of the new appointment or an error

    suspend fun updateSelectedServices(
        appointmentId: Int,
        selectedServicesWithDetails: List<ServiceDetailWithService>, // Service, Amount, Discount
    ) : Long //: Result<Long> // Returns the ID of the new appointment or an error

    suspend fun saveSelectedServicesN(
        appointmentId: Int,
        selectedServicesWithDetails: List<Int>, // Service, Amount, Discount
        totalPrice: Double
    ) : Long //: Result<Long> // Returns the ID of the new appointment or an error

    suspend fun getServiceDetailsForAppointment(appointmentId: Int): Flow<List<ServiceDetailWithService>>

    suspend fun getSelectedServices(appointmentId: Int): Flow<List<ServiceDetail>>

    fun getAllAppointments(): Flow<List<CustomerAppointment>>

    fun getAppointmentDetail(appointmentId: Int): Flow<AppointmentServices?>

    suspend fun getAppointmentDetailUsingTerminalOperator(appointmentId: Int): Flow<Appointment?>

    fun getAppointment(appointmentId: Int): Flow<Appointment>

    fun getCustomerAppointment(appointmentId: Int): Flow<CustomerAppointment>

    fun fetchAppointment(appointmentId: Int): Flow<Appointment>

    suspend fun updateAppointment(appointment: Appointment): Int

    fun getAppointments(
        startDate: Long,
        endDate: Long,
        status: AppointmentStatus?,
        paymentStatus: PaymentStatus?,
        customerNameQuery: String?,
        sortBy: AppointmentSortBy,
        sortOrder: SortOrder
    ): Flow<List<CustomerAppointment>>
}