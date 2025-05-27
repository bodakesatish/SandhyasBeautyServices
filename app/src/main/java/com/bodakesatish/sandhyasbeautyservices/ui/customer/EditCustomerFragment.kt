package com.bodakesatish.sandhyasbeautyservices.ui.customer

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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentCustomerAddBinding
import com.bodakesatish.sandhyasbeautyservices.extension.showKeyboard
import com.bodakesatish.sandhyasbeautyservices.util.AppDatePicker
import com.bodakesatish.sandhyasbeautyservices.util.DateHelper
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditCustomerFragment : Fragment() {

    private var _binding: FragmentCustomerAddBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: EditCustomerViewModel by viewModels()

    private val tag = this.javaClass.simpleName


    val args : EditCustomerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerAddBinding.inflate(inflater, container, false)
        val root: View = binding.root
        args.customer?.let {
            viewModel.customer = it
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initHeader()
        initView()
        initListeners()
        initObservers()
        initData()

    }

    private fun initView() {
        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_back_24)
        if(viewModel.customer.id != 0) {
            binding.btnAdd.text = "Update"
        }
        binding.evPatientFirstName.editText?.requestFocus()
        binding.evPatientFirstName.post {
            binding.evPatientFirstName.editText?.showKeyboard()
        }
    }

    private fun initHeader() {
        binding.headerGeneric.tvHeader.text = "New Customer"
    }

    private fun initListeners() {
        // Update ViewModel when input fields change
        binding.evPatientFirstName.editText?.doAfterTextChanged { editable ->
            viewModel.customer.firstName = editable?.toString() ?: ""
        }
        binding.evPatientLastName.editText?.doAfterTextChanged { editable ->
            viewModel.customer.lastName = editable?.toString() ?: ""
        }
        binding.evCustomerPhone.editText?.doAfterTextChanged { editable ->
            viewModel.customer.phone = editable?.toString() ?: ""
        }
        binding.evCustomerAddress.editText?.doAfterTextChanged { editable ->
            viewModel.customer.address = editable?.toString() ?: ""
        }
//        binding.evCustomerAge.editText?.doAfterTextChanged { editable ->
//            viewModel.customer.dob = editable?.toString()?.toInt() ?: 0
//        }

        binding.evCustomerAge.editText?.setOnClickListener {
            batchStartDatePicker()
        }

        binding.btnAdd.setOnClickListener {
            if(binding.evPatientFirstName.editText.toString().isEmpty()) {
                showSnackBar("Enter First name")
            } else {
                viewModel.addOrUpdateCustomer()
            }
        }
        viewModel.customerResponse.observe(viewLifecycleOwner) {
            showSnackBar("CheckUp added successfully")
            navigateToCustomerListScreen()
        }

        // This callback will only be called when FragmentAddOrUpdateCustomer is at least Started.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handle the back button event
            // e.g., navigate to the previous screen or pop the back stack
            navigateToCustomerListScreen()
        }

        binding.headerGeneric.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // You can enable/disable the callback based on certain conditions
        // callback.isEnabled = someCondition
    }

    private fun batchStartDatePicker() {

        AppDatePicker.showDatePicker(
            parentFragmentManager,
            viewModel.customer.dob,
            "Select Date of Birth",
        ) { selectedDate, formattedDate -> // Handle the selected date here
            viewModel.customer.dob = selectedDate.time
            binding?.evCustomerAge?.editText?.setText(formattedDate)
        }

    }

    private fun initObservers() {

    }

    private fun initData() {
        binding.evPatientFirstName.editText?.setText(viewModel.customer.firstName)
        binding.evPatientLastName.editText?.setText(viewModel.customer.lastName)
        binding.evCustomerPhone.editText?.setText(viewModel.customer.phone)
        binding.evCustomerAddress.editText?.setText(viewModel.customer.address)
        binding.evCustomerAge.editText?.setText(DateHelper.getFormattedTime(viewModel.customer.dob, DateHelper.DATE_FORMAT_dd_MMM_yyyy))
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}