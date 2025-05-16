package com.bodakesatish.sandhyasbeautyservices.ui.appointments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
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
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter.CategoryWithServiceViewItem
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter.SelectedServicesAdapter
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.dialog.SelectServicesDialogFragment
import com.bodakesatish.sandhyasbeautyservices.util.AppArrayAdapter
import com.bodakesatish.sandhyasbeautyservices.util.AppDatePicker
import com.bodakesatish.sandhyasbeautyservices.util.AppListPopupWindow
import com.bodakesatish.sandhyasbeautyservices.util.AppTimePicker
import com.bodakesatish.sandhyasbeautyservices.util.DateHelper
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.collections.filter

@AndroidEntryPoint
class FragmentNewAppointment : Fragment() {

    private var _binding: FragmentNewAppointmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // ViewModel scoped to the Activity (assuming it's intended to be shared if navigating back/forth)
    private val viewModel: ViewModelNewAppointment by viewModels(ownerProducer = { requireActivity() })

    private val tag = "Beauty->" + this.javaClass.simpleName

    private lateinit var selectedServicesAdapter: SelectedServicesAdapter

    val args: FragmentNewAppointmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewAppointmentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the adapter here
        selectedServicesAdapter = SelectedServicesAdapter()

        // Set the appointmentId from navigation arguments
        // Only set if it's a non-zero ID (for edit mode)
        if (args.appointmentId != 0) {
            viewModel.setAppointmentId(args.appointmentId)
        }

