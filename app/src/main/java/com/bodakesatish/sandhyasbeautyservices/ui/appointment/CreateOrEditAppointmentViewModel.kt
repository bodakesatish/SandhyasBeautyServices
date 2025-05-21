package com.bodakesatish.sandhyasbeautyservices.ui.appointment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.model.CustomerAppointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.PaymentMode
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.CreateNewAppointmentUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetCustomerAppointmentUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetCustomerListUseCase
import com.bodakesatish.sandhyasbeautyservices.util.DateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CreateOrEditAppointmentViewModel @Inject constructor(
    private val getAppointmentDetailUseCase: GetCustomerAppointmentUseCase,
    private val getCustomerListUseCase: GetCustomerListUseCase,
    private val createNewAppointmentUseCase: CreateNewAppointmentUseCase
) : ViewModel() {

    private val tag = this.javaClass.simpleName

    // StateFlow to hold the appointment ID (for edit mode)
    private val _appointmentId = MutableStateFlow<Int?>(0)

    val appointmentIdFlow: StateFlow<Int?> = _appointmentId.asStateFlow()

    // StateFlow to hold the list of all customers for the dropdown
    private val _customerListFlow = MutableStateFlow<List<Customer>>(emptyList())
    val customerListFlow: StateFlow<List<Customer>> = _customerListFlow.asStateFlow()

    // StateFlow to hold the manually selected customer from the dropdown
    private val _selectedCustomerFlow = MutableStateFlow<Customer?>(null)
    val selectedCustomerFlow: StateFlow<Customer?> = _selectedCustomerFlow.asStateFlow()

    // SharedFlow for one-time events like saving success/failure
    private val _saveResult = MutableSharedFlow<Int>()
    val saveResult: SharedFlow<Int> = _saveResult.asSharedFlow()


    // StateFlow to hold the current appointment data (for both new and edit mode)
    // This holds the non-service related appointment details.
    private val _currentAppointment = MutableStateFlow(
        Appointment(
            id = 0,
            customerId = 0,
            appointmentDate = DateHelper.formatDate(Date()),
            appointmentTime = Date(),
            totalBillAmount = 0.0
        )
    )
    val currentAppointment: StateFlow<Appointment?> = _currentAppointment.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val customerAppointmentFlow: StateFlow<CustomerAppointment?> =
        _appointmentId
            .flatMapLatest { id ->
                if (id == null || id == 0) {
                    flowOf(null) // New or invalid ID, emit null
                } else {
                    combine(
                        getCustomerListUseCase.invoke(),
                        getAppointmentDetailUseCase.invoke(id)
                    ) { customerList, appointmentAndCustomer ->
                        _customerListFlow.value = customerList
                        _selectedCustomerFlow.value = appointmentAndCustomer.customer
                        _currentAppointment.value = appointmentAndCustomer.appointment
                        CustomerAppointment(
                            appointment = appointmentAndCustomer.appointment,
                            customer = appointmentAndCustomer.customer
                        )
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    init {
        observeAppointmentDetails()
    }

    // Function to set the appointment ID (called from Fragment's onViewCreated)
    fun setAppointmentId(id: Int) {
        _appointmentId.value = id
    }

    // Function to handle customer selection from the dropdown
    fun selectCustomer(customer: Customer) {
        _selectedCustomerFlow.value = customer
        // Update the appointment's customer ID in the currentAppointment StateFlow
        _currentAppointment.value = _currentAppointment.value.copy(customerId = customer.id)
    }

    fun selectAppointmentDate(date: Date) {
        _currentAppointment.value = _currentAppointment.value.copy(appointmentDate = date)!!
    }

    fun selectAppointmentTime(time: Date) {
        _currentAppointment.value = _currentAppointment.value.copy(appointmentTime = time)!!
        _currentAppointment.value.appointmentTime = time
    }

    private fun observeAppointmentDetails() {
        viewModelScope.launch {
            customerAppointmentFlow.collectLatest { details ->
                details?.let {
                    _currentAppointment.value = it.appointment
                    _selectedCustomerFlow.value = it.customer
                }
            }

        }
    }


    fun createNewAppointment(appointment: Appointment) {
    }

    fun saveAppointment() {
        Log.d(tag, "In $tag saveAppointment")

        // Get the current values from StateFlows

        // Launch a coroutine in the viewModelScope, using Dispatchers.IO for database operations
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Call the UseCase to create the new appointment
                val newAppointmentId = createNewAppointmentUseCase.invoke(
                    currentAppointment.value!!,
                    emptyList()
                )

                Log.d(tag, "New appointment created with ID: $newAppointmentId")

                // Emit success on the main dispatcher
                withContext(Dispatchers.Main) {
                    _saveResult.emit(newAppointmentId)
                }

            } catch (e: Exception) {
                // Handle any exceptions during the save process
                Log.e(tag, "Error creating new appointment", e)

                // Emit failure on the main dispatcher
                withContext(Dispatchers.Main) {
                    _saveResult.emit(0)
                }

                // TODO: Consider emitting a more specific error state or message
            }
        }
    }

    // Function to load all customers
     fun loadCustomers() {
        viewModelScope.launch {
            getCustomerListUseCase.invoke().collect { customers ->
                _customerListFlow.value = customers
            }
        }
    }



}