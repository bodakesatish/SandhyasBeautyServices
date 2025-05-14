package com.bodakesatish.sandhyasbeautyservices.ui.appointments

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentDetails
import com.bodakesatish.sandhyasbeautyservices.domain.model.CategoriesWithServices
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.CreateNewAppointmentUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetAppointmentDetailUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetCategoriesWithServicesUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetCustomerListUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetSelectedCustomerUseCase
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.invoke
import kotlin.math.PI

@HiltViewModel
class ViewModelNewAppointment @Inject constructor(
    private val getAppointmentDetailUseCase: GetAppointmentDetailUseCase,
    private val createNewAppointmentUseCase: CreateNewAppointmentUseCase,
    private val getCustomerListUseCase: GetCustomerListUseCase,
    private val getCategoriesWithServicesUseCase: GetCategoriesWithServicesUseCase,
    private val getSelectedCustomer: GetSelectedCustomerUseCase,
    private val getSelectedServicesIds: GetServiceDetailWithServiceByAppointmentIdUseCase,
    private val getServiceDetailWithServiceByAppointmentIdUseCase: GetServiceDetailWithServiceByAppointmentIdUseCase // Use Case for service details
) : ViewModel() {

    private val tag = this.javaClass.simpleName

    // StateFlow to hold the appointment ID
    private val _appointmentId = MutableStateFlow<Int?>(null)
    val appointmentId: StateFlow<Int?> = _appointmentId.asStateFlow()

    // StateFlow to hold the list of all customers for the dropdown
    private val _customerListFlow = MutableStateFlow<List<Customer>>(emptyList())
    val customerListFlow: StateFlow<List<Customer>> = _customerListFlow.asStateFlow()

    // StateFlow to hold the list of all categories with services for the dialog
    private val _categoriesWithServicesFlow =
        MutableStateFlow<List<CategoriesWithServices>>(emptyList())
    val categoriesWithServicesFlow: StateFlow<List<CategoriesWithServices>> =
        _categoriesWithServicesFlow.asStateFlow()

    // StateFlow to hold the manually selected customer from the dropdown
    private val _selectedCustomerFlow = MutableStateFlow<Customer?>(null)
    val selectedCustomerFlow: StateFlow<Customer?> = _selectedCustomerFlow.asStateFlow()

    // StateFlow to hold the list of selected services for the RecyclerView
    private val _selectedServicesListFlow = MutableStateFlow<List<Service>>(emptyList())
    val selectedServicesListFlow: StateFlow<List<Service>> = _selectedServicesListFlow.asStateFlow()

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
                    combine(
                        getAppointmentDetailUseCase.invoke(id),
                        // Assuming you have a UseCase to get the customer by ID
                        // getCustomerByIdUseCase.invoke(appointment.customerId),
                        // Using the combined UseCase to get service details with service info
                        getServiceDetailWithServiceByAppointmentIdUseCase.invoke(id)
                    ) { appointment, serviceDetailsWithServices ->
                        if (appointment != null) {
                            // Assuming customer is part of the appointment detail
                            // If not, you would fetch it separately and combine here.
                            // For this example, assuming getAppointmentDetailUseCase also provides the customer.
                            // You might need to adjust based on your actual Use Case output.
                            // Let's assume for now appointment has a customerId and you have a way to get the customer.
                            // For simplicity in this example, we'll use a placeholder for customer.
                            val customer =
                                _customerListFlow.value.find { it.id == appointment.customer.id } // Find customer in the list

                            AppointmentDetails(
                                appointment = appointment.appointment,
                                customer = customer, // Provide the fetched customer
                                serviceDetailsWithServices = serviceDetailsWithServices
                            )
                        } else {
                            null // Appointment not found
                        }
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    // SharedFlow for one-time events like saving success/failure
    private val _saveResult = MutableSharedFlow<Boolean>()
    val saveResult: SharedFlow<Boolean> = _saveResult.asSharedFlow()

    // Mutable state for the new/edited appointment data
    // This will be updated by the Fragment as the user enters information
    var currentAppointment = Appointment(
        id = 0, // 0 indicates a new appointment
        customerId = 0,
        appointmentDate = DateHelper.formatDate(Date()), // Default to current date
        appointmentTime = Date(), // Default to current time
        totalBillAmount = 0.0,
//        category = Category(id = 0, categoryName = ""), // Placeholder
//        serviceDetails = emptyList() // List of ServiceDetail
    )

    // Mutable state for the total amount of selected services
    var selectedServicesTotalAmount: Double = 0.0
        private set // Only allow modification within the ViewModel

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
                customerList = customers
            }
        }
    }

    // Function to load all categories with services
    private fun loadCategoriesWithServices() {
        viewModelScope.launch {
            getCategoriesWithServicesUseCase.invoke().collect { categoriesWithServices ->
                _categoriesWithServicesFlow.value = categoriesWithServices
                Log.d(tag, "In $tag $categoriesWithServices")
                for (categoryWithService in categoriesWithServices) {
                    val category = categoryWithService.category
                    val categoryService = CategoryWithServiceViewItem.CategoryHeader(category)
                    categoryWithServiceList.add(categoryService)
                    for (service in categoryWithService.services) {
                        categoryWithServiceList.add(CategoryWithServiceViewItem.ServiceItem(service))
                    }
                }
                getSelectedServicesIdsOfAppointment()
            }
        }
    }

    // Function to observe appointment details and update mutable state for editing
    private fun observeAppointmentDetails() {
        viewModelScope.launch {
            appointmentDetailsFlow.collectLatest { details ->
                details?.let {
                    // Update mutable state with fetched appointment data for editing
                    currentAppointment = it.appointment
                    _selectedCustomerFlow.value = it.customer // Update selected customer Flow
                    // Update the list of selected services and calculate total
                    val selectedServices =
                        it.serviceDetailsWithServices.mapNotNull { serviceDetailWithService ->
                            Service(
                                id = serviceDetailWithService.serviceId,
                                serviceName = serviceDetailWithService.serviceName,
                                servicePrice = serviceDetailWithService.servicePrice
                            )
                        }
                    updateSelectedServicesList(selectedServices)
                    calculateSelectedServicesTotalAmount()
                }
                // If details is null (new appointment), currentAppointment remains default
            }
        }
    }


