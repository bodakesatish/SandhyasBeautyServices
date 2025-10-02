package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import javax.inject.Inject

class CreateNewAppointmentUseCase @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(
        appointment: Appointment,
        selectedServicesWithDetails: List<Int>
    ): Int {

        val appointmentId = appointmentRepository.createNewAppointment(
            appointment,
            selectedServicesWithDetails
        )
        if(appointment.id != 0) {
         return appointment.id
        } else
        return appointmentId.toInt()
    }
}
