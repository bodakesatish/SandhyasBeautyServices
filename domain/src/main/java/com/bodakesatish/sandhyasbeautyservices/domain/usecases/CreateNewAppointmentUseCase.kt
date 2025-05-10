package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import javax.inject.Inject

class CreateNewAppointmentUseCase @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(
        customer: Customer,
        appointment: Appointment,
        selectedServicesWithDetails: List<Int>
    ): Boolean {

        appointmentRepository.createNewAppointment(
            customer,
            appointment,
            selectedServicesWithDetails
        )
        return true
    }
}
