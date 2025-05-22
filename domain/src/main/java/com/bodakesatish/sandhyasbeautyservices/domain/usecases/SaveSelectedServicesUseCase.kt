package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import javax.inject.Inject

class SaveSelectedServicesUseCase @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(
        appointmentId: Int,
        selectedServicesWithDetails: List<Int>,
        totalPrice: Double
    ): Int {

        val appointmentId = appointmentRepository.saveSelectedServicesN(
            appointmentId,
            selectedServicesWithDetails,
            totalPrice
        )
        return appointmentId.toInt()
    }
}
