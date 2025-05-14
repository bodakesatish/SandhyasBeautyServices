package com.bodakesatish.sandhyasbeautyservices.ui.appointments

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.CreateNewAppointmentUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetAppointmentDetailUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetCategoriesWithServicesUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetCustomerListUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetSelectedCustomerUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetSelectedServicesUseCase
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter.CategoryWithServiceViewItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ViewModelNewAppointment @Inject constructor(
    private val createNewAppointmentUseCase: CreateNewAppointmentUseCase,
    private val getCustomerListUseCase: GetCustomerListUseCase,
    private val getCategoriesWithServicesUseCase: GetCategoriesWithServicesUseCase,
    private val getAppointmentDetailUseCase: GetAppointmentDetailUseCase,
    private val getSelectedCustomer: GetSelectedCustomerUseCase,
    private val getSelectedServicesIds: GetSelectedServicesUseCase
) : ViewModel() {

    private val tag = this.javaClass.simpleName

    // StateFlow to hold the appointment details
    private val _appointmentFlow = MutableStateFlow<Appointment?>(null)
    val appointmentFlow: StateFlow<Appointment?> = _appointmentFlow.asStateFlow()

    // StateFlow to hold the appointment details
    private val _selectedCustomerFlow = MutableStateFlow<Customer?>(null)
    val selectedCustomerFlow: StateFlow<Customer?> = _selectedCustomerFlow.asStateFlow()

    // Add a MutableSharedFlow to signal when data should be reloaded
    private val _reloadData = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val reloadData = _reloadData.asSharedFlow()

    private val _reloadSelectedService = MutableStateFlow<Boolean>(false)
    val reloadSelectedService: StateFlow<Boolean> = _reloadSelectedService.asStateFlow()


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


    // MutableStateFlow to hold the appointmentId provided by the Fragment
    private val _appointmentId = MutableStateFlow<Int?>(null)

    var category = Category()
    val customerResponse = MutableLiveData<Boolean>(false)
    var customerList: List<Customer> = emptyList()
    var categoryWithServiceList = ArrayList<CategoryWithServiceViewItem>()
    var appointment = Appointment()
    var selectedCustomer = Customer()
    var selectedServicesTotalAmount = 0.0

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
        // Clear other relevant data
        for (service in categoryWithServiceList) {
            if (service is CategoryWithServiceViewItem.ServiceItem && service.service.isSelected) {
                service.service.isSelected = false
            }
        }
        selectedServicesTotalAmount = 0.0

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
                    appointment = appointmentModel.appointment // Handle null case if updating 'appointment'
                    _selectedCustomerFlow.value = appointmentModel.customer
                    selectedCustomer = appointmentModel.customer
                }
        }
    }

    private fun getSelectedCustomer() {
        viewModelScope.launch {
            getSelectedCustomer.invoke(appointment.customerId)?.let {

            }
        }
    }


    // Function to set the appointmentId from the Fragment
    fun setAppointmentId(id: Int) {
        _appointmentId.value = id
    }

    // ... rest of your ViewModel functions (createNewAppointment, getCustomerList, getCategoriesWithServices, etc.)
    fun getSelectedServicesIds(): MutableList<Int> {
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
        appointment.customerId = selectedCustomer.id
        appointment.paymentMode = "PENDING"
        appointment.totalBillAmount = selectedServicesTotalAmount
        appointment.appointmentPlannedTime = Date()
        appointment.appointmentCompletedTime = Date()

        val selectedServicesWithDetails = getSelectedServicesIds()

        viewModelScope.launch(Dispatchers.IO) {
            val id = createNewAppointmentUseCase.invoke(
                selectedCustomer,
                appointment,
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
        viewModelScope.launch(Dispatchers.IO) {
            getSelectedServicesIds.invoke(appointment.id).collect { list ->
                for (service in categoryWithServiceList) {
                    if (service is CategoryWithServiceViewItem.ServiceItem && list.contains(service.service.id)) {
                        service.service.isSelected = true
                        selectedServicesTotalAmount += service.service.servicePrice
                    } else if(service is CategoryWithServiceViewItem.ServiceItem) {
                        service.service.isSelected = false
                    }
                }
                _reloadSelectedService.value = true
            }
        }
    }

    fun getSelectedServices(): MutableList<Service> {
        val services = mutableListOf<Service>()
        for (service in categoryWithServiceList) {
            if (service is CategoryWithServiceViewItem.ServiceItem && service.service.isSelected) {
                services.add(service.service)
                selectedServicesTotalAmount += service.service.servicePrice
            }
        }
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

}