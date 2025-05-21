package com.bodakesatish.sandhyasbeautyservices.ui.appointment

import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.CustomerAppointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.model.PaymentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentSortBy
import com.bodakesatish.sandhyasbeautyservices.domain.repository.SortOrder
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetAppointmentListUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetFilteredAppointmentListUseCase
import com.bodakesatish.sandhyasbeautyservices.util.DateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// Define a simple sealed class for filter types to be used as keys in a map for UI chips
sealed class FilterType(val displayName: String) {
    data object Status : FilterType("Status")
    data object Payment : FilterType("Payment")
    data object CustomerName : FilterType("Customer")
    data object DateRange : FilterType("Date") // If you want to represent date range as a chip
}

// Sealed class for richer UI state representation
sealed class AppointmentUiState {
    data object Loading : AppointmentUiState()
    data class Success(val appointments: List<CustomerAppointment>) : AppointmentUiState()
    data class Error(val message: String?) : AppointmentUiState()
    data object Empty : AppointmentUiState() // Specific state for no results (not an error)
}

// Define an enum or sealed class for your QuickDateRange options
enum class QuickDateRange(val displayName: String) {
    TODAY("Today"),
    TOMORROW("Tomorrow"),
    THIS_WEEK("This Week"),
    ALL_TIME("All Time"),
    CUSTOM("Custom") // For when user picks dates from dialog
}

