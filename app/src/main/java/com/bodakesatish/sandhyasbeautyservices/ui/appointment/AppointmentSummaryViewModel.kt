package com.bodakesatish.sandhyasbeautyservices.ui.appointment

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentDetails
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetail
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetailWithService
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import com.bodakesatish.sandhyasbeautyservices.domain.repository.CategoryRepository
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.DeleteAppointmentDetailUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetAppointmentDetailUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetServiceDetailWithServiceByAppointmentIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

// Define a UI state class to hold all formatted data for the screen
data class AppointmentDetailsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val appointment: Appointment? = null,
//    val billing: Billing? = null,
    val services: List<ServiceDetail> = emptyList(), // Original services booked
    // You might add more pre-formatted strings here if needed
    val formattedAppointmentDateTime: String? = null,
    val formattedPaymentDate: String? = null
)

/**
 * ViewModel for managing the UI state and data related to appointment bill details.
 *
 * This ViewModel is responsible for:
 * - Fetching appointment details, including customer information and services.
 * - Managing the UI state for displaying appointment bill information.
 * - Providing formatted data for the UI, such as currency.
 *
 * @param getAppointmentDetailUseCase Use case for fetching appointment details.
 * @param appointmentRepository Repository for accessing appointment data.
 * @param serviceRepository Repository for accessing service category data.
 * @param getServiceDetailUseCase Use case for fetching service details with associated services by appointment ID.
 * @param savedStateHandle Handle for accessing and saving ViewModel state.
 */
@HiltViewModel
class AppointmentBillDetailViewModel @Inject constructor(
    private val getAppointmentDetailUseCase: GetAppointmentDetailUseCase,
    private val appointmentRepository: AppointmentRepository,
//    private val billingRepository: BillingRepository,
    private val serviceRepository: CategoryRepository,
    private val getServiceDetailUseCase: GetServiceDetailWithServiceByAppointmentIdUseCase,
    private val deleteAppointmentDetailUseCase: DeleteAppointmentDetailUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val appointmentId: Int = savedStateHandle.get<Int>("appointmentId")
        ?: throw IllegalArgumentException("Appointment ID not provided")

    private val _uiState = MutableStateFlow(AppointmentDetailsUiState())
    val uiState: StateFlow<AppointmentDetailsUiState> = _uiState.asStateFlow()

    private val tag = this.javaClass.simpleName

    // StateFlow to hold the appointment ID (for edit mode)
    private val _appointmentId = MutableStateFlow<Int>(0)
    val appointmentIdFlow: StateFlow<Int> = _appointmentId.asStateFlow()

    private val _serviceDetailList = MutableStateFlow<List<ServiceDetailWithService>>(emptyList())
    val serviceDetailList: StateFlow<List<ServiceDetailWithService>> =
        _serviceDetailList.asStateFlow()

    // To signal deletion completion (success or failure)
    private val _appointmentDeletionStatus =  MutableSharedFlow<Boolean>() // true for success, false for failure
    val appointmentDeletionStatus = _appointmentDeletionStatus.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val appointmentDetailFlow: StateFlow<AppointmentDetails?> =
        _appointmentId
            .flatMapLatest { id ->
                if (id == 0) {
                    flowOf(null) // New or invalid ID, emit null
                } else {
                    getServiceDetail()
                    getAppointmentDetailUseCase.invoke(id) // This should return Flow<AppointmentAndCustomer?>
                        .map { appointmentAndCustomer ->
                            // Map the result from the use case to your UI model (AppointmentDetails)
                            appointmentAndCustomer?.let { details ->
                                AppointmentDetails(
                                    appointment = details.appointment,
                                    customer = details.customer,
                                    serviceDetailsWithServices = details.services
                                )
                            }
                            // If appointmentAndCustomer is null, map will propagate it as null
                        }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    // Function to set the appointment ID (called from Fragment's onViewCreated)
    fun setAppointmentId(id: Int) {
        _appointmentId.value = id
    }

    fun getServiceDetail() {
        Log.d(tag, "$tag->getServiceDetail")
        viewModelScope.launch(Dispatchers.IO) {

            getServiceDetailUseCase.invoke(_appointmentId.value).collect { list ->
                _serviceDetailList.value = list
                //  _serviceItems.value = list
                Log.d(tag, "In $tag $list")
            }
        }
    }

    // --- Helper for formatting ---
    fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount) // For INR
    }

    fun deleteCurrentAppointment() {
        val appointmentIdToDelete = appointmentIdFlow.value
        if (appointmentIdToDelete != 0) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    // Assuming you have an appointmentRepository or useCase to delete
                    // Example: appointmentRepository.deleteAppointment(appointmentIdToDelete)
                    // For demonstration, let's assume it's successful
                    Log.d("ViewModel", "Attempting to delete appointment ID: $appointmentIdToDelete")
                    // Replace with your actual deletion logic:
                     val id = deleteAppointmentDetailUseCase.invoke(appointmentIdToDelete)
                    val success = true // Placeholder for actual deletion call

                    _appointmentDeletionStatus.emit(success)
                    if (success) {
                        Log.d("ViewModel", "Appointment ID: $appointmentIdToDelete deleted successfully.$id")
                    } else {
                        Log.e("ViewModel", "Failed to delete appointment ID: $appointmentIdToDelete.")
                    }
                } catch (e: Exception) {
                    Log.e("ViewModel", "Error deleting appointment ID: $appointmentIdToDelete", e)
                    _appointmentDeletionStatus.emit(false)
                    // Optionally, emit a more detailed error state if needed
                }
            }
        } else {
            Log.w("ViewModel", "No valid appointment ID to delete.")
            // Optionally emit false if you want to signal this case too
            // viewModelScope.launch { _appointmentDeletionStatus.emit(false) }
        }
    }
}