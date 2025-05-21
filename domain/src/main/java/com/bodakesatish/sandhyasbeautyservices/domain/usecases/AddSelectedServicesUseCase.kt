package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import javax.inject.Inject

class AddSelectedServicesUseCase @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(
        appointmentId: Int,
        selectedServicesWithDetails: List<Int>,
        totalPrice: Double
    ): Int {

        val appointmentId = appointmentRepository.saveSelectedServices(
            appointmentId,
            selectedServicesWithDetails,
            totalPrice
        )
        return appointmentId.toInt()
    }
}
