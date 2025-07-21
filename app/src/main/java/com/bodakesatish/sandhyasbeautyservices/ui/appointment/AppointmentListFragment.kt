package com.bodakesatish.sandhyasbeautyservices.ui.appointment

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
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentAppointmentListBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.model.PaymentStatus
import com.bodakesatish.sandhyasbeautyservices.ui.appointment.adapter.AppointmentListAdapter
import com.bodakesatish.sandhyasbeautyservices.util.DateHelper
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.getValue

@AndroidEntryPoint
class AppointmentListFragment : Fragment() {

    private var _binding: FragmentAppointmentListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppointmentDashboardViewModel by viewModels()

    private lateinit var appointmentSummaryAdapter: AppointmentListAdapter // Initialize in onViewCreated
    private var isInitialToastShown = false // To show "Showing for Today" only once effectively
    private var isProgrammaticChipUpdate = false // Flag to prevent feedback loop
    private var isProgrammaticQuickChipUpdate = false // <--- DECLARE IT HERE

    // Map Chip IDs to QuickDateRange values - defined once
    private val quickDateChipIdToDateRangeMap by lazy {
        mapOf(
            R.id.chip_date_all_time to QuickDateRange.ALL_TIME,
            R.id.chip_date_today to QuickDateRange.TODAY,
            R.id.chip_date_tomorrow to QuickDateRange.TOMORROW,
            R.id.chip_date_this_week to QuickDateRange.THIS_WEEK
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        initView()
        setupQuickDateChipsListener() // Setup for the new quick date chips
        initListeners()
        initObservers()
        onBackPressed()
        viewModel.login()

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
        binding.headerGeneric.btnShowFilters.visibility = View.VISIBLE
        binding.headerGeneric.btnShowFilters.setOnClickListener { // Assuming you add a btnShowFilters to your layout
            showFilterDialog()
        }
    }

    private fun setupQuickDateChipsListener() {

        binding.chipGroupQuickDates.setOnCheckedStateChangeListener { group, checkedIds ->
            if (isProgrammaticChipUpdate) return@setOnCheckedStateChangeListener

            val selectedRange = if (checkedIds.isNotEmpty()) {
                quickDateChipIdToDateRangeMap[checkedIds.first()]
            } else {
                null
            }
            if (selectedRange != null) {
                Log.d("FragmentAppointments", "Quick Date Chip selected by user: $selectedRange")
                viewModel.setQuickDateRange(selectedRange)
            } else {
                Log.w("FragmentAppointments", "No quick date chip selected by user interaction.")
            }
        }
    }

    private fun setupRecyclerView() {
        appointmentSummaryAdapter = AppointmentListAdapter { clickedAppointment ->
            // Handle item click
            val action =
                AppointmentListFragmentDirections.actionFragmentAppointmentListToAppointmentBillDetail(
                    clickedAppointment.appointment.id
                )
            findNavController().navigate(action)
        }

        binding.rvCustomerList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = appointmentSummaryAdapter
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun initListeners() {
        binding.btnNewAppointment.setOnClickListener {
            // Consider using Safe Args for navigation if you have defined arguments
            val action =
                AppointmentListFragmentDirections.actionFragmentAppointmentDashboardToNavigationCreateAppointment(
                    0
                )
            findNavController().navigate(action)
        }
        binding.headerGeneric.btnBack.setOnClickListener {
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
                        updateFilterDisplayChips(filters)
                        val dateFilterValue = filters[FilterType.DateRange]
                        if (!isInitialToastShown && dateFilterValue == QuickDateRange.TODAY.displayName &&
                            filters.size == 1
                        ) { // Only show if "Today" is the sole filter
                            Toast.makeText(
                                context,
                                getString(R.string.showing_appointments_for_today),
                                Toast.LENGTH_SHORT
                            ).show()
                            isInitialToastShown = true
                        }
                    }
                }

                launch {
                    viewModel.currentQuickDateRangeSelection.collect { selectedRange ->
                        Log.d(
                            "FragmentAppointments",
                            "Observed QuickDateRangeSelection from VM: $selectedRange"
                        )
                        isProgrammaticQuickChipUpdate = true
                        val chipIdToCheck =
                            quickDateChipIdToDateRangeMap.entries.find { it.value == selectedRange }?.key

                        if (chipIdToCheck != null) {
                            // Check if the chip is already checked to prevent redundant calls if possible,
                            // though ChipGroup's check() might handle this internally.
                            if (!binding.chipGroupQuickDates.checkedChipIds.contains(chipIdToCheck)) {
                                binding.chipGroupQuickDates.check(chipIdToCheck)
                            }
                        } else {
                            // This means selectedRange is CUSTOM or not in our map
                            binding.chipGroupQuickDates.clearCheck()
                        }
                        isProgrammaticQuickChipUpdate = false
                    }
                }

            }
        }
    }

    private fun handleUiState(state: AppointmentUiState) {
        binding.rvCustomerList.isVisible = state is AppointmentUiState.Success
        binding.tvEmptyMessage.isVisible = when (state) {
            is AppointmentUiState.Error -> true
            is AppointmentUiState.Empty -> true
            is AppointmentUiState.Success -> state.appointments.isEmpty()
            is AppointmentUiState.Loading -> false // Or true if you want to show a message like "Loading..." in tvEmptyMessage
        }

        when (state) {
            is AppointmentUiState.Loading -> {
                binding.tvEmptyMessage.text = "" // Clear previous messages, progressBar is visible
            }

            is AppointmentUiState.Success -> {
                appointmentSummaryAdapter.submitList(state.appointments)
                binding.tvLabelGrandTotal.isVisible = true
                binding.tvValueGrandTotal.isVisible = true
                val grandTotal = state.appointments.sumOf { it.appointment.netTotal }
                val format: NumberFormat =
                    NumberFormat.getCurrencyInstance(Locale.getDefault()) // Or your app's specific locale
                binding.tvValueGrandTotal.text = format.format(grandTotal)
                binding.tvEmptyMessage.text = ""
                if (state.appointments.isEmpty()) { // Technically covered by Empty state, but good for robustness
                    binding.tvEmptyMessage.isVisible = true
                    Log.d("FragmentAppointments", "Success state, but no appointments found.")
                    binding.tvEmptyMessage.text = getString(R.string.no_appointments_found)
                }
            }

            is AppointmentUiState.Error -> {
                appointmentSummaryAdapter.submitList(emptyList()) // Clear previous data from RecyclerView

                binding.tvEmptyMessage.text =
                    state.message ?: getString(R.string.error_fetching_appointments)
            }

            is AppointmentUiState.Empty -> {
                binding.tvLabelGrandTotal.isVisible = false
                binding.tvValueGrandTotal.isVisible = false
                appointmentSummaryAdapter.submitList(emptyList())
                binding.tvEmptyMessage.text = getString(R.string.no_appointments_found_for_filters)
            }
        }
    }

    private fun updateFilterDisplayChips(filters: Map<FilterType, String>) { // Renaming to match your selection, but original name was updateFilterChips
        binding.chipGroupFilters.removeAllViews() // Clear existing chips

        if (filters.isEmpty()) {
            binding.chipGroupFilters.isVisible = false // Hide if no filters
            return
        }

        binding.chipGroupFilters.isVisible = true // Show if there are filters

        // Iterate through the filters map (key: FilterType, value: String representation of filter value)
        filters.forEach { (filterType, value) ->
            // Create a new Chip for each active filter
            val chip = Chip(context).apply {
                // Set the text to show what filter is applied
                // e.g., "Status: SCHEDULED", "Payment: PAID"
                text = "${filterType.displayName}: $value"

                // Make the 'x' (close) icon visible on the chip
                isCloseIconVisible = true

                // Set a listener for when the user clicks the close icon
                setOnCloseIconClickListener {
                    viewModel.removeFilter(filterType)
                }
            }
            // Add the newly created chip to the ChipGroup in your layout
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
                    dialogBinding.etStartDate.setText(
                        DateHelper.getFormattedDate(
                            Date(
                                selectedStartDateMillis
                            )
                        )
                    )
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
                    dialogBinding.etEndDate.setText(
                        DateHelper.getFormattedDate(
                            Date(
                                selectedEndDateMillis
                            )
                        )
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // --- Customer Name ---
        dialogBinding.etCustomerName.setText(viewModel.currentCustomerNameQuery ?: "")

        // --- Setup Status Dropdown (AutoCompleteTextView) ---
        val statusItems =
            listOf(getString(R.string.filter_all_option)) + AppointmentStatus.entries.map { it.name }
        val statusAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statusItems)
        (dialogBinding.tilAppointmentStatus.editText as? AutoCompleteTextView)?.setAdapter(
            statusAdapter
        )
        // Set current selection or "All"
        val currentStatusString = viewModel.activeFilters.value[FilterType.Status]
            ?: getString(R.string.filter_all_option)
        (dialogBinding.tilAppointmentStatus.editText as? AutoCompleteTextView)?.setText(
            currentStatusString,
            false
        )

        // --- Setup Payment Status Dropdown (AutoCompleteTextView) ---
        val paymentStatusItems =
            listOf(getString(R.string.filter_all_option)) + PaymentStatus.entries.map { it.name }
        val paymentAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            paymentStatusItems
        )
        (dialogBinding.tilPaymentStatus.editText as? AutoCompleteTextView)?.setAdapter(
            paymentAdapter
        )
        // Set current selection or "All"
        val currentPaymentStatusString = viewModel.activeFilters.value[FilterType.Payment]
            ?: getString(R.string.filter_all_option)
        (dialogBinding.tilPaymentStatus.editText as? AutoCompleteTextView)?.setText(
            currentPaymentStatusString,
            false
        )

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
                )
                viewModel.setQuickDateRange(QuickDateRange.CUSTOM) // Indicate that specific dates were set or cleared
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.action_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.action_apply)) { dialog, _ ->
                val selectedStatusString =
                    (dialogBinding.tilAppointmentStatus.editText as? AutoCompleteTextView)?.text.toString()
                val newStatusFilter =
                    if (selectedStatusString == getString(R.string.filter_all_option)) null else AppointmentStatus.valueOf(
                        selectedStatusString
                    )

                val selectedPaymentStatusString =
                    (dialogBinding.tilPaymentStatus.editText as? AutoCompleteTextView)?.text.toString()
                val newPaymentStatusFilter =
                    if (selectedPaymentStatusString == getString(R.string.filter_all_option)) null else PaymentStatus.valueOf(
                        selectedPaymentStatusString
                    )

                val customerNameQuery = dialogBinding.etCustomerName.text.toString().trim()

                // Ensure start date is not after end date
                if (selectedStartDateMillis > selectedEndDateMillis) {
                    // Swap them or show an error, for now, let's swap
                    val temp = selectedStartDateMillis
                    selectedStartDateMillis = selectedEndDateMillis
                    selectedEndDateMillis = temp
                    // Optionally update the EditText fields if swapped
                    dialogBinding.etStartDate.setText(
                        DateHelper.getFormattedDate(
                            Date(
                                selectedStartDateMillis
                            )
                        )
                    )
                    dialogBinding.etEndDate.setText(
                        DateHelper.getFormattedDate(
                            Date(
                                selectedEndDateMillis
                            )
                        )
                    )
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

    // Called when the fragment's view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Important to prevent memory leaks
    }

    override fun onResume() {
        super.onResume()
    }

}