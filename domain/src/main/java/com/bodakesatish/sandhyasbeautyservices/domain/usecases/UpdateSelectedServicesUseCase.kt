package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetailWithService
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import javax.inject.Inject

class UpdateSelectedServicesUseCase @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(
        appointmentId: Int,
        selectedServicesWithDetails: List<ServiceDetailWithService>
    ): Int {

        val response = appointmentRepository.updateSelectedServices(
            appointmentId,
            selectedServicesWithDetails,
        )
        return response.toInt()
    }
}
