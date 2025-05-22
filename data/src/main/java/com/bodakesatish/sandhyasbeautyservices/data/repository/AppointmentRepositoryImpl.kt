package com.bodakesatish.sandhyasbeautyservices.data.repository

import com.bodakesatish.sandhyasbeautyservices.data.mapper.AppointmentMapper.mapFromDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.AppointmentMapper.mapToDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.CustomerMapper.mapToDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.ServiceDetailWithServiceMapper.mapToDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.ServiceDetailMapper.mapToDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.ServiceDetailWithServiceMapper.mapFromDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.AppointmentsDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.CustomerDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.ServiceDetailDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.ServicesDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.AppointmentsEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CustomerEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceDetailEntity
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.CustomerAppointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentServices
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.model.PaymentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetail
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetailWithService
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentSortBy
import com.bodakesatish.sandhyasbeautyservices.domain.repository.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppointmentRepositoryImpl @Inject constructor(
    private val appointmentDao: AppointmentsDao,
    private val serviceDao: ServicesDao,
    private val serviceDetailDao: ServiceDetailDao,
    private val customerDao: CustomerDao
) : AppointmentRepository {

    override suspend fun createNewAppointment(
        appointment: Appointment,
        selectedServicesWithDetails: List<Int>
    ): Long {

        val appointmentId = appointmentDao.insertOrUpdate(appointment.mapFromDomainModel())
        if (appointment.id != 0) {
            serviceDetailDao.deleteServicesByAppointment(appointmentId)
        }

        for (serviceId in selectedServicesWithDetails) {
            val service = serviceDao.getServiceById(serviceId)
            val serviceDetail = ServiceDetailEntity(
                id = 0,
                customerId = appointment.customerId,
                appointmentId = appointmentId.toInt(),
                serviceId = serviceId,
                originalAmount = service?.servicePrice ?: 0.0,
                discount = 0.0,
                discountPercentage = 0.0,
                priceAfterDiscount = service?.servicePrice ?: 0.0,
                serviceSummary = ""
            )
            serviceDetailDao.insertOrUpdate(serviceDetail)
        }

        return appointmentId
    }

    override suspend fun saveSelectedServices(
        appointmentId: Int,
        selectedServicesWithDetails: List<Int>,
        totalPrice: Double
    ): Long {
        val appointment = appointmentDao.getAppointmentById(appointmentId).firstOrNull()
        serviceDetailDao.deleteServicesByAppointment(appointmentId.toLong())

        val updateTotalPrice = appointmentDao.updateTotalPrice(appointmentId, totalPrice)

        for (serviceId in selectedServicesWithDetails) {
            val service = serviceDao.getServiceById(serviceId)
            val serviceDetail = ServiceDetailEntity(
                id = 0,
                customerId = appointment!!.customerId,
                appointmentId = appointmentId.toInt(),
                serviceId = serviceId,
                originalAmount = service?.servicePrice ?: 0.0,
                discount = 0.0,
                discountPercentage = 0.0,
                priceAfterDiscount = service?.servicePrice ?: 0.0,
                serviceSummary = ""
            )
            serviceDetailDao.insertOrUpdate(serviceDetail)
        }
        return 1
    }

    override suspend fun updateSelectedServices(
        appointmentId: Int,
        selectedServicesWithDetails: List<ServiceDetailWithService>
    ): Long {
        serviceDetailDao.deleteServicesByAppointment(appointmentId.toLong())

        for (serviceDetail in selectedServicesWithDetails) {
            val serviceDetailEntity = ServiceDetailEntity(
                id = serviceDetail.id,
                customerId = serviceDetail.customerId,
                appointmentId = appointmentId.toInt(),
                serviceId = serviceDetail.serviceId,
                originalAmount = serviceDetail.originalPrice,
                discount = serviceDetail.discountAmount,
                discountPercentage = 0.0,
                priceAfterDiscount = (serviceDetail.originalPrice - serviceDetail.discountAmount),
                serviceSummary = ""
            )
            serviceDetailDao.insertOrUpdate(serviceDetailEntity)
        }
        return 0
    }

    override suspend fun saveSelectedServicesN(
        appointmentId: Int,
        selectedServicesWithDetails: List<Int>,
        totalPrice: Double
    ): Long {
        val appointment = appointmentDao.getAppointmentById(appointmentId).firstOrNull()
        serviceDetailDao.deleteServicesByAppointment(appointmentId.toLong())

        val updateTotalPrice = appointmentDao.updateTotalPrice(appointmentId, totalPrice)

        for (serviceId in selectedServicesWithDetails) {
            val service = serviceDao.getServiceById(serviceId)
            val serviceDetail = ServiceDetailEntity(
                id = 0,
                customerId = appointment!!.customerId,
                appointmentId = appointmentId.toInt(),
                serviceId = serviceId,
                originalAmount = service?.servicePrice ?: 0.0,
                discount = 0.0,
                discountPercentage = 0.0,
                priceAfterDiscount = service?.servicePrice ?: 0.0,
                serviceSummary = ""
            )
            serviceDetailDao.insertOrUpdate(serviceDetail)
        }
        return 1
    }

    override suspend fun getServiceDetailsForAppointment(appointmentId: Int): Flow<List<ServiceDetailWithService>> {
        return serviceDetailDao.getServiceDetailsWithServiceForAppointment(appointmentId).map {
            it.map { serviceDetailWithService ->
                serviceDetailWithService.mapToDomainModel()
            }
        }
    }

    override suspend fun getSelectedServices(appointmentId: Int): Flow<List<ServiceDetail>> {
        return serviceDetailDao.getServiceDetailsByAppointmentId(appointmentId.toLong())
            .map { serviceEntityList ->
                serviceEntityList.map { service ->
                    service.mapToDomainModel()
                }
            }
    }

    override fun getAllAppointments(): Flow<List<CustomerAppointment>> {
        // This assumes appointmentDao.getAppointmentCustomerList() returns a list of Room's
        // AppointmentCustomer (which has @Embedded AppointmentsEntity and @Relation CustomerEntity)
        return appointmentDao.getAppointmentCustomerList().map { appointmentCustomerEntityList ->
            appointmentCustomerEntityList.map {
                CustomerAppointment(
                    // Assuming entity.appointment is AppointmentsEntity and entity.customer is CustomerEntity
                    customer = it.customer.mapToDomainModel(),
                    appointment = it.appointment.mapToDomainModel()
                )
            }
        }
    }

    override fun getCustomerAppointment(appointmentId: Int): Flow<CustomerAppointment> {
        // AppointmentCustomer (which has @Embedded AppointmentsEntity and @Relation CustomerEntity)
        return appointmentDao.getAppointmentDetailById(appointmentId).map { appointmentDetail ->
            CustomerAppointment(
                // Assuming entity.appointment is AppointmentsEntity and entity.customer is CustomerEntity
                customer = appointmentDetail.customer.mapToDomainModel(),
                appointment = appointmentDetail.appointment.mapToDomainModel()
            )

        }
    }

    override fun fetchAppointment(appointmentId: Int): Flow<Appointment> {
        return appointmentDao.getAppointmentById(appointmentId).map { appointmentDetail ->
            appointmentDetail.mapToDomainModel()
        }
    }

    override suspend fun updateAppointment(appointment: Appointment): Int {
        return appointmentDao.update(appointment.mapFromDomainModel())
    }

    override fun getAppointments(
        startDate: Long,
        endDate: Long,
        status: AppointmentStatus?,
        paymentStatus: PaymentStatus?,
        customerNameQuery: String?,
        sortBy: AppointmentSortBy,
        sortOrder: SortOrder
    ): Flow<List<CustomerAppointment>> {
        // Delegate to the DAO method that handles complex filtering and sorting
        // This DAO method is expected to return Flow<List<com.example.data.local.entity.AppointmentCustomer>>
        // where AppointmentCustomer is the Room entity/POJO with @Embedded and @Relation

        // Map DOMAIN enums to what the DAO expects (e.g., String or data-layer enum)
        val statusString = status?.let { mapDomainStatusToString(it) }
        val paymentStatusString = paymentStatus?.let { mapDomainPaymentStatusToString(it) }

        return appointmentDao.getFilteredAppointments( // You need to create this DAO method
            startDate = startDate,
            endDate = endDate,
            status = status?.name, // Pass enum names as Strings if your DAO expects Strings
            paymentStatus = paymentStatus?.name, // Or pass enums directly if TypeConverters are set up
            customerNameQuery = customerNameQuery,
            sortByColumnName = mapSortByToColumnName(sortBy), // Helper to map enum to DB column name
            sortOrderSql = if (sortOrder == SortOrder.ASCENDING) "ASC" else "DESC"
        ).map { entityList ->
            entityList.map { entity -> // entity is Room's AppointmentCustomer POJO
                CustomerAppointment(
                    appointment = entity.appointment.mapToDomainModel(),
                    customer = entity.customer.mapToDomainModel()
                )
            }
        }
    }

    /**
     * Maps the domain-level AppointmentSortBy enum to actual database column names
     * that are expected by the DAO's ORDER BY CASE statements.
     *
     * IMPORTANT: The string values returned here MUST EXACTLY MATCH the strings
     * used in the `WHEN :sortByColumnName = '...'` conditions in your
     * `AppointmentsDao.getFilteredAppointments` query's `ORDER BY` clause.
     */
    private fun mapSortByToColumnName(sortBy: AppointmentSortBy): String {
        return when (sortBy) {
            AppointmentSortBy.DATE -> AppointmentsEntity.Columns.APPOINTMENT_DATE // e.g., "appointmentDate"
            AppointmentSortBy.CUSTOMER_NAME -> CustomerEntity.Columns.FIRST_NAME   // e.g., "customerFirstName"
            // For CUSTOMER_NAME, if you sort by full name, you might need a different strategy
            // or ensure your DAO's CASE statement for customer name handles this.
            // The current DAO sorts by first name if "customerFirstName" is passed.
            AppointmentSortBy.STATUS -> AppointmentsEntity.Columns.APPOINTMENT_STATUS // e.g., "status"
        }
    }

    // Example Mappers (Domain Enum to String for DAO)
    // Place these mappers appropriately, maybe in a dedicated mapper file if they get complex
    // or keep them private here if only used by this repository implementation.
    private fun mapDomainStatusToString(domainStatus: AppointmentStatus): String {
        return when (domainStatus) {
            AppointmentStatus.PENDING -> "PENDING"
            AppointmentStatus.COMPLETED -> "COMPLETED"
            AppointmentStatus.UNKNOWN -> "UNKNOWN"
        }
    }

    private fun mapDomainPaymentStatusToString(domainPaymentStatus: PaymentStatus): String {
        return when (domainPaymentStatus) {
            PaymentStatus.PAID -> "PAID"
            PaymentStatus.UNPAID -> "UNPAID"
            PaymentStatus.PARTIALLY_PAID -> "PARTIALLY_PAID"
            PaymentStatus.UNKNOWN -> "UNKNOWN"
        }
    }

    override fun getAppointmentDetail(appointmentId: Int): Flow<AppointmentServices?> {
        val appointmentFlow: Flow<AppointmentsEntity?> =
            appointmentDao.getAppointmentById(appointmentId)
        val servicesFlow: Flow<List<ServiceDetailWithService>> =
            serviceDetailDao.getServiceDetailsWithServiceForAppointment(appointmentId) // Assuming this returns the domain model directly
                .map { list -> list.map { it.mapToDomainModel() } } // if it returns entity list

        return appointmentFlow.combine(servicesFlow) { appointmentEntity, servicesList ->
            appointmentEntity?.let { entity ->
                // Fetch customer separately. This could also be a Flow and combined.
                // For simplicity, let's assume customerDao.getCustomerById is suspend and can be called
                // in a context where this combination happens (e.g. if combined within a flow builder)
                // This part is still tricky if getCustomerById is suspend.
                // A better approach would be to make customer a flow too.
                // Let's assume for now customer can be fetched if appointmentEntity exists.

                // This synchronous fetch of customer inside combine might block if not careful.
                // Ideally, customer is also a flow or fetched in a different manner.
                // For this example, let's simplify and assume it's okay.
                val customerEntity =
                    customerDao.getCustomerById(entity.customerId.toLong()) // This needs to be handled carefully.

                if (customerEntity != null) {
                    AppointmentServices(
                        customer = customerEntity.mapToDomainModel(),
                        appointment = entity.mapToDomainModel(),
                        services = servicesList
                    )
                } else {
                    null // Customer not found
                }
            }
        }
    }


    // Alternative using firstOrNull/singleOrNull at the collection point
    // This approach returns Flow<Appointment?> as well, but handles null at the end of the flow processing
    override suspend fun getAppointmentDetailUsingTerminalOperator(appointmentId: Int): Flow<Appointment?> {
        return appointmentDao.getAppointmentById(appointmentId)
            .map { appointmentsEntity -> appointmentsEntity?.mapToDomainModel() }
            .firstOrNull() // Or singleOrNull() - this operator is applied when collecting the Flow
            .let { appointment -> flowOf(appointment) } // Wrap the result back into a Flow if needed
    }

    override fun getAppointment(appointmentId: Int): Flow<Appointment> {
        return appointmentDao.getAppointmentById(appointmentId).map {
            it.mapToDomainModel()
        }
    }

}