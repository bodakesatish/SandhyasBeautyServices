package com.bodakesatish.sandhyasbeautyservices.ui.category

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentEditCategoryBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditCategoryFragment : Fragment() {

    private var _binding: FragmentEditCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditCategoryViewModel by viewModels()
    private val navArgs: EditCategoryFragmentArgs by navArgs()

    private val tag = this.javaClass.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load existing category if passed via navigation arguments
        navArgs.category?.let {
            viewModel.loadCategoryForEdit(it)
        }

        setupUIListeners()
        observeViewModel()
        setupOnBackPressed()
    }

    private fun setupUIListeners() {
        binding.headerGeneric.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.evCategoryName.editText?.doAfterTextChanged { editable ->
            viewModel.onCategoryNameChanged(editable?.toString() ?: "")
        }

        binding.btnAddCategory.setOnClickListener {
            viewModel.saveCategory()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    // Update header text
                    binding.headerGeneric.tvHeader.text = uiState.headerText
                    binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_back_24) // Assuming this is constant

                    // Update category name input (especially if loaded for edit)
                    // Only set text if it's different to avoid cursor jumps
                    if (binding.evCategoryName.editText?.text.toString() != uiState.categoryName) {
                        binding.evCategoryName.editText?.setText(uiState.categoryName)
                        // Optionally move cursor to the end if text was programmatically set
                        // binding.evCategoryName.editText?.setSelection(uiState.categoryName.length)
                    }

                    // Update button text based on edit mode
                    binding.btnAddCategory.text = if (uiState.isEditMode) {
                        getString(R.string.update_category_button_text) // e.g., "Update Category"
                    } else {
                        getString(R.string.add_category_button_text) // e.g., "Add Category"
                    }

                    // Show/hide loading indicator
//                    binding.progressBar.isVisible = uiState.isLoading
                    binding.btnAddCategory.isEnabled = !uiState.isLoading // Disable button while loading

                    // Handle category name error
                    if (uiState.categoryNameError != null) {
                        binding.evCategoryName.error = uiState.categoryNameError
                    } else {
                        binding.evCategoryName.error = null // Clear error
                        binding.evCategoryName.isErrorEnabled = false // Ensure helper text space is also cleared if using Material TextInputLayout
                    }

                    // Handle save result (success or error)
                    when (val saveResult = uiState.saveResult) {
                        is CategorySaveResult.Success -> {
                            val message = if (uiState.isEditMode) {
                                getString(R.string.category_updated_successfully)
                            } else {
                                getString(R.string.category_added_successfully)
                            }
                            showSnackBar(message)
                            navigateToCategoryListScreen()
                            viewModel.consumeSaveResult() // Reset the event
                        }
                        is CategorySaveResult.Error -> {
                            showSnackBar(saveResult.message)
                            viewModel.consumeSaveResult() // Reset the event
                        }
                        CategorySaveResult.Idle -> {
                            // Do nothing, no ongoing save operation or result to show
                        }
                    }
                }
            }
        }
    }

    private fun setupOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handle the back button event, e.g., navigate up or pop back stack
            navigateToCategoryListScreen()
        }
    }

    private fun navigateToCategoryListScreen() {
        // Ensure we are not trying to navigate if already navigating or fragment is not added
       // if (isAdded && findNavController().currentDestination?.id == R.id.navigation_category_list) { // Replace with your actual fragment ID
            findNavController().popBackStack()
        //}
    }

    private fun showSnackBar(message: String) {
        if (view == null) return // Ensure view is available
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(tag, "$tag->onDestroyView")
        _binding = null // Important for view binding memory leak prevention
    }
}