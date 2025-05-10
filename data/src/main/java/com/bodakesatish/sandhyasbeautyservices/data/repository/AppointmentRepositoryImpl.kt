package com.bodakesatish.sandhyasbeautyservices.data.repository

import com.bodakesatish.sandhyasbeautyservices.data.mapper.AppointmentMapper.mapFromDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.AppointmentMapper.mapToDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.AppointmentsDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.ServiceDetailDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.ServicesDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceDetailEntity
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetail
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppointmentRepositoryImpl @Inject constructor(
    private val appointmentDao: AppointmentsDao,
    private val serviceDao: ServicesDao,
    private val serviceDetailDao: ServiceDetailDao,
) : AppointmentRepository {

    override suspend fun createNewAppointment(
        customer: Customer,
        appointment: Appointment,
        selectedServicesWithDetails: List<Int>
    ) : Long {

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
                priceAfterDiscount = 0.0
            )
            serviceDetailDao.insertOrUpdate(serviceDetail)
        }

        return appointmentId
    }

    override suspend fun getServiceDetailsForAppointment(appointmentId: Int): Flow<Result<List<ServiceDetail>>> {
        TODO("Not yet implemented")
    }

    override fun getAllAppointments(): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentList().map { appointmentList ->
            appointmentList.map { appointment ->
                appointment.mapToDomainModel()
            }
        }
    }

}