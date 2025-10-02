package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteAppointmentDetailUseCase @Inject constructor(
    private val categoryRepository: AppointmentRepository
) {
    suspend operator fun invoke(appointmentId : Int) = categoryRepository.deleteAppointment(appointmentId = appointmentId)
}