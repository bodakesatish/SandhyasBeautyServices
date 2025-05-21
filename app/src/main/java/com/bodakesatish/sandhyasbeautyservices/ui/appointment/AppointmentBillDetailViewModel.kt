package com.bodakesatish.sandhyasbeautyservices.ui.appointment

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentDetails
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetail
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import com.bodakesatish.sandhyasbeautyservices.domain.repository.CategoryRepository
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetAppointmentDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

@HiltViewModel
class AppointmentBillDetailViewModel @Inject constructor(
    private val getAppointmentDetailUseCase: GetAppointmentDetailUseCase,
    private val appointmentRepository: AppointmentRepository,
//    private val billingRepository: BillingRepository,
    private val serviceRepository: CategoryRepository,
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val appointmentDetailFlow: StateFlow<AppointmentDetails?> =
        _appointmentId
            .flatMapLatest { id ->
                if (id == 0) {
                    flowOf(null) // New or invalid ID, emit null
                } else {
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


    init {
       // loadAppointmentDetails()
    }

    // Function to set the appointment ID (called from Fragment's onViewCreated)
    fun setAppointmentId(id: Int) {
        _appointmentId.value = id
    }

    fun getAppointmentList() {
        Log.d(tag, "$tag->getCategoryList")
        viewModelScope.launch(Dispatchers.IO) {

            getAppointmentDetailUseCase.invoke(_appointmentId.value).collect {
             //   _appointmentDetail.value = it
                Log.d(tag, "In $tag $it")
            }
        }
    }

    fun loadAppointmentDetails() {
        viewModelScope.launch {
            _uiState.value = AppointmentDetailsUiState(isLoading = true) // Start loading
            try {
                val appointment = appointmentRepository.getAppointment(appointmentId)
                if (appointment == null) {
                    _uiState.value = AppointmentDetailsUiState(isLoading = false, error = "Appointment not found.")
                    return@launch
                }

//                var billing: Billing? = null
//                if (appointment.billingId != null) {
//                    billing = billingRepository.getBillingById(appointment.billingId!!)
//                }

//                val services = serviceRepository.getServicesByIds(appointment.serviceIds)

                // Example of pre-formatting dates (implement DateHelper.formatTimestampToDateTime etc.)
                // val formattedAppointmentDate = DateHelper.formatTimestampToDateTime(appointment.dateTime)
                // val formattedPaymentDt = billing?.paymentDate?.let { DateHelper.formatTimestampToDateTime(it) }


//                _uiState.value = AppointmentDetailsUiState(
//                    isLoading = false,
//                    appointment = appointment,
//                    billing = billing,
//                    services = services,
//                    // formattedAppointmentDateTime = formattedAppointmentDate,
//                    // formattedPaymentDate = formattedPaymentDt
//                )

            } catch (e: Exception) {
                _uiState.value = AppointmentDetailsUiState(isLoading = false, error = "Error loading details: ${e.message}")
            }
        }
    }
}