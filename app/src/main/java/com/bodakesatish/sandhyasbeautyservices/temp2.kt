//package com.bodakesatish.sandhyasbeautyservices
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.bodakesatish.sandhyasbeautyservices.domain.model.*
//import com.bodakesatish.sandhyasbeautyservices.domain.usecase.appointment.*
//import com.bodakesatish.sandhyasbeautyservices.domain.usecase.customer.GetAllCustomersUseCase
//import com.bodakesatish.sandhyasbeautyservices.domain.usecase.service.GetAllCategoriesWithServicesUseCase
//import com.bodakesatish.sandhyasbeautyservices.domain.usecase.service.GetServiceDetailWithServiceByAppointmentIdUseCase
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//import java.util.Date
//import javax.inject.Inject
//
//@HiltViewModel
//class ViewModelNewAppointment @Inject constructor(
//    private val getAppointmentDetailUseCase: GetAppointmentDetailUseCase,
//    private val createAppointmentUseCase: CreateAppointmentUseCase,
//    private val updateAppointmentUseCase: UpdateAppointmentUseCase, // Assuming you have this Use Case
//    private val getAllCustomersUseCase: GetAllCustomersUseCase,
//    private val getAllCategoriesWithServicesUseCase: GetAllCategoriesWithServicesUseCase,
//    private val getServiceDetailWithServiceByAppointmentIdUseCase: GetServiceDetailWithServiceByAppointmentIdUseCase // Use Case for service details
//) : ViewModel() {
//
//    private val tag = this.javaClass.simpleName
//
//    // StateFlow to hold the appointment ID
//    private val _appointmentId = MutableStateFlow<Int?>(null)
//    val appointmentId: StateFlow<Int?> = _appointmentId.asStateFlow()
//
//    // StateFlow to hold the list of all customers for the dropdown
//    private val _customerListFlow = MutableStateFlow<List<Customer>>(emptyList())
//    val customerListFlow: StateFlow<List<Customer>> = _customerListFlow.asStateFlow()
//
//    // StateFlow to hold the list of all categories with services for the dialog
//    private val _categoriesWithServicesFlow = MutableStateFlow<List<CategoryWithServices>>(emptyList())
//    val categoriesWithServicesFlow: StateFlow<List<CategoryWithServices>> = _categoriesWithServicesFlow.asStateFlow()
//
//    // StateFlow to hold the manually selected customer from the dropdown
//    private val _selectedCustomerFlow = MutableStateFlow<Customer?>(null)
//    val selectedCustomerFlow: StateFlow<Customer?> = _selectedCustomerFlow.asStateFlow()
//
//    // StateFlow to hold the list of selected services for the RecyclerView
//    private val _selectedServicesListFlow = MutableStateFlow<List<Service>>(emptyList())
//    val selectedServicesListFlow: StateFlow<List<Service>> = _selectedServicesListFlow.asStateFlow()
//
//    // StateFlow to hold the combined details of the appointment (for edit mode)
//    // Combines Appointment, Customer, and selected ServiceDetailsWithService
//    @OptIn(ExperimentalCoroutinesApi::class)
//    val appointmentDetailsFlow: StateFlow<AppointmentDetails?> =
//        _appointmentId
//            .filterNotNull() // Only proceed if appointmentId is not null
//            .flatMapLatest { id ->
//                if (id == 0) {
//                    // For a new appointment, emit null initially
//                    flowOf(null)
//                } else {
//                    // For an existing appointment, fetch details
//                    combine(
//                        getAppointmentDetailUseCase.invoke(id),
//                        // Assuming you have a UseCase to get the customer by ID
//                        // getCustomerByIdUseCase.invoke(appointment.customerId),
//                        // Using the combined UseCase to get service details with service info
//                        getServiceDetailWithServiceByAppointmentIdUseCase.invoke(id)
//                    ) { appointment, serviceDetailsWithServices ->
//                        if (appointment != null) {
//                            // Assuming customer is part of the appointment detail
//                            // If not, you would fetch it separately and combine here.
//                            // For this example, assuming getAppointmentDetailUseCase also provides the customer.
//                            // You might need to adjust based on your actual Use Case output.
//                            // Let's assume for now appointment has a customerId and you have a way to get the customer.
//                            // For simplicity in this example, we'll use a placeholder for customer.
//                            val customer = _customerListFlow.value.find { it.id == appointment.customerId } // Find customer in the list
//
//                            AppointmentDetails(
//                                appointment = appointment,
//                                customer = customer, // Provide the fetched customer
//                                serviceDetailsWithServices = serviceDetailsWithServices
//                            )
//                        } else {
//                            null // Appointment not found
//                        }
//                    }
//                }
//            }
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(5000),
//                initialValue = null
//            )
//
//    // SharedFlow for one-time events like saving success/failure
//    private val _saveResult = MutableSharedFlow<Boolean>()
//    val saveResult: SharedFlow<Boolean> = _saveResult.asSharedFlow()
//
//    // Mutable state for the new/edited appointment data
//    // This will be updated by the Fragment as the user enters information
//    var currentAppointment = Appointment(
//        id = 0, // 0 indicates a new appointment
//        customerId = 0,
//        appointmentDate = DateHelper.formatDate(Date()), // Default to current date
//        appointmentTime = Date().time, // Default to current time
//        totalBillAmount = 0.0,
//        category = Category(id = 0, categoryName = ""), // Placeholder
//        serviceDetails = emptyList() // List of ServiceDetail
//    )
//
//    // Mutable state for the total amount of selected services
//    var selectedServicesTotalAmount: Double = 0.0
//        private set // Only allow modification within the ViewModel
//
//    init {
//        Log.d(tag, "$tag->init")
//        // Load initial data when the ViewModel is created
//        loadCustomers()
//        loadCategoriesWithServices()
//        // Observe the appointment details Flow to update the mutable state for editing
//        observeAppointmentDetails()
//    }
//
//    // Function to set the appointment ID (called from Fragment's onViewCreated)
//    fun setAppointmentId(id: Int) {
//        _appointmentId.value = id
//    }
//
//    // Function to load all customers
//    private fun loadCustomers() {
//        viewModelScope.launch {
//            getAllCustomersUseCase.invoke().collect { customers ->
//                _customerListFlow.value = customers
//            }
//        }
//    }
//
//    // Function to load all categories with services
//    private fun loadCategoriesWithServices() {
//        viewModelScope.launch {
//            getAllCategoriesWithServicesUseCase.invoke().collect { categoriesWithServices ->
//                _categoriesWithServicesFlow.value = categoriesWithServices
//            }
//        }
//    }
//
//    // Function to observe appointment details and update mutable state for editing
//    private fun observeAppointmentDetails() {
//        viewModelScope.launch {
//            appointmentDetailsFlow.collectLatest { details ->
//                details?.let {
//                    // Update mutable state with fetched appointment data for editing
//                    currentAppointment = it.appointment
//                    _selectedCustomerFlow.value = it.customer // Update selected customer Flow
//                    // Update the list of selected services and calculate total
//                    val selectedServices = it.serviceDetailsWithServices.mapNotNull { serviceDetailWithService ->
//                        serviceDetailWithService.service // Extract the Service object
//                    }
//                    updateSelectedServicesList(selectedServices)
//                    calculateSelectedServicesTotalAmount()
//                }
//                // If details is null (new appointment), currentAppointment remains default
//            }
//        }
//    }
//
//
//    // Function to update the category name in the mutable appointment state
//    fun updateCategoryName(name: String) {
//        currentAppointment.category.categoryName = name
//    }
//
//    // Function to handle customer selection from the dropdown
//    fun selectCustomer(customer: Customer) {
//        _selectedCustomerFlow.value = customer
//        currentAppointment.customerId = customer.id // Update the appointment's customer ID
//    }
//
//    // Function to handle the list of services selected from the dialog
//    fun updateSelectedServices(selectedServices: List<Service>) {
//        updateSelectedServicesList(selectedServices)
//        calculateSelectedServicesTotalAmount()
//    }
//
//    // Internal function to update the selected services list StateFlow
//    private fun updateSelectedServicesList(services: List<Service>) {
//        _selectedServicesListFlow.value = services
//    }
//
//    // Function to calculate the total amount of selected services
//    private fun calculateSelectedServicesTotalAmount() {
//        selectedServicesTotalAmount = _selectedServicesListFlow.value.sumOf { it.price }
//// You might want to expose this total amount as a StateFlow as well
//// if the UI needs to observe changes to the total amount dynamically.