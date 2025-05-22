package com.bodakesatish.sandhyasbeautyservices.ui.appointment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewbinding.ViewBinding
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentAppointmentEditBinding
import com.bodakesatish.sandhyasbeautyservices.databinding.ItemLayoutBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.util.AppArrayAdapter
import com.bodakesatish.sandhyasbeautyservices.util.AppDatePicker
import com.bodakesatish.sandhyasbeautyservices.util.AppListPopupWindow
import com.bodakesatish.sandhyasbeautyservices.util.AppTimePicker
import com.bodakesatish.sandhyasbeautyservices.util.DateHelper
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.getValue

@AndroidEntryPoint
class AppointmentEditFragment : Fragment() {

    private var _binding: FragmentAppointmentEditBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: AppointmentEditViewModel by viewModels()

    private val args: AppointmentEditFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAppointmentEditBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupListeners()
        setupObservers()
        if (args.appointmentId != 0) {
            viewModel.setAppointmentId(args.appointmentId)
        } else {
            viewModel.loadCustomers()
        }
    }

    private fun setupObservers() {
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
                viewModel.currentAppointment.collectLatest { appointment ->
                    appointment.let {
                        binding.evAppointmentDate.editText?.setText(
                            DateHelper.getFormattedDate(
                                it.appointmentDate,
                                DateHelper.DATE_FORMAT_dd_MMM_yyyy
                            )
                        )
                        binding.evAppointmentTime.editText?.setText(
                            DateHelper.formatTime(it.appointmentTime)
                        )
                        binding.tilNotes.editText?.setText(it.appointmentNotes)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe for save result (success or failure)
                launch {
                    viewModel.saveResult.collectLatest { appointmentId ->
                        if (args.appointmentId != 0) {
                            showSnackBar("Appointment saved successfully")
                            navigateToPreviousScreen()
                        } else {
                            navigateToDetail(appointmentId)
                        }
                    }
                }
            }
        }
    }

    private fun navigateToPreviousScreen() {
        findNavController().popBackStack()
    }

    private fun navigateToDetail(appointmentId: Int) {
        val action = AppointmentEditFragmentDirections
            .actionCreateOrEditAppointmentFragmentToAppointmentBillDetailFragment(appointmentId)
        findNavController().navigate(action)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(
            requireView(),
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun setupViews() {
        if (args.appointmentId == 0) {
            binding.headerGeneric.tvHeader.text = getString(R.string.new_appointment_title)
        } else {
            binding.headerGeneric.tvHeader.text = getString(R.string.edit_appointment_title)
            binding.btnProceedToAddServices.text = getString(R.string.update_appointment_button)
        }
        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_back_24)
    }

    private fun setupListeners() {
        binding.headerGeneric.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.btnProceedToAddServices.setOnClickListener {
            val appointmentNotes = binding.etNotes.text.toString()
            viewModel.saveAppointment(appointmentNotes)
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

    private fun selectDatePicker() {

        AppDatePicker.showDatePicker(
            childFragmentManager,
            viewModel.currentAppointment.value?.appointmentDate ?: Date(),
            "Select Start Date",
        ) { selectedDate, formattedDate -> // Handle the selected time here
            viewModel.selectAppointmentDate(selectedDate.time)
            binding.evAppointmentDate.editText?.setText(formattedDate)
        }
    }

    private fun selectTimePicker() {

        AppTimePicker.showTimePicker(
            childFragmentManager,
            DateHelper.formatDate(Date()),
            "Select Time",
        ) { selectedTime, formattedTime -> // Handle the selected time here
            viewModel.selectAppointmentTime(selectedTime.time)
            binding.evAppointmentTime.editText?.setText(formattedTime)
        }

    }

}