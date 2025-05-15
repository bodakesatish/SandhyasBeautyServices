package com.bodakesatish.sandhyasbeautyservices.ui.appointments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
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
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetailWithService
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.collections.filter

@AndroidEntryPoint
class FragmentNewAppointment : Fragment() {

    private var _binding: FragmentNewAppointmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // ViewModel scoped to the Activity (assuming it's intended to be shared if navigating back/forth)    private val viewModel: ViewModelNewAppointment by viewModels(ownerProducer = { requireActivity() })
    private val viewModel: ViewModelNewAppointment by viewModels(ownerProducer = { requireActivity() })

    private val tag = this.javaClass.simpleName

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
        viewModel.setAppointmentId(args.appointmentId)

        setupViews()
        setupListeners()
        observeViewModel()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
       // customerListPopupWindow?.dismiss() // Dismiss popup window to prevent window leaks
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
            // Consider adding validation here before creating/updating
            // if (validateInput()) {
            viewModel.createNewAppointment() // Or updateAppointment() based on ID
//            } else {
//                showSnackBar("Please fill in all required fields.")
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

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe the combined appointment and customer details for initial loading (edit mode)
                launch {
                    viewModel.appointmentDetailsFlow.filterNotNull().collectLatest { details ->
                        Log.d(tag, "Observed Appointment Details: $details")
                        details?.let {
                            // Update UI fields with initial appointment details
                            binding.evAppointmentDate.editText?.setText(
                                DateHelper.getFormattedDate(
                                    it.appointment.appointmentDate,
                                    DateHelper.DATE_FORMAT_dd_MMM_yyyy
                                )
                            )
                            binding.evAppointmentTime.editText?.setText(DateHelper.formatTime(it.appointment.appointmentTime))
                            // Update the customer field based on the loaded customer
                            it.customer?.let { customer ->
                                binding.evCustomerList.editText?.setText("${customer.firstName} ${customer.lastName}")
                                // ViewModel's selectedCustomerFlow is updated in observeAppointmentDetails in ViewModel
                            } ?: run {
                                binding.evCustomerList.editText?.setText("") // Clear customer field if not found
                            }

                            // The selected services list and total amount will be updated
                            // by observing selectedServicesListFlow and selectedServicesTotalAmount StateFlows below.

//                            updateUiWithAppointmentDetails(
//                                it.appointment,
//                                it.customer,
//                                it.serviceDetailsWithServices
//                            )
                        } ?: run {
                            // Handle null state, e.g., for a new appointment or data not found
                            if (args.appointmentId != 0) {
                                // If in edit mode but appointment not found
                                showSnackBar("Appointment not found.")
                                navigateToPreviousScreen()
                            } else {
                                // For a new appointment, the UI should be clear initially.
                                // This is handled by the default values in the ViewModel's StateFlows.
                                // No need to explicitly call clearAppointmentUi() here if StateFlows are reset.
                            }
                        }
                    }
                }

                // Observe the list of selected services for the RecyclerView
                launch {
                    viewModel.categoryWithServiceListFlow.collectLatest { allServices ->
                        Log.d(tag, "Observed All Services List: ${allServices.size}")
                        val selectedServices = allServices.filterIsInstance<CategoryWithServiceViewItem.ServiceItem>().filter { it.service.isSelected }.map { it.service }
                        Log.d(tag, "Observed Selected Services List: ${selectedServices.size}")
                        selectedServicesAdapter.setData(selectedServices)
                    }
                }

                // Observe the total amount of selected services
                launch {
                    viewModel.selectedServicesTotalAmount.collectLatest { totalAmount ->
                        Log.d(tag, "Observed Total Services Amount: $totalAmount")
                        binding.tvValueTotalBill?.setText(String.format("%.2f", totalAmount))
                    }
                }

                // Observe the manually selected customer when it's set from the dropdown
                launch {
                    viewModel.selectedCustomerFlow.collectLatest { selectedCustomer ->
                        Log.d(tag, "Observed Manually Selected Customer: $selectedCustomer")
                        // Update the customer list text field based on the manually selected customer
                        binding.evCustomerList.editText?.setText(
                            selectedCustomer?.let { "${it.firstName} ${it.lastName}" } ?: ""
                        )
                    }
                }

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

    private fun showCustomerList(anchorView: View) {
        // Use the customerListFlow from the ViewModel
        val customerList = viewModel.customerListFlow.value
        if (customerList.isNotEmpty()) {
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
                binding.evCustomerList.editText?.setText(selectedCustomer.firstName + " " + selectedCustomer.lastName)
                // Update the selected customer in the ViewModel using your dedicated function
                viewModel.selectCustomer(selectedCustomer) // Assuming you have a selectCustomer function in your ViewModel
                Toast.makeText(requireContext(), selectedCustomer.firstName, Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            showSnackBar("No customers available.")
        }

    }

    private fun showServiceDialog() {
        // Pass the current categoryWithServiceListFlow value to the dialog.
        // The dialog will update the selection state internally and return the selected services.
        val dialog = SelectServicesDialogFragment()
//        dialog.isCancelable = false
        dialog.onServicesSubmittedListener =
            object : SelectServicesDialogFragment.OnServicesSubmittedListener {
                override fun onServicesSubmitted(selectedServices: List<Service>) {
                    // When the dialog returns the selected services, update the ViewModel
                    viewModel.updateSelectedServicesFromDialog(selectedServices)
                }
            }
        dialog.show(
            childFragmentManager,
            "AddBillDialogFragment"
        ) // Or childFragmentManager if in a Fragment
    }

    private fun updateServicesTotalPrice() {
        binding.tvValueTotalBill.text = viewModel.selectedServicesTotalAmount.toString() + " Rs."
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

    private fun selectDatePicker() {

        AppDatePicker.showDatePicker(
            childFragmentManager,
            viewModel.currentAppointment.value.appointmentDate,
            "Select Start Date",
        ) { selectedDate, formattedDate -> // Handle the selected time here
            viewModel.currentAppointment.value.appointmentDate = DateHelper.formatDate(selectedDate.time)
            binding.evAppointmentDate.editText?.setText(formattedDate)
        }
    }

    override fun onStop() {
        super.onStop()
        // Consider if this is the desired behavior - it will clear data even on configuration changes
        viewModel.clearAppointmentData() // Implement this function in your ViewModel
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(tag, "$tag->onDestroy")
    }
}