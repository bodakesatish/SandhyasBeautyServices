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

@AndroidEntryPoint
class FragmentNewAppointment : Fragment() {

    private var _binding: FragmentNewAppointmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // ViewModel scoped to the Activity
    private val viewModel: ViewModelNewAppointment by viewModels(ownerProducer = { requireActivity() })

    private val tag = this.javaClass.simpleName

    private var selectedServicesAdapter = SelectedServicesAdapter()

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

        // Set the appointmentId from navigation arguments
        viewModel.setAppointmentId(args.appointmentId)

        setupViews()
        setupListeners()
        observeViewModel()

        // Initial data loading (ViewModel's init block should handle some of this)
        // You might still need to trigger specific actions here if not handled by Flow
        // viewModel.getCustomerList() // If not triggered by a Flow or init
        // viewModel.getCategoriesWithServices() // If not triggered by a Flow or init

//        getNavData()
//        initHeader()
//        initView()
//        initListeners()
//        initObservers()
//        initData()
//
//        setupRecyclerView()
//        setupCustomerDropdown()
//        setupSaveButton()
//        observeViewModel()
    }

    private fun setupViews() {
        if (args.appointmentId == 0) {
            binding.headerGeneric.tvHeader.setText("New Appointment")
            binding.btnNewAppointment.setText("Create Appointment")
        } else {
            binding.headerGeneric.tvHeader.setText("Edit Appointment")
            binding.btnNewAppointment.setText("Update Appointment")
        }
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

//        // Update ViewModel when input fields change using doAfterTextChanged
//        binding.evCategoryName.editText?.doAfterTextChanged { editable ->
//            // Update the category name in the ViewModel's state
//            viewModel.updateCategoryName(editable?.toString() ?: "")
//        }

        binding.evCustomerList.editText?.setOnClickListener {
            showCustomerList(it)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe the combined appointment and customer details
                viewModel.appointmentDetailsFlow.filterNotNull().collectLatest { details ->
                    Log.d(tag, "Observed Appointment Details: $details")
                    details?.let {
                        updateUiWithAppointmentDetails(
                            it.appointment,
                            it.customer,
                            it.serviceDetailsWithServices
                        )
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
                viewModel.selectedCustomerFlow
                    .filterNotNull() // Add this operator
                    .collectLatest { selectedCustomer ->
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
                    updateServicesTotalPrice()//viewModel.selectedServicesTotalAmount)
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
            }
        }

        viewModel.customerResponse.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.customerResponse.value = false
                showSnackBar("Appointment created successfully")
                navigateToCustomerListScreen()
            }
        }
    }

    private fun updateUiWithAppointmentDetails(
        appointment: Appointment,
        customer: Customer?,
        serviceDetailsWithServices: List<ServiceDetailWithService>
    ) {
        // Set the category name
//        binding.evCategoryName.editText?.setText(appointment.category.categoryName)

        // Update date and time fields
        binding.evAppointmentDate.editText?.setText(
            DateHelper.getFormattedDate(
                appointment.appointmentDate,
                DateHelper.DATE_FORMAT_dd_MMM_yyyy
            )
        )
        binding.evAppointmentTime.editText?.setText(DateHelper.formatTime(appointment.appointmentTime))

        // Update the customer field and ViewModel's selected customer
        customer?.let {
            binding.evCustomerList.editText?.setText("${it.firstName} ${it.lastName}")
            // Set the selected customer in the ViewModel
            viewModel.selectCustomer(it)
        } ?: run {
            binding.evCustomerList.editText?.setText("") // Clear customer field if not found
            viewModel.selectCustomer(null) // Clear selected customer in ViewModel
        }

        // Update the list of selected services for the RecyclerView
        // Extract the Service objects from the ServiceDetailWithService list
//        val selectedServices = serviceDetailsWithServices.mapNotNull { it.service }

        val selectedServices = serviceDetailsWithServices.mapNotNull {
            Service(
                id = it.serviceId,
                serviceName = it.serviceName,
                servicePrice = it.servicePrice
            )
        }

        // Update the selected services list in the ViewModel
        viewModel.updateSelectedServices(selectedServices)

        // Update the total price display based on the ViewModel's calculated total
        updateServicesTotalPrice()//viewModel.selectedServicesTotalAmount)

        // Update the total bill amount field (this is likely the final amount after services)
        binding.tvValueTotalBill?.setText(String.format("%.2f", appointment.totalBillAmount))

        // Note: If there are other fields in your Appointment model (e.g., notes), update them here too.
    }

    private fun setupRecyclerView() {
//        categoryWithServiceAdapter = CategoryWithServiceAdapter { serviceItem, isSelected ->
//            // Handle service selection change if needed
//            Log.d(tag, "Service ${serviceItem.service.serviceName} selected: $isSelected")
//            serviceItem.service.isSelected = isSelected
//        }
//        binding.recyclerViewCategoriesAndServices.adapter = categoryWithServiceAdapter
    }

    private fun clearAppointmentUi() {
        Log.d(tag, "$tag->clearAppointmentUi")
        // Clear or reset the UI fields for a new appointment
//        binding.evCategoryName.editText?.setText("")
        binding.evAppointmentDate.editText?.setText("")
        binding.evAppointmentTime.editText?.setText("")
        binding.evCustomerList.editText?.setText("")
        binding.tvValueTotalBill?.setText(String.format("%.2f", 0.0)) // Reset total bill to 0.0

        // Clear the selected services list
        selectedServicesAdapter.setData(emptyList())
        updateServicesTotalPrice()//(0.0)

        // Reset the ViewModel's state for a new appointment
        // Assuming your ViewModel has a way to reset its internal state
        viewModel.resetForNewAppointment() // You'll need to implement this in your ViewModel
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

    private fun observeViewModel12() {
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
//        binding.evCategoryName.editText?.doAfterTextChanged { editable ->
//            viewModel.category.categoryName = editable?.toString() ?: ""
//        }

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
            if (it) {
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
                    if (data != null) {
                        updateData()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedCustomerFlow.collect { data ->
                    if (data != null) {
                        binding.evCustomerList.editText?.setText(data?.firstName + " " + data?.lastName)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reloadSelectedService.collect { data ->
                    if (data) {
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
//        binding.evAppointmentDate.editText?.setText(
//            DateHelper.getFormattedDate(
//                viewModel.appointment.appointmentDate,
//                DateHelper.DATE_FORMAT_dd_MMM_yyyy
//            )
//        )
//        binding.evAppointmentTime.editText?.setText(DateHelper.formatTime(viewModel.appointment.appointmentTime))
    }

    private fun initData() {
        //if (viewModel.appointment.id != 0) {
            //  viewModel.getSelectedServicesIdsOfAppointment()
            //   viewModel.getAppointmentDetailById(viewModel.appointment.id)
        //}
//        binding.evCustomerName.editText?.setText(viewModel.customer.name)
//        binding.evCustomerPhone.editText?.setText(viewModel.customer.phone.toString())

        viewModel.getCustomerList()
        //viewModel.getCategoriesWithServices()
        viewModel.observeAppointmentId()
//        // Observe the _appointmentId Flow and trigger the use case
//        observeAppointmentId()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(tag, "$tag->onDestroyView")
        _binding = null
    }

    private fun navigateToCustomerListScreen() {
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
        // Get the current list of customers from the StateFlow's value property
        val customerList = viewModel.customerListFlow.value

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
            Toast.makeText(requireContext(), selectedCustomer.firstName, Toast.LENGTH_SHORT).show()
        }

    }

    private fun showServiceDialog() {
        viewModel.updateSelectedServicesWithCategoryServices()

        val dialog = SelectServicesDialogFragment()
//        dialog.isCancelable = false
        dialog.onServicesSubmittedListener =
            object : SelectServicesDialogFragment.OnServicesSubmittedListener {
                override fun onServicesSubmitted() {
                    selectedServicesAdapter.setData(viewModel.getSelectedServices())
                    updateServicesTotalPrice()
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
            //batch.batchStartTime = selectedTime.time
            viewModel.currentAppointment.appointmentTime = selectedTime.time
            binding.evAppointmentTime.editText?.setText(formattedTime)
        }

    }

    private fun selectDatePicker() {

        AppDatePicker.showDatePicker(
            childFragmentManager,
            viewModel.currentAppointment.appointmentDate,
            "Select Start Date",
        ) { selectedDate, formattedDate -> // Handle the selected time here
            viewModel.currentAppointment.appointmentDate = DateHelper.formatDate(selectedDate.time)
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