//    // Function to update the category name in the mutable appointment state
//    fun updateCategoryName(name: String) {
//        currentAppointment.category.categoryName = name
//    }

    // Function to handle customer selection from the dropdown
    fun selectCustomer(customer: Customer?) {
        _selectedCustomerFlow.value = customer
        currentAppointment.customerId = customer?.id ?: 0// Update the appointment's customer ID
        if (customer != null)
            selectedCustomer = customer

    }

    // Function to handle the list of services selected from the dialog
    fun updateSelectedServices(selectedServices: List<Service>) {
        updateSelectedServicesList(selectedServices)
        calculateSelectedServicesTotalAmount()
    }

    // Internal function to update the selected services list StateFlow
    private fun updateSelectedServicesList(services: List<Service>) {
        _selectedServicesListFlow.value = services
    }

    // Function to reset the ViewModel's state for a new appointment
    fun resetForNewAppointment() {
        Log.d(tag, "$tag->resetForNewAppointment")
        // Reset the appointment ID to indicate a new appointment
        _appointmentId.value = 0

        // Reset the mutable state for the new/edited appointment data
        currentAppointment = Appointment(
            id = 0, // 0 indicates a new appointment
            customerId = 0,
            appointmentDate = DateHelper.formatDate(Date()), // Default to current date
            appointmentTime = Date(), // Default to current time
            totalBillAmount = 0.0,
//            category = org.tensorflow.lite.support.label.Category(id = 0, categoryName = ""), // Placeholder
            //  serviceDetails = emptyList() // List of ServiceDetail
        )

        // Clear the manually selected customer
        _selectedCustomerFlow.value = null

        // Clear the list of selected services and reset the total amount
        _selectedServicesListFlow.value = emptyList()
        selectedServicesTotalAmount = 0.0
        // If you have a StateFlow for the total amount, update it here too.
    }


    // StateFlow to hold the appointment details
    private val _appointmentFlow = MutableStateFlow<Appointment?>(null)
    val appointmentFlow: StateFlow<Appointment?> = _appointmentFlow.asStateFlow()

    // Add a MutableSharedFlow to signal when data should be reloaded
    private val _reloadData = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val reloadData = _reloadData.asSharedFlow()

    private val _reloadSelectedService = MutableStateFlow<Boolean>(false)
    val reloadSelectedService: StateFlow<Boolean> = _reloadSelectedService.asStateFlow()

    // Function to calculate the total amount of selected services
    private fun calculateSelectedServicesTotalAmount() {
        selectedServicesTotalAmount = _selectedServicesListFlow.value.sumOf { it.servicePrice }
        // You might want to expose this total amount as a StateFlow as well
        // if the UI needs to observe changes to the total amount dynamically.
    }

