package com.bodakesatish.sandhyasbeautyservices.ui.appointments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.DialogAppointmentFiltersBinding
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentAppointmentsDashboardBinding
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.repository.PaymentStatus
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter.AppointmentsAdapter
import com.bodakesatish.sandhyasbeautyservices.util.DateHelper
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import kotlin.getValue

@AndroidEntryPoint
class FragmentAppointmentsRecord : Fragment() {

    private var _binding: FragmentAppointmentsDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: ViewModelAppointmentDashboard by viewModels()

    private lateinit var customerAdapter: AppointmentsAdapter // Initialize in onViewCreated

    private var isInitialLoad = true // Add this property to your fragment
    private var isProgrammaticChipUpdate = false // Flag to prevent feedback loop

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentsDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customerAdapter = AppointmentsAdapter() // Initialize adapter

        initView()
        setupQuickDateChips() // Setup for the new quick date chips
        initRecyclerView()
        initListeners()
        initObservers()
        onBackPressed()

        // Initial data load is now triggered by ViewModel's init or specific actions
        // viewModel.getAppointmentList() // Removed, as ViewModel handles initial load
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (findNavController().previousBackStackEntry == null) {
                requireActivity().finish()
            } else {
                findNavController().popBackStack()
            }
        }
    }

    private fun initView() {
        binding.headerGeneric.tvHeader.text = getString(R.string.title_list_of_appointments)
        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_menu_24)
        binding.btnShowFilters.setOnClickListener { // Assuming you add a btnShowFilters to your layout
            showFilterDialog()
        }
    }

    private fun setupQuickDateChips() {
        // Map Chip IDs to QuickDateRange values
        val chipIdToDateRangeMap = mapOf(
            R.id.chip_date_today to QuickDateRange.TODAY,
            R.id.chip_date_tomorrow to QuickDateRange.TOMORROW,
            R.id.chip_date_this_week to QuickDateRange.THIS_WEEK,
            R.id.chip_date_all_time to QuickDateRange.ALL_TIME
            // No chip for CUSTOM, it's a state
        )

        binding.chipGroupQuickDates.setOnCheckedStateChangeListener { group, checkedIds ->
            if (isProgrammaticChipUpdate) return@setOnCheckedStateChangeListener

            if (checkedIds.isNotEmpty()) {
                val checkedChipId = checkedIds.first() // singleSelection is true
                chipIdToDateRangeMap[checkedChipId]?.let { selectedRange ->
                    Log.d("FragmentAppointments", "Quick Date Chip selected: $selectedRange")
                    viewModel.setQuickDateRange(selectedRange)
                }
            } else {
                // This case should ideally not happen if selectionRequired is true
                // and a default is checked. If it can, handle appropriately,
                // perhaps by re-checking the default (e.g., TODAY).
                Log.w("FragmentAppointments", "No quick date chip selected, defaulting to TODAY.")
                isProgrammaticChipUpdate = true
                binding.chipDateToday.isChecked = true // Re-check today
                isProgrammaticChipUpdate = false
                viewModel.setQuickDateRange(QuickDateRange.TODAY)
            }
        }
    }

    private fun initRecyclerView() {
        binding.rvCustomerList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = customerAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    private fun initListeners() {
        binding.btnNewAppointment.setOnClickListener {
            // Consider using Safe Args for navigation if you have defined arguments
            val action = FragmentAppointmentsRecordDirections.actionFragmentAppointmentDashboardToFragmentNewAppointment(0)
            findNavController().navigate(action)
        }
        customerAdapter.setOnClickListener { appointment ->
            val action = FragmentAppointmentsRecordDirections.actionFragmentAppointmentDashboardToFragmentNewAppointment(
                appointment.appointment.id
            )
            findNavController().navigate(action)
        }
        binding.headerGeneric.btnBack.setOnClickListener {
            // Example: Open drawer or navigate up. For now, let's keep it simple.
            // If it's meant to be a navigation drawer icon:
            // (requireActivity() as? MainActivity)?.openDrawer()
            // If it's a back button for a different hierarchy:
            // findNavController().popBackStack()
            // For now, let's make it finish if it's the only fragment in activity
            if (findNavController().previousBackStackEntry == null) {
                requireActivity().finish()
            } else {
                findNavController().popBackStack()
            }
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState.collect { state ->
                        Log.d("FragmentAppointments", "UI State: $state")
                        handleUiState(state)
                    }
                }
                // Observe Active Filters to update chips
                launch {
                    viewModel.activeFilters.collect { filters ->
                        Log.d("FragmentAppointments", "Active Filters: $filters")
                        updateFilterChips(filters)
                      //  updateHeaderSubtitle(filters)
                        // Check if it's the initial load and only "Today" date filter is active
                        if (isInitialLoad && filters.size == 1 && filters.containsKey(FilterType.DateRange) && filters[FilterType.DateRange] == "Today") {
                            Toast.makeText(context, "Showing appointments for Today", Toast.LENGTH_SHORT).show()
                            isInitialLoad = false // Prevent showing again on config change or re-observation
                        }
                    }
                }

            }
        }
    }

    private fun handleUiState(state: AppointmentUiState) {
        binding.progressBar.isVisible = state is AppointmentUiState.Loading
        binding.rvCustomerList.isVisible = state is AppointmentUiState.Success
        binding.tvEmptyMessage.isVisible = state is AppointmentUiState.Error || state is AppointmentUiState.Empty

        when (state) {
            is AppointmentUiState.Loading -> {
                binding.tvEmptyMessage.text = "" // Clear previous messages
            }
            is AppointmentUiState.Success -> {
                customerAdapter.setData(state.appointments)
                binding.tvEmptyMessage.text = ""
                if (state.appointments.isEmpty()) { // Technically covered by Empty state, but good for robustness
                    binding.rvCustomerList.isVisible = false
                    binding.tvEmptyMessage.isVisible = true
                    binding.tvEmptyMessage.text = getString(R.string.no_appointments_found)
                }
            }
            is AppointmentUiState.Error -> {
                customerAdapter.setData(emptyList()) // Clear previous data
                binding.tvEmptyMessage.text = state.message ?: getString(R.string.error_fetching_appointments)
                // Optionally show a retry button
            }
            is AppointmentUiState.Empty -> {
                customerAdapter.setData(emptyList())
                binding.tvEmptyMessage.text = getString(R.string.no_appointments_found_for_filters)
            }
        }
    }

    private fun updateFilterChips(filters: Map<FilterType, String>) {
        binding.chipGroupFilters.removeAllViews() // Clear existing chips
        if (filters.isEmpty()) {
            binding.chipGroupFilters.isVisible = false
            return
        }
        binding.chipGroupFilters.isVisible = true
        filters.forEach { (filterType, value) ->
            val chip = Chip(context).apply {
                text = "${filterType.displayName}: $value" // e.g., "Status: SCHEDULED"
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    viewModel.removeFilter(filterType)
                }
            }
            binding.chipGroupFilters.addView(chip)
        }
    }

    private fun showFilterDialog() {
        val dialogBinding = DialogAppointmentFiltersBinding.inflate(LayoutInflater.from(context))
        val dialogView = dialogBinding.root

        // --- Date Pickers ---
        // Initialize with current filter values from ViewModel
        var selectedStartDateMillis = viewModel.currentStartDate
        var selectedEndDateMillis = viewModel.currentEndDate

        dialogBinding.etStartDate.setText(DateHelper.getFormattedDate(Date(selectedStartDateMillis)))
        dialogBinding.etEndDate.setText(DateHelper.getFormattedDate(Date(selectedEndDateMillis)))

        dialogBinding.etStartDate.setOnClickListener {
            val calendar = Calendar.getInstance().apply { timeInMillis = selectedStartDateMillis }
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth, 0, 0, 0) // Set time to start of day
                    calendar.set(Calendar.MILLISECOND, 0)
                    selectedStartDateMillis = calendar.timeInMillis
                    dialogBinding.etStartDate.setText(DateHelper.getFormattedDate(Date(selectedStartDateMillis)))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        dialogBinding.etEndDate.setOnClickListener {
            val calendar = Calendar.getInstance().apply { timeInMillis = selectedEndDateMillis }
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth, 23, 59, 59) // Set time to end of day
                    calendar.set(Calendar.MILLISECOND, 999)
                    selectedEndDateMillis = calendar.timeInMillis
                    dialogBinding.etEndDate.setText(DateHelper.getFormattedDate(Date(selectedEndDateMillis)))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // --- Customer Name ---
        dialogBinding.etCustomerName.setText(viewModel.currentCustomerNameQuery ?: "")

        // --- Setup Status Dropdown (AutoCompleteTextView) ---
        val statusItems = listOf(getString(R.string.filter_all_option)) + AppointmentStatus.entries.map { it.name }
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statusItems)
        (dialogBinding.tilAppointmentStatus.editText as? AutoCompleteTextView)?.setAdapter(statusAdapter)
        // Set current selection or "All"
        val currentStatusString = viewModel.activeFilters.value[FilterType.Status] ?: getString(R.string.filter_all_option)
        (dialogBinding.tilAppointmentStatus.editText as? AutoCompleteTextView)?.setText(currentStatusString, false)


        // --- Setup Payment Status Dropdown (AutoCompleteTextView) ---
        val paymentStatusItems = listOf(getString(R.string.filter_all_option)) + PaymentStatus.entries.map { it.name }
        val paymentAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, paymentStatusItems)
        (dialogBinding.tilPaymentStatus.editText as? AutoCompleteTextView)?.setAdapter(paymentAdapter)
        // Set current selection or "All"
        val currentPaymentStatusString = viewModel.activeFilters.value[FilterType.Payment] ?: getString(R.string.filter_all_option)
        (dialogBinding.tilPaymentStatus.editText as? AutoCompleteTextView)?.setText(currentPaymentStatusString, false)


        // --- Build and Show Dialog ---
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.title_filter_appointments))
            .setView(dialogView)
            .setNeutralButton(getString(R.string.action_clear_filters)) { dialog, _ ->
                viewModel.applyFilters(
                    // Clears by passing defaults or nulls
                    status = null,
                    paymentStatus = null,
                    customerName = null,
                    // Optionally reset dates to a default like today or all time
                    // startDate = viewModel.getDefaultStartDate(),
                    // endDate = viewModel.getDefaultEndDate(),
                )
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.action_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.action_apply)) { dialog, _ ->
                val selectedStatusString = (dialogBinding.tilAppointmentStatus.editText as? AutoCompleteTextView)?.text.toString()
                val newStatusFilter = if (selectedStatusString == getString(R.string.filter_all_option)) null else AppointmentStatus.valueOf(selectedStatusString)

                val selectedPaymentStatusString = (dialogBinding.tilPaymentStatus.editText as? AutoCompleteTextView)?.text.toString()
                val newPaymentStatusFilter = if (selectedPaymentStatusString == getString(R.string.filter_all_option)) null else PaymentStatus.valueOf(selectedPaymentStatusString)

                val customerNameQuery = dialogBinding.etCustomerName.text.toString().trim()

                // Ensure start date is not after end date
                if (selectedStartDateMillis > selectedEndDateMillis) {
                    // Swap them or show an error, for now, let's swap
                    val temp = selectedStartDateMillis
                    selectedStartDateMillis = selectedEndDateMillis
                    selectedEndDateMillis = temp
                }

                viewModel.applyFilters(
                    status = newStatusFilter,
                    paymentStatus = newPaymentStatusFilter,
                    customerName = if (customerNameQuery.isBlank()) null else customerNameQuery,
                    startDate = selectedStartDateMillis,
                    endDate = selectedEndDateMillis
                    // Add sortBy and sortOrder if you have UI for them in the dialog
                )
                dialog.dismiss()
            }
            .show()
    }

    private fun updateHeaderSubtitle(filters: Map<FilterType, String>) {
        val dateFilterValue = filters[FilterType.DateRange]
        if (dateFilterValue == "Today") {
//            binding.headerGeneric.tvHeaderSubtitle.text = "Showing for: Today" // Assuming you add tvHeaderSubtitle to header_generic.xml
//            binding.headerGeneric.tvHeaderSubtitle.isVisible = true
        } else if (dateFilterValue != null) {
//            binding.headerGeneric.tvHeaderSubtitle.text = "Date: $dateFilterValue"
//            binding.headerGeneric.tvHeaderSubtitle.isVisible = true
        }
        else {
//            binding.headerGeneric.tvHeaderSubtitle.isVisible = false
        }
    }


    // Called when the fragment's view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Important to prevent memory leaks
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAppointmentList()
    }

}