package com.bodakesatish.sandhyasbeautyservices.data.repository

import com.bodakesatish.sandhyasbeautyservices.data.mapper.AppointmentMapper.mapFromDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.AppointmentMapper.mapToDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.CategoryMapper.mapToDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.CustomerMapper.mapToDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.ServiceMapper.mapToDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.AppointmentsDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.CustomerDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.ServiceDetailDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.ServicesDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceDetailEntity
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentCustomer
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentServices
import com.bodakesatish.sandhyasbeautyservices.domain.model.CategoriesWithServices
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetail
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.map

class AppointmentRepositoryImpl @Inject constructor(
    private val appointmentDao: AppointmentsDao,
    private val serviceDao: ServicesDao,
    private val serviceDetailDao: ServiceDetailDao,
    private val customerDao: CustomerDao
) : AppointmentRepository {

    override suspend fun createNewAppointment(
        customer: Customer,
        appointment: Appointment,
        selectedServicesWithDetails: List<Int>
    ): Long {

        val appointmentId = appointmentDao.insertOrUpdate(appointment.mapFromDomainModel())

        for (serviceId in selectedServicesWithDetails) {
            val service = serviceDao.getServiceById(serviceId)
            val serviceDetail = ServiceDetailEntity(
                id = 0,
                customerId = customer.id,
                appointmentId = appointmentId.toInt(),
                serviceId = serviceId,
                amount = service?.servicePrice ?: 0.0,
                discount = 0,
                priceAfterDiscount = service?.servicePrice ?: 0.0
            )
            serviceDetailDao.insertOrUpdate(serviceDetail)
        }

        return appointmentId
    }

    override suspend fun getServiceDetailsForAppointment(appointmentId: Int): Flow<List<Int>> {
        return serviceDetailDao.getServiceIdsForAppointment(appointmentId.toLong())
    }

    override fun getAllAppointments(): Flow<List<AppointmentCustomer>> {
        return customerDao.getAppointmentCustomerList().map { appointmentCustomerEntityList ->
            appointmentCustomerEntityList.filter { appointmentCustomerEntity ->
                appointmentCustomerEntity.appointments.isNotEmpty()
            }.map { appointmentCustomerEntity ->
                AppointmentCustomer(
                    customer = appointmentCustomerEntity.customer.mapToDomainModel(),
                    appointment = appointmentCustomerEntity.appointments.get(0).mapToDomainModel()
                )
            }
        }
    }

    override fun getAppointmentDetail(appointmentId: Int): Flow<AppointmentServices> {
        // Get the Flow of AppointmentsEntity from the DAO
        return appointmentDao.getAppointmentById(appointmentId)
            // Use the map operator to transform each AppointmentsEntity
            // emitted by the Flow into an Appointment
            // Map the entity to a domain model, handling the case where the entity is null
            .map{ appointmentsEntity ->
                AppointmentServices(
                    customer = customerDao.getCustomerById(appointmentsEntity!!.customerId.toLong())!!.mapToDomainModel(),
                    appointment = appointmentsEntity.mapToDomainModel()
                )

            }
        // Now the Flow emits null if no entity was found
    }

    // Alternative using firstOrNull/singleOrNull at the collection point
    // This approach returns Flow<Appointment?> as well, but handles null at the end of the flow processing
    override suspend fun getAppointmentDetailUsingTerminalOperator(appointmentId: Int): Flow<Appointment?> {
        return appointmentDao.getAppointmentById(appointmentId)
            .map { appointmentsEntity -> appointmentsEntity?.mapToDomainModel() }
            .firstOrNull() // Or singleOrNull() - this operator is applied when collecting the Flow
            .let { appointment -> flowOf(appointment) } // Wrap the result back into a Flow if needed
    }

}