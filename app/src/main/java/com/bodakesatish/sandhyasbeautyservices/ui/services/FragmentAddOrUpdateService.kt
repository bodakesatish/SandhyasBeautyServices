package com.bodakesatish.sandhyasbeautyservices.ui.services

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentAddEditServiceBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentAddOrUpdateService : Fragment() {

    private var _binding: FragmentAddEditServiceBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: AddOrUpdateServiceViewModel by viewModels()

    private val tag = this.javaClass.simpleName

    private var categoryList: List<Category> = emptyList()

    val args : FragmentAddOrUpdateServiceArgs by navArgs()

    var category = Category()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditServiceBinding.inflate(inflater, container, false)
        val root: View = binding.root
        args.category?.let {
           category = it
        }
        args.service?.let {
            viewModel.service = it
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initHeader()
        initListeners()
        initObservers()
        initData()
        onBackPressed()

    }

    private fun initView() {
        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_back_24)
    }


    private fun onBackPressed() {
        // This callback will only be called when FragmentCustomerList is at least Started.
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handle the back button event
            // e.g., navigate to the previous screen or pop the back stack
            //requireActivity().finish()
            findNavController().popBackStack()
        }

        // You can enable/disable the callback based on certain conditions
        // callback.isEnabled = someCondition
    }


    private fun initHeader() {
        if(viewModel.service.id == 0) {
            binding.headerGeneric.tvHeader.text = "Add your service to " + category.categoryName
        } else {
            binding.headerGeneric.tvHeader.text = "Update service - " + category.categoryName
        }
    }

    private fun initListeners() {
        // Update ViewModel when input fields change
        binding.evServiceName.editText?.doAfterTextChanged { editable ->
            viewModel.service.serviceName = editable?.toString() ?: ""
        }

        binding.evServicePrice.editText?.doAfterTextChanged { editable ->
            if(editable?.isNotEmpty() == true) {
                viewModel.service.servicePrice = editable?.toString()?.toDouble() ?: 0.0
            }
        }

        binding.btnNewService.setOnClickListener {
            if (binding.evServiceName.editText.toString().isEmpty()) {
                showSnackBar("Enter Service Name")
            } else if (binding.evServicePrice.editText.toString().isEmpty()) {
                showSnackBar("Enter Service Price")
            } else {
                viewModel.addOrUpdateService()
            }
        }

        binding.headerGeneric.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.customerResponse.observe(viewLifecycleOwner) {
            if(it) {
                showSnackBar("Service added successfully")
                navigateToCustomerListScreen()
            }
        }

    }

    private fun initObservers() {

    }

    private fun initData() {
          viewModel.service.categoryId = category.id
          if(viewModel.service.id != 0 ) {
              binding.evServiceName.editText?.setText(viewModel.service.serviceName)
              binding.evServicePrice.editText?.setText(viewModel.service.servicePrice.toString())
          }
//        binding.evCustomerName.editText?.setText(viewModel.customer.name)
//        binding.evCustomerPhone.editText?.setText(viewModel.customer.phone.toString())
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

}