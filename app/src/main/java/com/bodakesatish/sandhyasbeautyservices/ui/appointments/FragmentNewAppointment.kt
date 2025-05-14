package com.bodakesatish.sandhyasbeautyservices.ui.appointments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter.SelectedServicesAdapter
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.dialog.SelectServicesDialogFragment
import com.bodakesatish.sandhyasbeautyservices.util.AppArrayAdapter
import com.bodakesatish.sandhyasbeautyservices.util.AppDatePicker
import com.bodakesatish.sandhyasbeautyservices.util.AppListPopupWindow
import com.bodakesatish.sandhyasbeautyservices.util.AppTimePicker
import com.bodakesatish.sandhyasbeautyservices.util.DateHelper
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class FragmentNewAppointment : Fragment() {

    private var _binding: FragmentNewAppointmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: ViewModelNewAppointment by viewModels(ownerProducer = { requireActivity() })

    private val tag = this.javaClass.simpleName

    private var selectedServicesAdapter = SelectedServicesAdapter()

    val args : FragmentNewAppointmentArgs by navArgs()

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

        getNavData()
        initHeader()
        initView()
        initListeners()
        initObservers()
        initData()

        setupRecyclerView()
        setupCustomerDropdown()
        setupSaveButton()
        observeViewModel()
    }

    private fun setupRecyclerView() {
//        categoryWithServiceAdapter = CategoryWithServiceAdapter { serviceItem, isSelected ->
//            // Handle service selection change if needed
//            Log.d(tag, "Service ${serviceItem.service.serviceName} selected: $isSelected")
//            serviceItem.service.isSelected = isSelected
//        }
//        binding.recyclerViewCategoriesAndServices.adapter = categoryWithServiceAdapter
    }

    private fun setupCustomerDropdown() {
//        customerAdapter = ArrayAdapter(
//            requireContext(),
//            android.R.layout.simple_dropdown_item_1line,
//            viewModel.customerList // Use the customer list from the ViewModel
//        )
//        binding.autoCompleteTextViewCustomer.setAdapter(customerAdapter)
//
//        binding.autoCompleteTextViewCustomer.setOnItemClickListener { parent, view, position, id ->
//            val selectedCustomer = parent.getItemAtPosition(position) as Customer
//            viewModel.selectedCustomer = selectedCustomer
//            Log.d(tag, "Selected Customer: ${selectedCustomer.first_name}")
        }

    private fun setupSaveButton() {
//        binding.buttonSaveAppointment.setOnClickListener {
//            viewModel.createNewAppointment()
//        }
    }
    private fun observeViewModel() {
//    // Observe the appointment details from the ViewModel
//    lifecycleScope.launch {
//        viewModel.appointmentFlow.collect { appointment ->
//            Log.d(tag, "Observed Appointment: $appointment")
//            // Update UI with appointment details when they are loaded
//            appointment?.let {
//                // Populate UI elements with appointment data
//                // Example: binding.editTextTotalAmount.setText(it.totalBillAmount.toString())
//                // You'll need to implement the mapping from Appointment model to UI fields
//            }
//        }
//    }
//
//    // Observe the customer list and update the dropdown adapter
//    // Note: You are currently populating the adapter directly in setupCustomerDropdown
//    // Consider observing a Flow/LiveData from the ViewModel for customer list updates
//    // if the list can change dynamically after the ViewModel is created.
//    // For now, this relies on the list being loaded in the ViewModel's init.
//    // You might want to add an observer here if the list loading is asynchronous.
//
//    // Observe the category and service list and update the RecyclerView adapter
//    // Similar to the customer list, if this list loads asynchronously,
//    // you should observe a Flow/LiveData for updates.
//    // For now, this relies on the list being loaded in the ViewModel's init.
//    if (viewModel.categoryWithServiceList.isNotEmpty()) {
//        categoryWithServiceAdapter.submitList(viewModel.categoryWithServiceList)
//    }
//
//
//    // Observe the customer creation response if needed for navigation or feedback
//    viewModel.customerResponse.observe(viewLifecycleOwner) { success ->
//        if (success) {
//            // Navigate back or show a success message
//            Log.d(tag, "New appointment created successfully!")
//            findNavController().popBackStack() // Example: Navigate back
//        } else {
//            // Handle failure if necessary
//        }
//    }
    }

    private fun observeViewModel1() {
//        // Observe the appointment details from the ViewModel's StateFlow
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.appointmentFlow.collect { appointment ->
//                    Log.d(tag, "Observed Appointment: $appointment")
//                    // Update UI with appointment details when they are loaded
//                    appointment?.let {
//                        // Populate UI elements with appointment data
//                        // Example: binding.editTextTotalAmount.setText(it.totalBillAmount.toString())
//                        // You'll need to implement the mapping from Appointment model to UI fields
//                        updateUiWithAppointment(it) // Call a function to update UI
//                    } ?: run {
//                        // Handle the case where appointment is null (e.g., initial state, or not found)
//                        clearAppointmentUi() // Call a function to clear or show empty state
//                    }
//                }
//            }
//        }

        // ... other observations (customer list, category/services, etc.)
    }

//    // Example helper functions to update/clear UI
//    private fun updateUiWithAppointment(appointment: Appointment) {
//        binding.evCategoryName.editText?.setText(appointment.category.categoryName)
//        // Update other UI elements here
//    }
//
//    private fun clearAppointmentUi() {
//        binding.evCategoryName.editText?.setText("")
//        // Clear other UI elements here
//    }

    fun getNavData() {
        // Get the appointmentId from navArgs
        args.appointmentId.let {
            viewModel.appointment.id = it
        }
        // Provide the appointmentId to the ViewModel
        // This will trigger the use case execution in the ViewModel
        if (args.appointmentId != 0) { // Assuming 0 is your default value for a new appointment
            viewModel.setAppointmentId(args.appointmentId)
        }
    }

    private fun initHeader() {
        binding.headerGeneric.tvHeader.setText("New Appointment")
    }

    private fun initView() {
        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_back_24)
        binding.rvSelectedServiceList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSelectedServiceList.adapter = selectedServicesAdapter
    }

    private fun initListeners() {
        // Update ViewModel when input fields change
        binding.evCategoryName.editText?.doAfterTextChanged { editable ->
            viewModel.category.categoryName = editable?.toString() ?: ""
        }

        binding.headerGeneric.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnNewAppointment.setOnClickListener {
//            if(binding.evCategoryName.editText.toString().isEmpty()) {
//                showSnackBar("Enter Category Name")
//            } else {
                viewModel.createNewAppointment()
//            }
        }
        viewModel.customerResponse.observe(viewLifecycleOwner) {
            if(it) {
                viewModel.customerResponse.value = false
                showSnackBar("Appointment created successfully")
                navigateToCustomerListScreen()
            }
        }
        binding.headerGeneric.btnBack.setOnClickListener {
            navigateToCustomerListScreen()
        }

        // This callback will only be called when FragmentAddOrUpdateCustomer is at least Started.
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handle the back button event
            // e.g., navigate to the previous screen or pop the back stack
            navigateToCustomerListScreen()
        }

        // You can enable/disable the callback based on certain conditions
        // callback.isEnabled = someCondition

        binding.btnShowService.setOnClickListener {
            showServiceDialog()
        }

        binding.evAppointmentDate.editText?.setOnClickListener {
            selectDatePicker()
        }
        binding.evAppointmentTime.editText?.setOnClickListener {
            selectTimePicker()
        }
    }

    private fun initObservers() {
        binding.evCustomerList.editText?.setOnClickListener {
            showCustomerList(it)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appointmentFlow.collect { data ->
                    if(data != null) {
                        updateData()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedCustomerFlow.collect { data ->
                    if(data != null) {
                        binding.evCustomerList.editText?.setText(data?.firstName + " " + data?.lastName)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reloadSelectedService.collect { data ->
                    if(data) {
                        selectedServicesAdapter.setData(viewModel.getSelectedServices())
                        updateServicesTotalPrice()
                    } else {
                        selectedServicesAdapter.setData(emptyList())
                        updateServicesTotalPrice()
                    }
                }
            }
        }

    }

    private fun updateData() {
        binding.evAppointmentDate.editText?.setText(DateHelper.getFormattedDate(viewModel.appointment.appointmentDate, DateHelper.DATE_FORMAT_dd_MMM_yyyy))
        binding.evAppointmentTime.editText?.setText(DateHelper.formatTime(viewModel.appointment.appointmentTime))
    }

    private fun initData() {
        if(viewModel.appointment.id != 0) {
          //  viewModel.getSelectedServicesIdsOfAppointment()
         //   viewModel.getAppointmentDetailById(viewModel.appointment.id)
        }
//        binding.evCustomerName.editText?.setText(viewModel.customer.name)
//        binding.evCustomerPhone.editText?.setText(viewModel.customer.phone.toString())

        viewModel.getCustomerList()
        viewModel.getCategoriesWithServices()
        viewModel.observeAppointmentId()
//        // Observe the _appointmentId Flow and trigger the use case
//        observeAppointmentId()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(tag , "$tag->onDestroyView")
        _binding = null
    }

    private fun navigateToCustomerListScreen() {
        findNavController().popBackStack()
    }

    private fun showSnackBar(message : String) {
        Snackbar.make(
            requireView(),
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showCustomerList(anchorView: View) {

        val appArrayAdapter = AppArrayAdapter(
            requireContext(),
            viewModel.customerList
        ) { listBinding: ViewBinding, customer: Customer ->
            (listBinding as ItemLayoutBinding).itemNameTextView.text = customer.firstName +" "+ customer.lastName
        }
        AppListPopupWindow.showListPopupWindow(
            anchorView,
            appArrayAdapter
        ) { position ->
            val selectedCustomer = viewModel.customerList.get(position)
            binding.evCustomerList.editText?.setText(selectedCustomer.firstName +" "+ selectedCustomer.lastName)
            viewModel.selectedCustomer = selectedCustomer
            Toast.makeText(requireContext(), selectedCustomer.firstName, Toast.LENGTH_SHORT ).show()
        }

    }

    private fun showServiceDialog() {
        val dialog = SelectServicesDialogFragment()
        dialog.isCancelable = false
        dialog.onServicesSubmittedListener = object : SelectServicesDialogFragment.OnServicesSubmittedListener {
            override fun onServicesSubmitted() {
                selectedServicesAdapter.setData(viewModel.getSelectedServices())
                updateServicesTotalPrice()
            }
        }
        dialog.show(childFragmentManager, "AddBillDialogFragment") // Or childFragmentManager if in a Fragment
    }

    private fun updateServicesTotalPrice() {
        binding.tvValueTotalBill.text = viewModel.selectedServicesTotalAmount.toString()+" Rs."
    }

    private fun selectTimePicker() {

        AppTimePicker.showTimePicker(
            childFragmentManager,
            DateHelper.formatDate(Date()),
            "Select Time",
        ) { selectedTime, formattedTime -> // Handle the selected time here
            //batch.batchStartTime = selectedTime.time
            viewModel.appointment.appointmentTime = selectedTime.time
            binding.evAppointmentTime.editText?.setText(formattedTime)
        }

    }

    private fun selectDatePicker() {

        AppDatePicker.showDatePicker(
            childFragmentManager,
            viewModel.appointment.appointmentDate,
            "Select Start Date",
        ) { selectedDate, formattedDate -> // Handle the selected time here
            viewModel.appointment.appointmentDate = DateHelper.formatDate(selectedDate.time)
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