@HiltViewModel
class AppointmentDashboardViewModel @Inject constructor(
    private val getAppointmentListUseCase: GetAppointmentListUseCase,
    private val getFilteredAppointmentsUseCase: GetFilteredAppointmentListUseCase
) : ViewModel() {

    private val tag = this.javaClass.simpleName

    // State for UI (loading, success with data, error, empty)
    private val _uiState = MutableStateFlow<AppointmentUiState>(AppointmentUiState.Loading)
    val uiState: StateFlow<AppointmentUiState> = _uiState.asStateFlow()

    // State for active filter chips displayed in the UI
    private val _activeFilters = MutableStateFlow<Map<FilterType, String>>(emptyMap())
    val activeFilters: StateFlow<Map<FilterType, String>> = _activeFilters.asStateFlow()

    // Store the current quick date range selection, also used by Fragment to set the correct chip
    private val _currentQuickDateRangeSelection = MutableStateFlow(QuickDateRange.TODAY)
    val currentQuickDateRangeSelection: StateFlow<QuickDateRange> = _currentQuickDateRangeSelection.asStateFlow()

    private val _appointmentList = MutableStateFlow<List<CustomerAppointment>>(emptyList())
    val appointmentList: StateFlow<List<CustomerAppointment>> = _appointmentList.asStateFlow()


    // Store current filter parameters to re-apply or modify
    private var currentStatusFilter: AppointmentStatus? = null
    private var currentPaymentStatusFilter: PaymentStatus? = null
    internal var currentCustomerNameQuery: String? = null

    // Default date range (e.g., today, or you might want to make this configurable)
    internal var currentStartDate: Long = getStartOfDayMillis(Date()) // Default: Today
    internal var currentEndDate: Long = getEndOfDayMillis()     // Default: Today
    private var currentSortBy: AppointmentSortBy = AppointmentSortBy.DATE // Default sort
    private var currentSortOrder: SortOrder = SortOrder.ASCENDING      // Default sort order

    private var fetchJob: Job? = null

    init {
        Log.d(tag, "$tag->init")
        // Initial fetch will use the default "Today"
        setQuickDateRange(QuickDateRange.TODAY) // This will also call fetchAppointments and updateActiveFilterChips
    }

    fun setQuickDateRange(quickDateRange: QuickDateRange) {
        _currentQuickDateRangeSelection.value = quickDateRange // Update state for Fragment to observe        val today = Date()
        val today = Date()
        when (quickDateRange) {
            QuickDateRange.TODAY -> {
                currentStartDate = getStartOfDayMillis(today)
                currentEndDate = getEndOfDayMillis(today)
            }
            QuickDateRange.TOMORROW -> {
                val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                currentStartDate = getStartOfDayMillis(tomorrowCal.time)
                currentEndDate = getEndOfDayMillis(tomorrowCal.time)
            }
            QuickDateRange.THIS_WEEK -> {
                val calendar = Calendar.getInstance()
                calendar.firstDayOfWeek = Calendar.MONDAY // Or Sunday, depending on your locale/preference
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                currentStartDate = getStartOfDayMillis(calendar.time)

                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.add(Calendar.DAY_OF_YEAR, -1) // End of the week
                currentEndDate = getEndOfDayMillis(calendar.time)
            }
            QuickDateRange.ALL_TIME -> {
                currentStartDate = getVeryPastDateMillis()
                currentEndDate = getVeryFutureDateMillis()
            }
            QuickDateRange.CUSTOM -> {
                // Dates are already set by applyFilters or will be by the dialog.
                // The _currentQuickDateRangeSelection is set to CUSTOM if applyFilters is called with new dates.
                // No date changes here, just ensure fetch is called.
            }
        }
        // `fetchAppointments` will call `updateActiveFilterChips` before fetching.
        fetchAppointments()
    }

    fun applyFilters(
        status: AppointmentStatus? = currentStatusFilter,
        paymentStatus: PaymentStatus? = currentPaymentStatusFilter,
        customerName: String? = currentCustomerNameQuery,
        startDate: Long? = null, // Nullable: if provided, it's a custom date range
        endDate: Long? = null,
        sortBy: AppointmentSortBy = currentSortBy,
        sortOrder: SortOrder = currentSortOrder
    ) {
        currentStatusFilter = status
        currentPaymentStatusFilter = paymentStatus
        currentCustomerNameQuery = customerName?.trim() // Trim whitespace
        currentSortBy = sortBy
        currentSortOrder = sortOrder

        if (startDate != null && endDate != null) {
            currentStartDate = startDate
            currentEndDate = endDate
            // Determine if this custom range matches a predefined one
            _currentQuickDateRangeSelection.value = when {
                isToday(startDate, endDate) -> QuickDateRange.TODAY
                isTomorrow(startDate, endDate) -> QuickDateRange.TOMORROW
                isThisWeek(startDate, endDate) -> QuickDateRange.THIS_WEEK
                isAllTime(startDate, endDate) -> QuickDateRange.ALL_TIME
                else -> QuickDateRange.CUSTOM
            }
        } else {
            // If no new dates are provided, the existing quick date range selection is maintained (or should be).
            // This case might occur if filters other than date are applied from the dialog.
            // Ensure _currentQuickDateRangeSelection reflects the active currentStartDate/EndDate
            _currentQuickDateRangeSelection.value = determineQuickDateRangeFromCurrentDates()
        }
        fetchAppointments()
    }

    private fun determineQuickDateRangeFromCurrentDates(): QuickDateRange {
        return when {
            isToday(currentStartDate, currentEndDate) -> QuickDateRange.TODAY
            isTomorrow(currentStartDate, currentEndDate) -> QuickDateRange.TOMORROW
            isThisWeek(currentStartDate, currentEndDate) -> QuickDateRange.THIS_WEEK
            isAllTime(currentStartDate, currentEndDate) -> QuickDateRange.ALL_TIME
            else -> QuickDateRange.CUSTOM
        }
    }

// Ensure this is within your ViewModelAppointmentDashboard class

    private fun updateActiveFilterChips() {
        val newActiveFiltersMap = mutableMapOf<FilterType, String>()
        val currentSelection = _currentQuickDateRangeSelection.value

        // 1. Add DateRange filter text
        val dateRangeText = when (currentSelection) {
            QuickDateRange.CUSTOM -> {
                // For custom, we need to check if it coincidentally matches a predefined range
                // This makes the chip text consistent if a custom selection happens to be "Today", etc.
                when {
                    isToday(currentStartDate, currentEndDate) -> QuickDateRange.TODAY.displayName
                    isTomorrow(currentStartDate, currentEndDate) -> QuickDateRange.TOMORROW.displayName
                    isThisWeek(currentStartDate, currentEndDate) -> QuickDateRange.THIS_WEEK.displayName
                    isAllTime(currentStartDate, currentEndDate) -> QuickDateRange.ALL_TIME.displayName
                    else -> {
                        // Format custom dates nicely
                        val startDateFormatted = DateHelper.getFormattedDate(Date(currentStartDate), DateHelper.DATE_FORMAT_dd_MMM_yyyy) // Use your DateHelper
                        val endDateFormatted = DateHelper.getFormattedDate(Date(currentEndDate), DateHelper.DATE_FORMAT_dd_MMM_yyyy)
                        "$startDateFormatted - $endDateFormatted"
                    }
                }
            }
            else -> {
                // For predefined ranges, use their display name directly
                currentSelection.displayName
            }
        }
        // Add the date range text to the map, unless it's "All Time" and you prefer not to show a chip for it.
        // Most users would expect to see "All Time" if it's selected.
        newActiveFiltersMap[FilterType.DateRange] = dateRangeText


        // 2. Add other active filters
        currentStatusFilter?.let {
            newActiveFiltersMap[FilterType.Status] = it.name // Or it.displayName if you have one
        }
        currentPaymentStatusFilter?.let {
            newActiveFiltersMap[FilterType.Payment] = it.name // Or it.displayName
        }
        currentCustomerNameQuery?.takeIf { it.isNotBlank() }?.let {
            newActiveFiltersMap[FilterType.CustomerName] = "\"$it\"" // Adding quotes to indicate it's a search term
        }

        // If there's only one filter and it's "All Time" for DateRange,
        // and you want no chips to be visible in that specific case, you could clear the map.
        // However, generally, it's better to show what's active.
        // Example:
        // if (newActiveFiltersMap.size == 1 && newActiveFiltersMap[FilterType.DateRange] == QuickDateRange.ALL_TIME.displayName) {
        //     // Optionally clear if "All Time" means "no effective filter chip"
        //     // newActiveFiltersMap.clear()
        // }

        _activeFilters.value = newActiveFiltersMap
        Log.d(tag, "Updated active filter chips: $newActiveFiltersMap")
    }

    fun removeFilter(filterType: FilterType) {
        when (filterType) {
            is FilterType.Status -> currentStatusFilter = null
            is FilterType.Payment -> currentPaymentStatusFilter = null
            is FilterType.CustomerName -> currentCustomerNameQuery = null
            is FilterType.DateRange -> {
                // Reset to a very wide range or a "show all" state
                // When removing date range chip, default back to "Today"
                // or "All Time" depending on desired behavior.
                // Let's default to "Today" to match initial state.
                setQuickDateRange(QuickDateRange.TODAY)
                // Note: setQuickDateRange calls updateActiveFilterChips and getAppointmentList
                return // Exit early as setQuickDateRange handles the refresh
            }
        }
        updateActiveFilterChips()
        fetchAppointments()
    }

    // Helper for SimpleDateFormat if needed in ViewModel, or handle formatting in Fragment
    private val simpleDateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

    // Call this method to fetch/refresh appointments
    fun fetchAppointments() {
        Log.d(
            tag,
            "$tag->fetchAppointments with filters: Status=$currentStatusFilter, Payment=$currentPaymentStatusFilter, Customer=$currentCustomerNameQuery, StartDate=$currentStartDate, EndDate=$currentEndDate, SortBy=$currentSortBy, SortOrder=$currentSortOrder"
        )
        fetchJob?.cancel() // Cancel previous job if any
        _uiState.value = AppointmentUiState.Loading // Set loading state

        fetchJob = viewModelScope.launch { // viewModelScope defaults to Main, use case should use flowOn(Dispatchers.IO)
            getFilteredAppointmentsUseCase.invoke(
                startDate = currentStartDate,
                endDate = currentEndDate,
                status = currentStatusFilter,
                paymentStatus = currentPaymentStatusFilter,
                customerNameQuery = currentCustomerNameQuery,
                sortBy = currentSortBy,
                sortOrder = currentSortOrder
            ).catch { exception ->
                Log.e(tag, "Error fetching appointments: ${exception.message}", exception)
                _uiState.value = AppointmentUiState.Error(exception.localizedMessage ?: "An unexpected error occurred")
            }.collect { list ->
                if (list.isEmpty()) {
                    _uiState.value = AppointmentUiState.Empty
                    Log.d(tag, "In $tag received an empty list of appointments")
                } else {
                    _uiState.value = AppointmentUiState.Success(list)
                    Log.d(tag, "In $tag received ${list.size} appointments")
                }
            }
        }
    }

    private fun getEndOfDayMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    fun getAppointmentList() {
        Log.d(tag, "$tag->getCategoryList")
        viewModelScope.launch(Dispatchers.IO) {

            getAppointmentListUseCase.invoke().collect { list ->
                _appointmentList.value = list
                Log.d(tag, "In $tag $list")
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.i(tag , "$tag->onCleared")
    }

    // Helper functions for ViewModelAppointmentDashboard

    private fun getStartOfDayMillis(date: Date): Long {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getEndOfDayMillis(date: Date): Long {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    // Example helper methods for "all time" if you want to clear to that
    private fun getVeryPastDateMillis(): Long {
        return Calendar.getInstance().apply { set(1900, Calendar.JANUARY, 1) }.timeInMillis
    }

    private fun getVeryFutureDateMillis(): Long {
        return Calendar.getInstance().apply { set(2100, Calendar.DECEMBER, 31) }.timeInMillis
    }

    /**
     * Checks if the given start and end dates represent "Today".
     */
    fun isToday(startDateMillis: Long, endDateMillis: Long): Boolean {
        val today = Date()
        val expectedStart = getStartOfDayMillis(today)
        val expectedEnd = getEndOfDayMillis(today)
        return startDateMillis == expectedStart && endDateMillis == expectedEnd
    }

    /**
     * Checks if the given start and end dates represent "Tomorrow".
     */
    fun isTomorrow(startDateMillis: Long, endDateMillis: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to tomorrow
        val tomorrow = calendar.time
        val expectedStart = getStartOfDayMillis(tomorrow)
        val expectedEnd = getEndOfDayMillis(tomorrow)
        return startDateMillis == expectedStart && endDateMillis == expectedEnd
    }

    /**
     * Checks if the given start and end dates represent "This Week".
     * Assumes week starts on Monday and ends on Sunday (adjust firstDayOfWeek if needed).
     */
    fun isThisWeek(startDateMillis: Long, endDateMillis: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY // Or Sunday, depending on your definition
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val expectedStart = getStartOfDayMillis(calendar.time)

        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        calendar.add(Calendar.DAY_OF_YEAR, -1) // End of the current week (e.g., Sunday)
        val expectedEnd = getEndOfDayMillis(calendar.time)

        return startDateMillis == expectedStart && endDateMillis == expectedEnd
    }

    /**
     * Checks if the given start and end dates represent "All Time"
     * (i.e., a very broad range like 1900-01-01 to 2100-12-31).
     */
    fun isAllTime(startDateMillis: Long, endDateMillis: Long): Boolean {
        val expectedStart = getVeryPastDateMillis()
        val expectedEnd = getVeryFutureDateMillis()
        // It's possible the exact milliseconds won't match if the "all time" range
        // is set slightly differently. Consider a small tolerance or ensure consistency.
        return startDateMillis == expectedStart && endDateMillis == expectedEnd
    }


// Optional: If you still need a way to get all appointments without any filters
// (though typically covered by setting all filter params to null in getFilteredAppointmentsUseCase)
/*
funfetchAllAppointmentsUnfiltered() {
    currentStatusFilter = null
    currentPaymentStatusFilter = null
    currentCustomerNameQuery = null
    // Reset dates to a very wide range or handle in use case
    // currentStartDate = veryPastDate
    // currentEndDate = veryFutureDate
    updateActiveFilterChips()
    fetch*/
}