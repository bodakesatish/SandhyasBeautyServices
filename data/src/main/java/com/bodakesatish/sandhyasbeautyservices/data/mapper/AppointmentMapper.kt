package com.bodakesatish.sandhyasbeautyservices.data.mapper

import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.AppointmentsEntity
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment

object AppointmentMapper : Mapper<AppointmentsEntity, Appointment> {
    override fun AppointmentsEntity.mapToDomainModel(): Appointment {
        return Appointment(
            id = id,
            customerId = customerId,
            appointmentDate = appointmentDate,
            appointmentTime = appointmentTime,
            appointmentPlannedTime = appointmentPlannedTime,
            appointmentCompletedTime = appointmentCompletedTime,
            totalBillAmount = totalBillAmount,
            appointmentStatus = appointmentStatus,
            paymentMode = paymentMode
        )
    }

    override fun Appointment.mapFromDomainModel(): AppointmentsEntity {
        return AppointmentsEntity(
            id = id,
            customerId = customerId,
            appointmentDate = appointmentDate,
            appointmentTime = appointmentTime,
            appointmentPlannedTime = appointmentPlannedTime,
            appointmentCompletedTime = appointmentCompletedTime,
            totalBillAmount = totalBillAmount,
            appointmentStatus = appointmentStatus,
            paymentMode = paymentMode
        )
    }

}