//    // StateFlow representing the appointment details
//    @OptIn(ExperimentalCoroutinesApi::class)
//    val appointmentFlow: StateFlow<Appointment?> =
//        _appointmentId
//            .filterNotNull() // Only proceed when appointmentId is not null
//            .flatMapLatest { id ->
//                // Invoke the use case to get the appointment detail
//                getAppointmentDetailUseCase.invoke(id)
//            }
//            .stateIn(
//                scope = viewModelScope, // The coroutine scope for stateIn
//                started = SharingStarted.WhileSubscribed(5000), // When to start and stop collecting the upstream flow
//                initialValue = null // The initial value before the first emission
//            )

    /*
    Explanation
    Explanation:1.appointmentFlow: StateFlow<Appointment?> = ... .stateIn(...): We directly convert the Flow chain (_appointmentId.filterNotNull().flatMapLatest { ... }) into a StateFlow called appointmentFlow.2.scope = viewModelScope: This specifies the coroutine scope in which the upstream Flow will be collected.
    viewModelScope is the appropriate scope for operations that should be tied to the ViewModel's lifecycle.3.started = SharingStarted.WhileSubscribed(5000): This argument controls when the upstream Flow is collected and shared.•WhileSubscribed: The upstream Flow is collected only when there are active collectors (observers).•5000: This is an optional stopTimeoutMillis.
     It means that if all collectors disappear, the upstream Flow will continue to be collected for an additional 5000 milliseconds (5 seconds) before it's cancelled. This is useful to keep the data fresh for a short period even if the UI briefly goes to the background (e.g., screen rotation).4.initialValue = null: This is the initial value of the StateFlow before any value is emitted by the upstream Flow.
      In this case, we set it to null because initially, we don't have any appointment data loaded.5.Removed observeAppointmentId(): Since stateIn handles the collection of the upstream Flow and manages the StateFlow, the explicit observeAppointmentId() function is no longer needed. The collection starts automatically based on the started strategy when there are observers.6.Consider appointment variable: The appointment variable in your ViewModel might become redundant if you are fully relying on appointmentFlow to represent the current appointment state in your UI. You could potentially remove it and always get the current appointment from appointmentFlow.value. However, if you use it for other purposes wit
    * */

    var category = Category()
    val customerResponse = MutableLiveData<Boolean>(false)
    var customerList: List<Customer> = emptyList()
    var categoryWithServiceList = ArrayList<CategoryWithServiceViewItem>()
    var selectedCustomer = Customer()

    init {
        Log.d(tag, "$tag->init")
//        getCustomerList()
//        getCategoriesWithServices()
//        // Observe the _appointmentId Flow and trigger the use case
//        observeAppointmentId()
    }

    fun clearAppointmentData() {
        _appointmentId.value = null
        _appointmentFlow.value = null // If you are using a MutableStateFlow for this
        _selectedCustomerFlow.value = null
        _selectedCustomerFlow.value = null
        _reloadSelectedService.value = false
        _selectedServicesListFlow.value = emptyList()
        // Clear other relevant data
        for (service in categoryWithServiceList) {
            if (service is CategoryWithServiceViewItem.ServiceItem && service.service.isSelected) {
                service.service.isSelected = false
            }
        }
        selectedServicesTotalAmount = 0.0
        currentAppointment = Appointment(
            id = 0, // 0 indicates a new appointment
            customerId = 0,
            appointmentDate = DateHelper.formatDate(Date()), // Default to current date
            appointmentTime = Date(), // Default to current time
            totalBillAmount = 0.0,
//        category = Category(id = 0, categoryName = ""), // Placeholder
//        serviceDetails = emptyList() // List of ServiceDetail
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeAppointmentId() {
        viewModelScope.launch {
            _appointmentId
                .filterNotNull() // Only proceed when appointmentId is not null
                .flatMapLatest { id -> // Use flatMapLatest to switch to a new flow when ID changes
                    // Invoke the use case to get the appointment detail
                    getAppointmentDetailUseCase.invoke(id)
                }
                .collect { appointmentModel ->
                    Log.d(tag, "appointmentModel->$appointmentModel")
                    // Update the StateFlow with the result
                    _appointmentFlow.value = appointmentModel!!.appointment
                    // You might still need to update the 'appointment' var if it's used elsewhere
                    // Consider if this variable is truly necessary or if you can rely solely on _appointmentFlow
                    currentAppointment = appointmentModel.appointment // Handle null case if updating 'appointment'
                    _selectedCustomerFlow.value = appointmentModel.customer
                    selectedCustomer = appointmentModel.customer
                }
        }
    }


//    // Function to set the appointmentId from the Fragment
//    fun setAppointmentId(id: Int) {
//        _appointmentId.value = id
//    }

    // ... rest of your ViewModel functions (createNewAppointment, getCustomerList, getCategoriesWithServices, etc.)
    fun getSelectedServicesIds(): MutableList<Int> {
        selectedServicesTotalAmount = 0.0
        val services = mutableListOf<Int>()
        for (service in categoryWithServiceList) {
            if (service is CategoryWithServiceViewItem.ServiceItem && service.service.isSelected) {
                services.add(service.service.id)
                selectedServicesTotalAmount += service.service.servicePrice
            }
        }
        return services
    }

    fun createNewAppointment() {
        Log.d(tag, "In $tag createNewAppointmentUseCase")
        currentAppointment.paymentMode = "PENDING"
        currentAppointment.appointmentPlannedTime = Date()
        currentAppointment.appointmentCompletedTime = Date()

        val selectedServicesWithDetails = getSelectedServicesIds()
        currentAppointment.totalBillAmount = selectedServicesTotalAmount

        viewModelScope.launch(Dispatchers.IO) {
            val id = createNewAppointmentUseCase.invoke(
                selectedCustomer,
                currentAppointment,
                selectedServicesWithDetails
            )
            Log.d(tag, "In $tag $id")
            viewModelScope.launch(Dispatchers.Main) {
                customerResponse.value = true
            }
        }

    }

    fun getCustomerList() {
        Log.d(tag, "$tag->getCustomerList")
        viewModelScope.launch(Dispatchers.IO) {

            getCustomerListUseCase.invoke().collect { list ->
                //_customerList.value = list
                customerList = list
                Log.d(tag, "In $tag $list")
            }
        }
    }

    fun getCategoriesWithServices() {
        Log.d(tag, "$tag->getCategoriesWithServices")
        viewModelScope.launch(Dispatchers.IO) {
            getCategoriesWithServicesUseCase.invoke().collect { list ->
                Log.d(tag, "In $tag $list")
                for (categoryWithService in list) {
                    val category = categoryWithService.category
                    val categoryService = CategoryWithServiceViewItem.CategoryHeader(category)
                    categoryWithServiceList.add(categoryService)
                    for (service in categoryWithService.services) {
                        categoryWithServiceList.add(CategoryWithServiceViewItem.ServiceItem(service))
                    }
                }
                getSelectedServicesIdsOfAppointment()
            }
        }
    }

    fun getSelectedServicesIdsOfAppointment() {
        selectedServicesTotalAmount = 0.0
        viewModelScope.launch(Dispatchers.IO) {
            getSelectedServicesIds.invoke(currentAppointment.id).collect { list ->
                for (service in categoryWithServiceList) {
                    if (service is CategoryWithServiceViewItem.ServiceItem &&
                        list.any { it.serviceId == service.service.id }
                    ) {
                        service.service.isSelected = true
                        selectedServicesTotalAmount += service.service.servicePrice
                    } else if (service is CategoryWithServiceViewItem.ServiceItem) {
                        service.service.isSelected = false
                    }
                }
                _reloadSelectedService.value = true
            }
        }
    }

    fun getSelectedServices(): MutableList<Service> {
        selectedServicesTotalAmount = 0.0
        val services = mutableListOf<Service>()
        for (service in categoryWithServiceList) {
            if (service is CategoryWithServiceViewItem.ServiceItem && service.service.isSelected) {
                services.add(service.service)
                selectedServicesTotalAmount += service.service.servicePrice
            }
        }
        _selectedServicesListFlow.value = services
        return services
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(tag, "$tag->onCleared")
    }

    fun getAppointmentDetailById(appointmentId: Int) {
//        Log.d(tag, "$tag->getAppointmentDetailById")
//        viewModelScope.launch(Dispatchers.IO) {
//            getAppointmentDetailUseCase.invoke(appointmentId).collect { appointmentModel ->
//                Log.d(tag, "appointmentModel->$appointmentModel")
//                appointmentModel?.let {
//                    _appointmentFlow.value = appointmentModel
//                    appointment = appointmentModel
//                }
//            }
//        }
    }

    fun updateSelectedServicesWithCategoryServices() {
        selectedServicesTotalAmount = 0.0
        viewModelScope.launch(Dispatchers.IO) {
            val selectedIds = selectedServicesListFlow.value.map { it.id }.toIntArray()
            for (service in categoryWithServiceList) {
                if (service is CategoryWithServiceViewItem.ServiceItem &&
                    selectedIds.contains(service.service.id)
                ) {
                    service.service.isSelected = true
                    selectedServicesTotalAmount += service.service.servicePrice
                } else if (service is CategoryWithServiceViewItem.ServiceItem) {
                    service.service.isSelected = false
                }
            }
        }
    }
}