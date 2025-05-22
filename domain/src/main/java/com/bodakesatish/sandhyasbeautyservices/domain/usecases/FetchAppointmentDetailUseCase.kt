package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FetchAppointmentDetailUseCase @Inject constructor(
    private val categoryRepository: AppointmentRepository
) {
    operator fun invoke(appointmentId : Int) = categoryRepository.fetchAppointment(appointmentId = appointmentId)
}