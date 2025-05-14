package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAppointmentDetailUseCase @Inject constructor(
    private val categoryRepository: AppointmentRepository
) {
    operator fun invoke(appointmentId : Int) = categoryRepository.getAppointmentDetail(appointmentId = appointmentId)
}