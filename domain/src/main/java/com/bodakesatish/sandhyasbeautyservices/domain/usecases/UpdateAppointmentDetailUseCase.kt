package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateAppointmentDetailUseCase @Inject constructor(
    private val categoryRepository: AppointmentRepository
) {
    suspend operator fun invoke(appointment : Appointment) = categoryRepository.updateAppointment(appointment = appointment)
}