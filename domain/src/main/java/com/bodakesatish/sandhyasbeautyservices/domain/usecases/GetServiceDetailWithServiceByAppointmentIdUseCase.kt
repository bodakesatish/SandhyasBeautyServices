package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetServiceDetailWithServiceByAppointmentIdUseCase @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(appointmentId: Int) = appointmentRepository.getServiceDetailsForAppointment(appointmentId)
}