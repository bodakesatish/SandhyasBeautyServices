package com.bodakesatish.sandhyasbeautyservices.ui.appointments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentDetails
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetailWithService
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.CreateNewAppointmentUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetAppointmentDetailUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetCategoriesWithServicesUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetCustomerListUseCase
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter.CategoryWithServiceViewItem
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ViewModelNewAppointment @Inject constructor(
    private val getAppointmentDetailUseCase: GetAppointmentDetailUseCase,
    private val createNewAppointmentUseCase: CreateNewAppointmentUseCase,
    private val getCustomerListUseCase: GetCustomerListUseCase,
    private val getCategoriesWithServicesUseCase: GetCategoriesWithServicesUseCase
) : ViewModel() {

    private val tag = this.javaClass.simpleName

    // StateFlow to hold the appointment ID (for edit mode)
    private val _appointmentId = MutableStateFlow<Int?>(null)

    // StateFlow to hold the list of all customers for the dropdown
    private val _customerListFlow = MutableStateFlow<List<Customer>>(emptyList())
    val customerListFlow: StateFlow<List<Customer>> = _customerListFlow.asStateFlow()

    // StateFlow to hold the manually selected customer from the dropdown
    private val _selectedCustomerFlow = MutableStateFlow<Customer?>(null)
    val selectedCustomerFlow: StateFlow<Customer?> = _selectedCustomerFlow.asStateFlow()

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
    val currentAppointment: StateFlow<Appointment> = _currentAppointment.asStateFlow()

    // StateFlow to hold the list of all categories and services with their selection state.
    // This is the main source of truth for the service selection UI in the dialog.
    private val _categoryWithServiceListFlow =
        MutableStateFlow<List<CategoryWithServiceViewItem>>(emptyList())
    val categoryWithServiceListFlow: StateFlow<List<CategoryWithServiceViewItem>> =
        _categoryWithServiceListFlow.asStateFlow()

    // StateFlow to hold the list of *currently selected* services (derived from _categoryWithServiceListFlow)
    // This flow simplifies getting just the selected Service objects.
    val selectedServicesListFlow: StateFlow<List<Service>> =
        _categoryWithServiceListFlow.map { list ->
            list.filterIsInstance<CategoryWithServiceViewItem.ServiceItem>()
                .filter { it.service.isSelected }
                .map { it.service }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // StateFlow to hold the total amount of selected services (derived from selectedServicesListFlow)
    val selectedServicesTotalAmount: StateFlow<Double> =
        selectedServicesListFlow.map { services ->
            services.sumOf { it.servicePrice }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    // SharedFlow for one-time events like saving success/failure
    private val _saveResult = MutableSharedFlow<Boolean>()
    val saveResult: SharedFlow<Boolean> = _saveResult.asSharedFlow()

    // StateFlow to hold the combined details of the appointment (for edit mode)
    // Combines Appointment, Customer, and selected ServiceDetailsWithService
    @OptIn(ExperimentalCoroutinesApi::class)
    val appointmentDetailsFlow: StateFlow<AppointmentDetails?> =
        _appointmentId // Trigger on any change to _appointmentId, including null
            .flatMapLatest { id ->
                if (id == null || id == 0) {
                    // For a new or cleared appointment, emit null
                    // For a new appointment, emit null initially
                    flowOf(null)
                } else {
                    // For an existing appointment, fetch details
                    // Combine appointment details with customer list to find the customer
                    combine(
                        getAppointmentDetailUseCase.invoke(id),
                        _customerListFlow // Combine with the customer list Flow
                    ) { appointmentAndCustomer, customerList ->
                        if (appointmentAndCustomer != null) {
                            // Find the customer in the list based on the customerId from the appointment details
                            val customer =
                                customerList.find { it.id == appointmentAndCustomer.customer.id }

                            val servicesDetailsWithServices = appointmentAndCustomer.services

                            // Now combine the result with the service details
                            combine(
                                flowOf(appointmentAndCustomer.appointment), // Emit the appointment
                                flowOf(customer), // Emit the found customer
                                flowOf(servicesDetailsWithServices)
                            ) { appointment, foundCustomer, services ->
                                val selectedServiceIds = services.map { it.serviceId }.toSet()
                                updateCategoryServiceSelection(selectedServiceIds) // Update the main list
                                if (foundCustomer != null) {
                                    AppointmentDetails(
                                        appointment = appointment,
                                        customer = foundCustomer,
                                        serviceDetailsWithServices = services
                                    )
                                } else {
                                    null // Indicate data is incomplete// Appointment not found
                                }
                            }
                        } else {
                            flowOf(null) // Appointment not found
                        }
                    }.flatMapLatest {
                        it ?: flowOf(null)
                    } // Flatten the nested flow
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )


    init {
        Log.d(tag, "$tag->init")
        // Load initial data when the ViewModel is created
        loadCustomers()
        loadCategoriesWithServices()
        // Observe the appointment details Flow to update the mutable state for editing
        observeAppointmentDetails()
    }

    // Function to set the appointment ID (called from Fragment's onViewCreated)
    fun setAppointmentId(id: Int) {
        _appointmentId.value = id
    }

    // Function to load all customers
    private fun loadCustomers() {
        viewModelScope.launch {
            getCustomerListUseCase.invoke().collect { customers ->
                _customerListFlow.value = customers
            }
        }
    }

    // Function to load all categories with services
    private fun loadCategoriesWithServices() {
        viewModelScope.launch {
            getCategoriesWithServicesUseCase.invoke().collect { categoriesWithServices ->
                // Build the initial CategoryWithServiceViewItem list without selection state
                val initialList = mutableListOf<CategoryWithServiceViewItem>()
                for (categoryWithService in categoriesWithServices) {
                    initialList.add(CategoryWithServiceViewItem.CategoryHeader(categoryWithService.category))
                    for (service in categoryWithService.services) {
                        initialList.add(CategoryWithServiceViewItem.ServiceItem(service))
                    }
                }
                _categoryWithServiceListFlow.value = initialList
                // Don't call getSelectedServicesIdsOfAppointment() here directly
               // getSelectedServicesIdsOfAppointment()
            }
        }
    }

    // Function to observe appointment details and update mutable state for editing
    private fun observeAppointmentDetails() {
        viewModelScope.launch {
            appointmentDetailsFlow.collectLatest { details ->
                details?.let {
                    // Update mutable state with fetched appointment data for editing
                    _currentAppointment.value = it.appointment
                    _selectedCustomerFlow.value = it.customer // Update selected customer Flow

                    // Update the list of selected services in the main categoryWithServiceListFlow
                    // and also update the _selectedServicesListFlow and total amount.
                    val selectedServiceIds = it.serviceDetailsWithServices.map { serviceDetailWithService ->
                        serviceDetailWithService.serviceId
                    }.toSet()

                    updateCategoryServiceSelection(selectedServiceIds)
                }
            }
        }
    }

    // Function to handle customer selection from the dropdown
    fun selectCustomer(customer: Customer?) {
        _selectedCustomerFlow.value = customer
        // Update the appointment's customer ID in the currentAppointment StateFlow
        _currentAppointment.value = _currentAppointment.value.copy(customerId = customer?.id ?: 0)
    }

    // Function to handle the list of services selected from the dialog
    // When the dialog confirms selection, update the main categoryWithServiceListFlow
    fun updateSelectedServices(selectedServices: List<ServiceDetailWithService>) {
        // Get the IDs of the services selected in the dialog
        val selectedServiceIds = selectedServices.map { it.serviceId }.toSet()

        // Update the isSelected state in the main categoryWithServiceListFlow
        updateCategoryServiceSelection(selectedServiceIds)

        // _selectedServicesListFlow and selectedServicesTotalAmount
        // will be updated automatically because they are derived from _categoryWithServiceListFlow.
    }

    // Internal function to update the selection state within categoryWithServiceListFlow
    internal fun updateCategoryServiceSelection(selectedIds: Set<Int>) {
        Log.d(tag, "ViewModel: updateCategoryServiceSelection called with selectedIds: $selectedIds")
        val updatedList = _categoryWithServiceListFlow.value.map { item ->
            if (item is CategoryWithServiceViewItem.ServiceItem) {
                val isSelected = selectedIds.contains(item.service.id)
                Log.d(tag, "ViewModel: Service ${item.service.serviceName} (ID: ${item.service.id}) isSelected: $isSelected")
                // Create a new ServiceItem with the updated isSelected state
                item.copy(service = item.service.copy(isSelected = selectedIds.contains(item.service.id)))
            } else {
                item // Keep CategoryHeader items as they are
            }
        }
        // Emit the new list to the StateFlow to trigger updates in derived flows and the UI
        Log.d(tag, "ViewModel: Emitting new _categoryWithServiceListFlow with ${updatedList.size} items.")
        _categoryWithServiceListFlow.value = updatedList
        Log.d(tag, "ViewModel: _categoryWithServiceListFlow value updated.")
    }

    // Function to create a new appointment
    fun createNewAppointment() {
        Log.d(tag, "In $tag createNewAppointment")

        // Get the current values from StateFlows
        val appointmentToSave = _currentAppointment.value.copy(
            // Update paymentMode and totalBillAmount based on current state
            paymentMode = "PENDING", // Or get this from UI state if applicable
            totalBillAmount = selectedServicesTotalAmount.value // Use the derived StateFlow value
        )

        val selectedCustomer = _selectedCustomerFlow.value
        val selectedServiceIds = selectedServicesListFlow.value.map { it.id } // Use the derived StateFlow

        // Validate necessary data before proceeding
        if (selectedCustomer == null) {
            Log.e(tag, "Customer is not selected, cannot create appointment.")
            // TODO: Consider emitting a specific error event or state for the UI
            viewModelScope.launch { _saveResult.emit(false) }
            return
        }

        if (selectedServiceIds.isEmpty()) {
            Log.e(tag, "No services selected, cannot create appointment.")
            // TODO: Consider emitting a specific error event or state for the UI
            viewModelScope.launch { _saveResult.emit(false) }
            return
        }

        // Launch a coroutine in the viewModelScope, using Dispatchers.IO for database operations
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Call the UseCase to create the new appointment
                val newAppointmentId = createNewAppointmentUseCase.invoke(
                    selectedCustomer,
                    appointmentToSave,
                    selectedServiceIds
                )

                Log.d(tag, "New appointment created with ID: $newAppointmentId")

                // Emit success on the main dispatcher
                withContext(Dispatchers.Main) {
                    _saveResult.emit(true)
                }

            } catch (e: Exception) {
                // Handle any exceptions during the save process
                Log.e(tag, "Error creating new appointment", e)

                // Emit failure on the main dispatcher
                withContext(Dispatchers.Main) {
                    _saveResult.emit(false)
                }

                // TODO: Consider emitting a more specific error state or message
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(tag, "$tag->onCleared")
    }

    fun updateServiceSelectionState() {
        val updatedList = _categoryWithServiceListFlow.value.map {  item ->
            if (item is CategoryWithServiceViewItem.CategoryHeader) {
                // If it's the service that was updated, replace it with the updatedService
                val updatedCategory = item.category.copy(categoryDescription = "${Calendar.getInstance().timeInMillis}")
                item.copy(category = updatedCategory)
            } else {
                item // Keep other items as they are
            }
        }
        _categoryWithServiceListFlow.value = updatedList
    }

    fun updateServiceSelectionState(updatedService: Service) {
        // Create a new list by mapping over the current list
        val updatedList = _categoryWithServiceListFlow.value.map { item ->
            if (item is CategoryWithServiceViewItem.ServiceItem && item.service.id == updatedService.id) {
                // If it's the service that was updated, replace it with the updatedService
                item.copy(service = updatedService)
            } else {
                item // Keep other items as they are
            }
        }
        // Emit the new list to the StateFlow
        _categoryWithServiceListFlow.value = updatedList

    }

    // **New function to clear the ViewModel's state**
    fun clearData() {
        Log.d(tag, "$tag->clearData: Clearing ViewModel data.")
        _appointmentId.value = null // Reset appointment ID
        _selectedCustomerFlow.value = null // Clear selected customer
        _currentAppointment.value = Appointment( // Reset current appointment to initial state
            id = 0,
            customerId = 0,
            appointmentDate = DateHelper.formatDate(Date()),
            appointmentTime = Date(),
            totalBillAmount = 0.0
        )
        // Reset the service list to its initial state (all services unselected)
        viewModelScope.launch {
            val currentCategorizedList = _categoryWithServiceListFlow.value
            if (currentCategorizedList.isEmpty()) {
                // If the list is somehow empty (e.g., initial load failed or cleared before),
                // we might need to reload it to get the structure.
                // Or, if you are certain loadCategoriesWithServices always populates it,
                // you might skip this and assume it's populated.
                // For safety, let's ensure it has the base structure if you want to clear selections.
                // However, simpler is to just map existing items to unselected.
                Log.d(tag, "$tag->clearData: _categoryWithServiceListFlow is empty, cannot clear selections effectively without structure.")
                // Optionally, you could call loadCategoriesWithServices() here if you want to fully reset
                // and ensure all services are present and unselected.
                // For now, we'll proceed assuming it has items if it's not empty.
            }

            val updatedList = currentCategorizedList.map { item ->
                if (item is CategoryWithServiceViewItem.ServiceItem) {
                    // Create a NEW Service instance with isSelected = false
                    val unselectedService = item.service.copy(isSelected = false)
                    // Create a NEW ServiceItem containing the NEW unselectedService
                    item.copy(service = unselectedService)
                } else {
                    item // Keep CategoryHeader items as they are
                }
            }
            _categoryWithServiceListFlow.value = updatedList // Emit the new list

            // The derived flows (selectedServicesListFlow, selectedServicesTotalAmount)
            // will now correctly update because the `Service` objects inside
            // _categoryWithServiceListFlow are new instances, triggering the .map
            // and .sumOf operations in their definitions.

            // You can log after the value is set and coroutine context switches,
            // or use collect on the derived flows if you need to be absolutely sure
            // for logging immediately after.
            Log.d(tag, "$tag->clearData: _categoryWithServiceListFlow updated. Checking derived flows...")
            Log.d(tag, "$tag->Selected Services.${selectedServicesListFlow.value}")
            Log.d(tag, "$tag->Selected Amount.${selectedServicesTotalAmount.value}")
        }
        // _saveResult doesn't need explicit clearing as it's a SharedFlow for one-time events
        // Derived flows (selectedServicesListFlow, selectedServicesTotalAmount) will update automatically
        // based on the reset _categoryWithServiceListFlow.

    }

}