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
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetServiceDetailWithServiceByAppointmentIdUseCase
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

    private val _categoryWithServiceListFlow =
        MutableStateFlow<List<CategoryWithServiceViewItem>>(emptyList())
    val categoryWithServiceListFlow: StateFlow<List<CategoryWithServiceViewItem>> =
        _categoryWithServiceListFlow.asStateFlow()

    // StateFlow to hold the list of selected services(derived from _categoryWithServiceListFlow) for the RecyclerView
    // StateFlow to hold the list of selected services (derived from _categoryWithServiceListFlow)
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
        _appointmentId
            .filterNotNull() // Only proceed if appointmentId is not null
            .flatMapLatest { id ->
                if (id == 0) {
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

                            val services = appointmentAndCustomer.services

                            // Now combine the result with the service details
                            combine(
                                flowOf(appointmentAndCustomer.appointment), // Emit the appointment
                                flowOf(customer), // Emit the found customer
                                flowOf(services)
                            ) { appointment, foundCustomer, services ->
                                updateSelectedServices(services)
                                if (appointment != null && foundCustomer != null) {
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
                    }.flatMapLatest { it ?: flowOf(null) } // Flatten the nested flow
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
                    // The selectedServicesListFlow and selectedServicesTotalAmount
                    // are derived from _categoryWithServiceListFlow and will update automatically
                    // after updateCategoryServiceSelection is called.

//                    // Update the list of selected services and calculate total
//                    val selectedServices =
//                        it.serviceDetailsWithServices.map { serviceDetailWithService ->
//                            Service(
//                                id = serviceDetailWithService.serviceId,
//                                serviceName = serviceDetailWithService.serviceName,
//                                servicePrice = serviceDetailWithService.servicePrice
//                            )
//                        }
//                    updateSelectedServicesList(selectedServices)
//                    calculateSelectedServicesTotalAmount()
                }
                // If details is null (new appointment), currentAppointment remains default
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
    fun updateSelectedServicesFromDialog(selectedServices: List<Service>) {
        // Get the IDs of the services selected in the dialog
        val selectedServiceIds = selectedServices.map { it.id }.toSet()

        // Update the isSelected state in the main categoryWithServiceListFlow
        updateCategoryServiceSelection(selectedServiceIds)

        // _selectedServicesListFlow and selectedServicesTotalAmount
        // will be updated automatically because they are derived from _categoryWithServiceListFlow.
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
    private fun updateCategoryServiceSelection(selectedIds: Set<Int>) {
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


    // Function to handle the list of services selected from the dialog
//    // When updating selected services, create a new list:
//    fun updateSelectedServices(selectedServices: List<Service>) {
//        _selectedServicesListFlow.value = selectedServices
//        calculateSelectedServicesTotalAmount()
//    }

    // Function to reset the ViewModel's state for a new appointment
    fun resetForNewAppointment() {
        Log.d(tag, "$tag->resetForNewAppointment")
        // Reset the appointment ID to indicate a new appointment
        _appointmentId.value = 0

        // Reset the mutable state for the new/edited appointment data
        _currentAppointment.value = Appointment(
            id = 0, // 0 indicates a new appointment
            customerId = 0,
            appointmentDate = DateHelper.formatDate(Date()), // Default to current date
            appointmentTime = Date(), // Default to current time
            totalBillAmount = 0.0,
        )

        // Clear the manually selected customer
        _selectedCustomerFlow.value = null

        // Reset the selection state in the main categoryWithServiceListFlow
        // by setting isSelected to false for all ServiceItems.
        updateCategoryServiceSelection(emptySet())

        // The derived flows (selectedServicesListFlow and selectedServicesTotalAmount)
        // will automatically update based on the reset categoryWithServiceListFlow.
    }

    // Function to update the appointment date (called from UI)
    fun updateAppointmentDate(date: Date) {
        _currentAppointment.value = _currentAppointment.value.copy(appointmentDate = date)
    }

    // Function to update the appointment time (called from UI)
    fun updateAppointmentTime(time: Date) {
        _currentAppointment.value = _currentAppointment.value.copy(appointmentTime = time)
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

    fun clearAppointmentData() {
        _appointmentId.value = null
        _selectedCustomerFlow.value = null
        _selectedCustomerFlow.value = null
        //_selectedServicesListFlow.value = emptyList()
        // Clear other relevant data
        updateCategoryServiceSelection(emptySet())

//        selectedServicesTotalAmount = 0.0
//        currentAppointment = Appointment(
//            id = 0, // 0 indicates a new appointment
//            customerId = 0,
//            appointmentDate = DateHelper.formatDate(Date()), // Default to current date
//            appointmentTime = Date(), // Default to current time
//            totalBillAmount = 0.0,
//        )
    }

//    // ... rest of your ViewModel functions (createNewAppointment, getCustomerList, getCategoriesWithServices, etc.)
//    fun getSelectedServicesIds(): List<Int> {
//        //return _selectedServicesListFlow.value.map { it.id }
//        _selectedServicesTotalAmount.value = 0.0
//        val services = mutableListOf<Int>()
//        var totalAmount = 0.0
//        for (service in _categoryWithServiceListFlow.value) {
//            if (service is CategoryWithServiceViewItem.ServiceItem && service.service.isSelected) {
//                services.add(service.service.id)
//                totalAmount += service.service.servicePrice
//            }
//        }
//        _selectedServicesTotalAmount.value = totalAmount
//        return services
//    }

//    fun createNewAppointmentOld() {
//        Log.d(tag, "In $tag createNewAppointmentUseCase")
//        //  currentAppointment.paymentMode = "PENDING"
//        val selectedServicesWithDetails = getSelectedServicesIds()
//        // currentAppointment.totalBillAmount = selectedServicesTotalAmount
//
//        viewModelScope.launch(Dispatchers.IO) {
////            val id = createNewAppointmentUseCase.invoke(
////                selectedCustomer,
////                currentAppointment,
////                selectedServicesWithDetails
////            )
//            // Log.d(tag, "In $tag $id")
//            viewModelScope.launch(Dispatchers.Main) {
//                _saveResult.emit(true)
//            }
//        }
//    }

    // Inside your ViewModelNewAppointment class

    // Assuming these StateFlows are declared as suggested before:
    // private val _currentAppointment = MutableStateFlow(...)
    // val currentAppointment: StateFlow<Appointment> = _currentAppointment.asStateFlow()
    //
    // private val _selectedCustomerFlow = MutableStateFlow<Customer?>(null)
    // val selectedCustomerFlow: StateFlow<Customer?> = _selectedCustomerFlow.asStateFlow()
    //
    // private val _selectedServicesTotalAmount = MutableStateFlow(0.0)
    // val selectedServicesTotalAmount: StateFlow<Double> = _selectedServicesTotalAmount.asStateFlow()
    //
    // private val _selectedServicesListFlow = MutableStateFlow<List<Service>>(emptyList())
    // val selectedServicesListFlow: StateFlow<List<Service>> = _selectedServicesListFlow.asStateFlow()
    //
    // private val _saveResult = MutableSharedFlow<Boolean>()
    // val saveResult: SharedFlow<Boolean> = _saveResult.asSharedFlow()

//    fun createNewAppointment() {
//        Log.d(tag, "In $tag createNewAppointment")
//
//        // Get the current values from StateFlows
//        val appointmentToSave = _currentAppointment.value.copy(
//            // Update paymentMode and totalBillAmount based on current state
//            paymentMode = "PENDING", // Or get this from UI state if applicable
//            totalBillAmount = _selectedServicesTotalAmount.value // Use the StateFlow value for the total
//        )
//
//        val selectedCustomer = _selectedCustomerFlow.value
//        val selectedServiceIds = _selectedServicesListFlow.value.map { it.id }
//
//        // Validate necessary data before proceeding
//        if (selectedCustomer == null) {
//            Log.e(tag, "Customer is not selected, cannot create appointment.")
//            // Consider emitting a different event type or logging an error for the UI
//            // _saveResult.emit(false) could be called here as well, or a specific error flow
//            return
//        }
//
//        if (selectedServiceIds.isEmpty()) {
//            Log.e(tag, "No services selected, cannot create appointment.")
//            // Handle case where no services are selected
//            // _saveResult.emit(false) could be called here
//            return
//        }
//
//        // Launch a coroutine in the viewModelScope, using Dispatchers.IO for database operations
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                // Call the UseCase to create the new appointment
//                // The UseCase should handle database transactions and insertions
//                val newAppointmentId = createNewAppointmentUseCase.invoke(
//                    selectedCustomer,
//                    appointmentToSave,
//                    selectedServiceIds
//                )
//                Log.d(tag, "New appointment created with ID: $newAppointmentId")
//
//                // Emit success on the main dispatcher
//                viewModelScope.launch(Dispatchers.Main) {
//                    _saveResult.emit(true)
//                }
//
//            } catch (e: Exception) {
//                // Handle any exceptions during the save process
//                Log.e(tag, "Error creating new appointment", e)
//
//                // Emit failure on the main dispatcher
//                viewModelScope.launch(Dispatchers.Main) {
//                    _saveResult.emit(false)
//                }
//
//                // You might want to emit a more specific error state or message
//                // depending on your UI's needs.
//            }
//        }
//    }

//    fun getSelectedServicesIdsOfAppointment() {
//        _selectedServicesTotalAmount.value = 0.0
//        viewModelScope.launch(Dispatchers.IO) {
//            getSelectedServicesDetails.invoke(_currentAppointment.value.id).collect { list ->
//                var totalAmount = 0.0
//                for (service in _categoryWithServiceListFlow.value) {
//                    if (service is CategoryWithServiceViewItem.ServiceItem &&
//                        list.any { it.serviceId == service.service.id }
//                    ) {
//                        service.service.isSelected = true
//                        totalAmount += service.service.servicePrice
//                    } else if (service is CategoryWithServiceViewItem.ServiceItem) {
//                        service.service.isSelected = false
//                    }
//                }
//                _selectedServicesTotalAmount.value = totalAmount
//            }
//        }
//    }

//    fun getSelectedServices(): MutableList<Service> {
//        selectedServicesTotalAmount = 0.0
//        val services = mutableListOf<Service>()
//        for (service in categoryWithServiceList) {
//            if (service is CategoryWithServiceViewItem.ServiceItem && service.service.isSelected) {
//                services.add(service.service)
//                selectedServicesTotalAmount += service.service.servicePrice
//            }
//        }
//        _selectedServicesListFlow.value = services
//        return services
//    }

//    fun updateSelectedServicesWithCategoryServices() {
//        selectedServicesTotalAmount = 0.0
//        viewModelScope.launch(Dispatchers.IO) {
//            val selectedIds = selectedServicesListFlow.value.map { it.id }.toIntArray()
//            for (service in categoryWithServiceList) {
//                if (service is CategoryWithServiceViewItem.ServiceItem &&
//                    selectedIds.contains(service.service.id)
//                ) {
//                    service.service.isSelected = true
//                    selectedServicesTotalAmount += service.service.servicePrice
//                } else if (service is CategoryWithServiceViewItem.ServiceItem) {
//                    service.service.isSelected = false
//                }
//            }
//        }
//    }

    override fun onCleared() {
        super.onCleared()
        Log.i(tag, "$tag->onCleared")
    }

    fun setCategoryWithServiceListFlow(items: ArrayList<CategoryWithServiceViewItem>) {
        //_categoryWithServiceListFlow.value = items
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

}