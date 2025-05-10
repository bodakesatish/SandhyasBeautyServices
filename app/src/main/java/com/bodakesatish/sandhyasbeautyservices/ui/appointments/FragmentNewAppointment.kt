package com.bodakesatish.sandhyasbeautyservices.ui.appointments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentAddEditCategoryBinding
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentNewAppointmentBinding
import com.bodakesatish.sandhyasbeautyservices.ui.category.AddOrUpdateCategoryViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentNewAppointment : Fragment() {

    private var _binding: FragmentNewAppointmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: ViewModelNewAppointment by viewModels()

    private val tag = this.javaClass.simpleName


//    val args : FragmentAddOrUpdateCustomerArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewAppointmentBinding.inflate(inflater, container, false)
        val root: View = binding.root
//        args.customer?.let {
//            viewModel.customer = it
//        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initHeader()
        initListeners()
        initObservers()
        initData()

    }

    private fun initHeader() {

    }

    private fun initListeners() {
        // Update ViewModel when input fields change
        binding.evCategoryName.editText?.doAfterTextChanged { editable ->
            viewModel.category.categoryName = editable?.toString() ?: ""
        }

        binding.btnNewAppointment.setOnClickListener {
//            if(binding.evCategoryName.editText.toString().isEmpty()) {
//                showSnackBar("Enter Category Name")
//            } else {
                viewModel.createNewAppointment()
//            }
        }
        viewModel.customerResponse.observe(viewLifecycleOwner) {
            showSnackBar("Appointment created successfully")
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
    }

    private fun initObservers() {

    }

    private fun initData() {
//        binding.evCustomerName.editText?.setText(viewModel.customer.name)
//        binding.evCustomerPhone.editText?.setText(viewModel.customer.phone.toString())
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

}