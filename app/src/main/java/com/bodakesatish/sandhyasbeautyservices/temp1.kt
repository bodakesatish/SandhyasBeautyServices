package com.bodakesatish.sandhyasbeautyservices

import androidx.compose.ui.semantics.setText

package com.bodakesatish.sandhyasbeautyservices.ui.appointments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentNewAppointmentBinding
import com.bodakesatish.sandhyasbeautyservices.databinding.ItemLayoutBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter.SelectedServicesAdapter
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.dialog.SelectServicesDialogFragment
import com.bodakesatish.sandhyasbeautyservices.util.AppArrayAdapter
import com.bodakesatish.sandhyasbeautyservices.util.AppDatePicker
import com.bodakesatish.sandhyasbeautyservices.util.AppListPopupWindow
import com.bodakesatish.sandhyasbeautyservices.util.AppTimePicker
import com.bodakesatish.sandhyasbeautyservices.util.DateHelper
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest // Use collectLatest for efficient collection
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class FragmentNewAppointment : Fragment() {

    private var _binding: FragmentNewAppointmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // ViewModel scoped to the Activity
    private val viewModel: ViewModelNewAppointment by viewModels(ownerProducer = { requireActivity() })

    private val tag = this.javaClass.simpleName

    private val selectedServicesAdapter = SelectedServicesAdapter()

    val args: FragmentNewAppointmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(tag, "$tag->onViewCreated")

        // Set the appointmentId from navigation arguments
        viewModel.setAppointmentId(args.appointmentId)

        setupViews()
        setupListeners()
        observeViewModel()

        // Initial data loading (ViewModel's init block should handle some of this)
        // You might still need to trigger specific actions here if not handled by Flow
        // viewModel.getCustomerList() // If not triggered by a Flow or init
        // viewModel.getCategoriesWithServices() // If not triggered by a Flow or init
    }

    private fun setupViews() {
        binding.headerGeneric.tvHeader.setText(if (args.appointmentId == 0) "New Appointment" else "Edit Appointment")
        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_back_24)
        binding.rvSelectedServiceList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSelectedServiceList.adapter = selectedServicesAdapter
    }

    private fun setupListeners() {
        binding.headerGeneric.btnBack.setOnClickListener {
            navigateToCustomerListScreen()
        }

        // Handle system back button
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToCustomerListScreen()
        }

        binding.btnNewAppointment.setOnClickListener {
            // Consider adding validation here before creating/updating
            if (validateInput()) {
                viewModel.createNewAppointment() // Or updateAppointment() based on ID
            } else {
                showSnackBar("Please fill in all required fields.")
            }
        }

        binding.btnShowService.setOnClickListener {
            showServiceDialog()
        }

        binding.evAppointmentDate.editText?.setOnClickListener {
            selectDatePicker()
        }

        binding.evAppointmentTime.editText?.setOnClickListener {
            selectTimePicker()
        }

        // Update ViewModel when input fields change using doAfterTextChanged
        binding.evCategoryName.editText?.doAfterTextChanged { editable ->
            // Update the category name in the ViewModel's state
            viewModel.updateCategoryName(editable?.toString() ?: "")
        }

        binding.evCustomerList.editText?.setOnClickListener {
            showCustomerList(it)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe the combined appointment and customer details
                viewModel.appointmentDetailsFlow.collectLatest { details ->
                    Log.d(tag, "Observed Appointment Details: $details")
                    details?.let {
                        updateUiWithAppointmentDetails(it.appointment, it.customer, it.serviceDetailsWithServices)
                    } ?: run {
                        // Handle null state, e.g., for a new appointment or data not found
                        // If args.appointmentId == 0, this is expected initially.
                        // If args.appointmentId != 0 and details is null, it means the appointment was not found.
                        if (args.appointmentId != 0) {
                            showSnackBar("Appointment not found.")
                            navigateToCustomerListScreen() // Or handle differently
                        } else {
                            // For a new appointment, clear or initialize the UI
                            clearAppointmentUi()
                        }
                    }
                }
            }
        }

        // Observe the selected customer when it's set (e.g., from the dropdown)
        // This is separate from the customer loaded with appointment details.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedCustomerFlow.collectLatest { selectedCustomer ->
                    Log.d(tag, "Observed Manually Selected Customer: $selectedCustomer")
                    // Update the customer list text field based on the manually selected customer
                    binding.evCustomerList.editText?.setText(selectedCustomer?.firstName + " " + selectedCustomer?.lastName)
                }
            }
        }

        // Observe for success message or navigation trigger after saving
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveResult.collectLatest { success ->
                    if (success) {
                        showSnackBar("Appointment saved successfully")
                        navigateToCustomerListScreen()
                    }
                    // Handle failure case if needed
                }
            }
        }

        // Observe for changes in the list of selected services in the ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedServicesListFlow.collectLatest { selectedServices ->
                    Log.d(tag, "Observed Selected Services List: $selectedServices")
                    selectedServicesAdapter.setData(selectedServices)
                    updateServicesTotalPrice(viewModel.selectedServicesTotalAmount)
                }
            }
        }

        // Observe customer list for the dropdown
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.customerListFlow.collectLatest { customerList ->
                    // No direct UI update needed here, the list is used by showCustomerList
                    Log.d(tag, "Customer list updated: ${customerList.size} customers")
                }
            }
        }

        // Observe category with services list for the dialog
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
