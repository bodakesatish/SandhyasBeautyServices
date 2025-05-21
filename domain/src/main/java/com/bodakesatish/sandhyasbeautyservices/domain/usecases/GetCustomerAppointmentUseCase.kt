package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetCustomerAppointmentUseCase @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) {
    operator fun invoke(appointmentId: Int) = appointmentRepository.getCustomerAppointment(appointmentId = appointmentId)
}