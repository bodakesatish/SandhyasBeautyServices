package com.bodakesatish.sandhyasbeautyservices.ui.appointment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.CategoriesWithServices
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetail
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.AddSelectedServicesUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetCategoriesWithServicesUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetSelectedServicesUseCase
import com.bodakesatish.sandhyasbeautyservices.ui.appointment.adapter.CategoryWithServiceViewItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.map

@HiltViewModel
class AddOrEditServicesToAppointmentViewModel @Inject constructor(
    private val getCategoriesWithServicesUseCase: GetCategoriesWithServicesUseCase,
    private val getSelectedServicesUseCase: GetSelectedServicesUseCase,
    private val addSelectedServicesUseCase: AddSelectedServicesUseCase
) : ViewModel() {

    private val tag = this.javaClass.simpleName

    // Input: Current Appointment ID (null if new appointment)
    private val _appointmentId = MutableStateFlow<Int?>(null)

    // Output: The combined list for the UI
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

    // Example for loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // SharedFlow for one-time events like saving success/failure
    private val _saveResult = MutableSharedFlow<Boolean>()
    val saveResult: SharedFlow<Boolean> = _saveResult.asSharedFlow()


    // Internal flow to manage loading and combining
    // This will be collected in init {} to update _categoryWithServiceListFlow
    @OptIn(ExperimentalCoroutinesApi::class)
    private val processingFlow: Flow<List<CategoryWithServiceViewItem>> =
        _appointmentId.flatMapLatest { appointmentId ->
            _isLoading.value = true // Start loading
            if (appointmentId == null || appointmentId == 0) {
                // New appointment or invalid ID: Load all categories, none selected
                getCategoriesWithServicesUseCase.invoke().map { allCategoriesWithServices ->
                    Log.d(tag, "New/Invalid ID: Loading all categories, none selected.")
                    mapToViewItems(allCategoriesWithServices, emptySet())
                }
            } else {
                // Existing appointment: Fetch selected, then all, then combine
                Log.d(tag, "Existing ID ($appointmentId): Fetching selected services first.")
                getSelectedServicesUseCase.invoke(appointmentId)
                    .flatMapLatest { selectedServiceDetails ->
                        Log.d(
                            tag,
                            "Fetched ${selectedServiceDetails.size} selected services for ID $appointmentId."
                        )
                        val selectedServiceIds = selectedServiceDetails.map { it.serviceId }.toSet()
                        // Now fetch all categories and services
                        getCategoriesWithServicesUseCase.invoke().map { allCategoriesWithServices ->
                            Log.d(
                                tag,
                                "Fetched all categories. Now combining with selected IDs: $selectedServiceIds"
                            )
                            mapToViewItems(allCategoriesWithServices, selectedServiceIds)
                        }
                    }
            }
        }
            .onCompletion {
                _isLoading.value = false
            } // Stop loading on completion (success or error)
            .catch { e ->
                Log.e(tag, "Error in processingFlow: ${e.message}", e)
                emit(emptyList()) // Emit empty list on error or handle appropriately
            }


    // This is likely what you used as _selectedServiceListFlow before.
    // It can be derived if needed, or you can populate it during the mapping.
    val selectedServiceDetailsForAppointmentFlow: StateFlow<List<ServiceDetail>> =
        _appointmentId.flatMapLatest { id ->
            if (id == null || id == 0) {
                flowOf(emptyList())
            } else {
                getSelectedServicesUseCase.invoke(id)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Collect the processingFlow to update the main UI state flow
        viewModelScope.launch {
            processingFlow.collect { combinedList ->
                Log.d(tag, "Updating _categoryWithServiceListFlow with ${combinedList.size} items.")
                _categoryWithServiceListFlow.value = combinedList
            }
        }
    }

    /**
     * Helper function to map domain models to ViewItems.
     * @param categoriesWithServices List from GetCategoriesWithServicesUseCase.
     * @param selectedIds Set of IDs for services that should be marked as selected.
     */
//    private fun mapToViewItems(
//        categoriesWithServices: List<CategoriesWithServices>, // Explicitly use domain model type
//        selectedIds: Set<Int>
//    ): List<CategoryWithServiceViewItem> {
//        val viewItems = mutableListOf<CategoryWithServiceViewItem>()
//        for (categoryWithService in categoriesWithServices) {
//            viewItems.add(CategoryWithServiceViewItem.CategoryHeader(categoryWithService.category))
//            for (service in categoryWithService.services) {
//                viewItems.add(
//                    CategoryWithServiceViewItem.ServiceItem(
//                        service.copy(isSelected = selectedIds.contains(service.id))
//                    )
//                )
//            }
//        }
//        return viewItems
//    }

    // Call this function from your Fragment/Activity when an appointment is being edited
    fun setAppointmentId(id: Int?) {
        Log.d(tag, "Setting appointmentId to: $id")
        _appointmentId.value = id
    }

    // Call this when a service's selection state is changed by the user in the UI
    fun updateServiceSelectionState(serviceId: Int, isSelected: Boolean) {
        val currentList = _categoryWithServiceListFlow.value
        val updatedList = currentList.map { item ->
            if (item is CategoryWithServiceViewItem.ServiceItem && item.service.id == serviceId) {
                item.copy(service = item.service.copy(isSelected = isSelected))
            } else {
                item
            }
        }
        _categoryWithServiceListFlow.value = updatedList
        Log.d(tag, "Service $serviceId selection updated to $isSelected by user.")
    }

    /**
     * Refreshes the selection state of the services in the [_categoryWithServiceListFlow]
     * based on a provided set of selected service IDs.
     *
     * This is useful if the source of truth for selected IDs changes externally
     * (e.g., after saving and getting a new list of selected IDs from a repository)
     * and you need to reflect these changes in the UI without a full data reload
     * if the underlying categories and services themselves haven't changed.
     *
     * Note: If the categories or services themselves might have changed,
     * it's often better to re-trigger the main `processingFlow` by calling
     * `loadServicesForAppointment(_appointmentId.value)`.
     *
     * @param newSelectedServiceIds A set containing the IDs of all services that should now be marked as selected.
     */
    internal fun refreshSelectionStates(newSelectedServiceIds: Set<Int>) {
        Log.d(
            tag,
            "refreshSelectionStates: Called with ${newSelectedServiceIds.size} selected IDs: $newSelectedServiceIds"
        )

        val currentCategorizedList = _categoryWithServiceListFlow.value

        if (currentCategorizedList.isEmpty()) {
            Log.w(
                tag,
                "refreshSelectionStates: Current categoryWithServiceListFlow is empty. Cannot refresh selection states. Consider loading data first via loadServicesForAppointment()."
            )
            // Optionally, if appointmentId is known, you could trigger a full reload:
            // if (_appointmentId.value != null) {
            //     Log.d(tag, "refreshSelectionStates: List empty, triggering full reload for appointmentId: ${_appointmentId.value}")
            //     loadServicesForAppointment(_appointmentId.value)
            // }
            return
        }

        val updatedList = currentCategorizedList.map { item ->
            if (item is CategoryWithServiceViewItem.ServiceItem) {
                val service = item.service
                val isNowSelected = newSelectedServiceIds.contains(service.id)
                if (service.isSelected != isNowSelected) {
                    // Only create new objects if the selection state actually changes
                    item.copy(service = service.copy(isSelected = isNowSelected))
                } else {
                    item // No change in selection for this item
                }
            } else {
                item // Keep CategoryHeader items as they are
            }
        }

        // Only update the flow if the list content has actually changed to avoid unnecessary recompositions
        if (updatedList != currentCategorizedList) {
            Log.d(
                tag,
                "refreshSelectionStates: Updating _categoryWithServiceListFlow due to selection changes."
            )
            _categoryWithServiceListFlow.value = updatedList
        } else {
            Log.d(
                tag,
                "refreshSelectionStates: No actual changes to selection states. _categoryWithServiceListFlow not updated."
            )
        }
    }


    private fun mapToViewItems(
        categoriesWithServices: List<CategoriesWithServices>, // Explicitly use domain model type
        selectedIds: Set<Int>
    ): List<CategoryWithServiceViewItem> {
        val viewItems = mutableListOf<CategoryWithServiceViewItem>()
        for (categoryWithService in categoriesWithServices) {
            viewItems.add(CategoryWithServiceViewItem.CategoryHeader(categoryWithService.category))
            for (service in categoryWithService.services) {
                viewItems.add(
                    CategoryWithServiceViewItem.ServiceItem(
                        service.copy(isSelected = selectedIds.contains(service.id))
                    )
                )
            }
        }
        Log.d(
            tag,
            "mapToViewItems: Mapped ${viewItems.size} view items. Selected IDs used: $selectedIds"
        )
        return viewItems
    }

    /**
     * Call this function from your Fragment/Activity when an appointment is being created or edited.
     * For new appointments, pass null or 0.
     * For existing appointments, pass the actual appointment ID.
     */
    fun loadServicesForAppointment(id: Int?) {
        Log.d(tag, "Setting appointmentId to: $id for loading services.")
        _appointmentId.value = id
    }

    /**
     * Call this when a service's selection state is changed by the user directly in the UI
     * (e.g., by tapping a checkbox).
     * This function updates the selection state optimistically in the current list.
     */
    fun toggleServiceSelection(serviceId: Int) {
        val currentList = _categoryWithServiceListFlow.value
        var wasSelected = false
        val updatedList = currentList.map { item ->
            if (item is CategoryWithServiceViewItem.ServiceItem && item.service.id == serviceId) {
                wasSelected = item.service.isSelected
                item.copy(service = item.service.copy(isSelected = !item.service.isSelected))
            } else {
                item
            }
        }
        _categoryWithServiceListFlow.value = updatedList
        Log.d(
            tag,
            "Service $serviceId selection toggled by user. Was selected: $wasSelected, New state: ${!wasSelected}"
        )

        // Optional: If you maintain a separate list of just selected ServiceDetail objects,
        // you might need to update that here as well, or ensure it's derived reactively.
        // For example, if you were updating _selectedServiceDetailsFlow:
        // val currentlySelectedDetails = updatedList
        //     .filterIsInstance<CategoryWithServiceViewItem.ServiceItem>()
        //     .filter { it.service.isSelected }
        //     .map { ServiceDetail(id = it.service.id /*, other necessary fields from Service object if possible */) } // This mapping might be tricky if Service and ServiceDetail are very different
        // _selectedServiceDetailsFlow.value = currentlySelectedDetails
    }


    // The saveSelectedServices function from your original code.
// You'll need to implement its actual logic (e.g., saving to repository).
    fun saveSelectedServices() {
        viewModelScope.launch(Dispatchers.IO)  {
            val currentAppointmentId = _appointmentId.value ?: 0
            // val servicesToSave = selectedServicesListFlow.value // This flow derives selected Service objects

            var selectedServiceIds = ArrayList<Int>()
            var totalServicesPrice = 0.0
            _categoryWithServiceListFlow.value.map {
                if (it is CategoryWithServiceViewItem.ServiceItem && it.service.isSelected) {
                    selectedServiceIds.add(it.service.id)
                    totalServicesPrice += it.service.servicePrice
                }
            }

            //val selectedServiceIds = selectedServicesListFlow.value.filter { it.isSelected }.map { it.id }
            Log.d(tag, "Saving services : $selectedServiceIds.")


            val result = addSelectedServicesUseCase.invoke(currentAppointmentId, selectedServiceIds, totalServicesPrice)
            Log.d(tag, "Save result: $result")
            _saveResult.emit(true)
            if (currentAppointmentId == null || currentAppointmentId == 0) {
                Log.d(tag, "Saving services for a new appointment.")
                // Handle saving for a new appointment (e.g., create appointment then add services)
                // Example: val newAppointmentId = appointmentRepository.createAppointment(...)
                //          servicesRepository.updateServicesForAppointment(newAppointmentId, servicesToSave)
            } else {
                Log.d(tag, "Saving services for existing appointment ID: $currentAppointmentId.")
                // Handle saving for an existing appointment
                // Example: servicesRepository.updateServicesForAppointment(currentAppointmentId, servicesToSave)
            }
            // Potentially navigate or show a confirmation message
        }
    }

}