package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import javax.inject.Inject
import javax.inject.Singleton

import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentCustomer
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.model.PaymentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentSortBy
import com.bodakesatish.sandhyasbeautyservices.domain.repository.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

@Singleton
class GetFilteredAppointmentListUseCase @Inject constructor(
    // Renamed from categoryRepository to appointmentRepository for clarity
    private val appointmentRepository: AppointmentRepository
) {
    /**
     * Fetches a list of appointments based on the provided filter criteria and sorting options.
     *
     * @param startDate The start date (inclusive) for the appointment range (in milliseconds).
     * @param endDate The end date (inclusive) for the appointment range (in milliseconds).
     * @param status Optional filter for appointment status. If null, status is not filtered.
     * @param paymentStatus Optional filter for payment status. If null, payment status is not filtered.
     * @param customerNameQuery Optional query string to search for customer names. If null or blank, not filtered.
     * @param sortBy The criteria by which to sort the appointments.
     * @param sortOrder The order of sorting (ascending or descending).
     * @return A Flow emitting a list of AppointmentCustomer matching the criteria.
     *         The flow will operate on Dispatchers.IO.
     */
    operator fun invoke(
        startDate: Long,
        endDate: Long,
        status: AppointmentStatus?,
        paymentStatus: PaymentStatus?,
        customerNameQuery: String?,
        sortBy: AppointmentSortBy,
        sortOrder: SortOrder
    ): Flow<List<AppointmentCustomer>> {
        // Delegate to the repository to get the appointments
        // The repository implementation will handle the actual data fetching and mapping
        return appointmentRepository.getAppointments(
            startDate = startDate,
            endDate = endDate,
            status = status,
            paymentStatus = paymentStatus,
            customerNameQuery = customerNameQuery,
            sortBy = sortBy,
            sortOrder = sortOrder
        ).flowOn(Dispatchers.IO) // Ensure the upstream operations (DB/network) run on IO dispatcher
        // The repository might also do this, but it's good practice
        // for the use case to define its execution context if relevant.
    }
}