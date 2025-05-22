package com.bodakesatish.sandhyasbeautyservices.ui.category

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
import androidx.navigation.fragment.navArgs
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentAddEditCategoryBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAddOrUpdateCategory : Fragment() {

    private var _binding: FragmentAddEditCategoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: AddOrUpdateCategoryViewModel by viewModels()

    private val tag = this.javaClass.simpleName

    val args : FragmentAddOrUpdateCategoryArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditCategoryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        args.category?.let {
            viewModel.category = it
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initHeader()
        initListeners()
        initObservers()
        initData()
        onBackPressed()

    }

    private fun onBackPressed() {
        // This callback will only be called when FragmentCustomerList is at least Started.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handle the back button event
            // e.g., navigate to the previous screen or pop the back stack
            //requireActivity().finish()
            findNavController().popBackStack()
        }

        // You can enable/disable the callback based on certain conditions
        // callback.isEnabled = someCondition
    }

    private fun initHeader() {
        binding.headerGeneric.tvHeader.text = "All Category"
        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_back_24)
        if(viewModel.category.id != 0) {
            binding.headerGeneric.tvHeader.text = "Edit Category"
        }
    }

    private fun initListeners() {
        // Update ViewModel when input fields change
        binding.evCategoryName.editText?.doAfterTextChanged { editable ->
            viewModel.category.categoryName = editable?.toString() ?: ""
        }
        binding.headerGeneric.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnAddCategory.setOnClickListener {
            if(binding.evCategoryName.editText.toString().isEmpty()) {
                showSnackBar("Enter Category Name")
            } else {
                viewModel.addOrUpdateCategory()
            }
        }
        viewModel.customerResponse.observe(viewLifecycleOwner) {
            showSnackBar("Category added successfully")
            navigateToCustomerListScreen()
        }

        // This callback will only be called when FragmentAddOrUpdateCustomer is at least Started.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
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
        if(viewModel.category.id != 0) {
            binding.btnAddCategory.text = "Update Category"
            binding.evCategoryName.editText?.setText(viewModel.category.categoryName)
        }
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