        setupViews()
        setupListeners()
        observeViewModel()
        setupFragmentResultListeners() // Set up the listener for dialog results
    }

    private fun setupViews() {
        if (args.appointmentId == 0) {
            binding.headerGeneric.tvHeader.setText(getString(R.string.new_appointment_title))
            binding.btnNewAppointment.setText(getString(R.string.create_appointment_button))
        } else {
            binding.headerGeneric.tvHeader.setText(getString(R.string.edit_appointment_title))
            binding.btnNewAppointment.setText(getString(R.string.update_appointment_button))
        }
        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_back_24)
        binding.rvSelectedServiceList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSelectedServiceList.adapter = selectedServicesAdapter
    }

    private fun setupListeners() {
        binding.headerGeneric.btnBack.setOnClickListener {
            navigateToPreviousScreen()
        }

        // Handle system back button
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToPreviousScreen()
        }

        binding.btnNewAppointment.setOnClickListener {
            viewModel.createNewAppointment()
//            if (args.appointmentId == 0) {
//                viewModel.createNewAppointment()
//            } else {
//                viewModel.updateAppointment()
//            }
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

        binding.evCustomerList.editText?.setOnClickListener {
            showCustomerList(it)
        }

        // Remove this test button listener if it's not needed for production
        binding.btTest.setOnClickListener {
            // Example: Triggering a ViewModel update or check
            Log.d(tag, "Test button clicked")
            viewModel.updateServiceSelectionState()
        }

    }

    private fun setupFragmentResultListeners() {
        // Listen for the result from the SelectServicesDialogFragment
        setFragmentResultListener(SelectServicesDialogFragment.REQUEST_KEY_SELECTED_SERVICES) { requestKey, bundle ->
            // Ensure we handle the correct request key
            if (requestKey == SelectServicesDialogFragment.REQUEST_KEY_SELECTED_SERVICES) {
                val selectedServices =
                    bundle.getSerializable(SelectServicesDialogFragment.BUNDLE_KEY_SELECTED_SERVICES) as? ArrayList<Service>
                selectedServices?.let {
                    Log.i(tag, "Received Selected Services from Dialog: $it")
                    // Update the ViewModel with the new list of selected services
                    // This assumes you have a function in your ViewModel to handle this update
                    val selectedServicesIds = it.map { service -> service.id }.toSet()
                    viewModel.updateCategoryServiceSelection(selectedServicesIds)
                }
            }
        }
    }

    private fun observeViewModel() {


        // Observe the manually selected customer when it's set from the dropdown
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedCustomerFlow.collectLatest { selectedCustomer ->
                    Log.d(tag, "Observed Manually Selected Customer: $selectedCustomer")
                    // Update the customer list text field based on the manually selected customer
                    binding.evCustomerList.editText?.setText(
                        selectedCustomer?.let { "${it.firstName} ${it.lastName}" } ?: ""
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedServicesTotalAmount.collectLatest { totalAmount ->
                    Log.d(tag, "Observed Total Services Amount: $totalAmount")
                    binding.tvValueTotalBill?.setText(String.format("%.2f", totalAmount))
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe the list of selected services for the RecyclerView
                launch {
                    viewModel.categoryWithServiceListFlow.collectLatest { allServices ->
                        Log.d(
                            tag,
                            "Observed categoryWithServiceListFlow update. Size: ${allServices.size}"
                        )
                        // Filter for selected services and update the SelectedServicesAdapter
                        val selectedServices = allServices
                            .filterIsInstance<CategoryWithServiceViewItem.ServiceItem>()
                            .filter { it.service.isSelected }
                            .map { it.service }
                        selectedServicesAdapter.submitList(selectedServices)
                        Log.d(tag, "Selected Services for RV: ${selectedServices.size}")
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe for save result (success or failure)
                launch {
                    viewModel.saveResult.collectLatest { success ->
                        if (success) {
                            showSnackBar("Appointment saved successfully")
                            navigateToPreviousScreen()
                        } else {
                            showSnackBar("Appointment failed to save")
                        }
                    }
                }
            }
        }


        // Observe the combined appointment and customer details for initial loading (edit mode)
        // Observe the combined appointment and customer details for initial loading (edit mode)
        // Only collect this once if appointmentId is not 0
        if (args.appointmentId != 0) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        viewModel.appointmentDetailsFlow
                            .filterNotNull() // Ensure we only process non-null details
                            .collectLatest { details ->
                                Log.d(tag, "Observed Appointment Details: $details")
                                // Update UI fields with initial appointment details
                                binding.evAppointmentDate.editText?.setText(
                                    DateHelper.getFormattedDate(
                                        details.appointment.appointmentDate,
                                        DateHelper.DATE_FORMAT_dd_MMM_yyyy
                                    )
                                )
                                binding.evAppointmentTime.editText?.setText(
                                    DateHelper.formatTime(
                                        details.appointment.appointmentTime
                                    )
                                )
                                // Update the customer field based on the loaded customer
                                details.customer?.let { customer ->
                                    binding.evCustomerList.editText?.setText(customer.firstName + " " + customer.lastName)
                                }
                                // The selected services for the RecyclerView are handled by the categoryWithServiceListFlow observer
                                // because the viewModel.updateSelectedServicesFromDialog() is called from the dialog result.

                                // Update total bill amount
                                binding.tvValueTotalBill.text =
                                    "$${details.appointment.totalBillAmount}"
                            }
                    }
                }
            }
        }

    }

    private fun navigateToPreviousScreen() {
        findNavController().popBackStack()
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(
            requireView(),
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    // Function to show the customer list as a popup window
    private fun showCustomerList(anchorView: View) {
        // Get the current list from StateFlow
        val customerList = viewModel.customerListFlow.value
        if (customerList.isEmpty()) {
            Toast.makeText(requireContext(), "No customers available", Toast.LENGTH_SHORT).show()
            return
        }
        val appArrayAdapter = AppArrayAdapter(
            requireContext(),
            customerList // Use the value from the StateFlow
        ) { listBinding: ViewBinding, customer: Customer ->
            (listBinding as ItemLayoutBinding).itemNameTextView.text =
                customer.firstName + " " + customer.lastName
        }
        AppListPopupWindow.showListPopupWindow(
            anchorView,
            appArrayAdapter
        ) { position ->
            // Get the selected customer from the local customerList variable
            val selectedCustomer = customerList[position]
            // Update the selected customer in the ViewModel using your dedicated function
            viewModel.selectCustomer(selectedCustomer) // Assuming you have a selectCustomer function in your ViewModel
        }
    }

    // Function to show the service selection dialog
    private fun showServiceDialog() {
        // Get the current list of all services with their selection state from the ViewModel
        val currentServicesList = viewModel.categoryWithServiceListFlow.value
        // Create and show the dialog, passing the current list of services
        val dialog = SelectServicesDialogFragment().newInstance(currentServicesList)
        dialog.show(childFragmentManager, "SelectServicesDialog")
    }

    private fun selectDatePicker() {

        AppDatePicker.showDatePicker(
            childFragmentManager,
            viewModel.currentAppointment.value.appointmentDate,
            "Select Start Date",
        ) { selectedDate, formattedDate -> // Handle the selected time here

            viewModel.currentAppointment.value.appointmentDate =
                DateHelper.formatDate(selectedDate.time)
            binding.evAppointmentDate.editText?.setText(formattedDate)
        }
    }

    private fun selectTimePicker() {

        AppTimePicker.showTimePicker(
            childFragmentManager,
            DateHelper.formatDate(Date()),
            "Select Time",
        ) { selectedTime, formattedTime -> // Handle the selected time here
            viewModel.currentAppointment.value.appointmentTime = selectedTime.time
            binding.evAppointmentTime.editText?.setText(formattedTime)
        }

    }

    // Basic validation (can be expanded)
    private fun validateInput(): Boolean {
        return viewModel.selectedCustomerFlow.value != null &&
                viewModel.selectedServicesListFlow.value.isNotEmpty() &&
                !binding.evAppointmentDate.editText?.text.isNullOrEmpty() &&
                !binding.evAppointmentTime.editText?.text.isNullOrEmpty()
    }

    override fun onDestroy() {
        viewModel.clearData()
        super.onDestroy()
        Log.i(tag, "$tag->onDestroy")
    }

}