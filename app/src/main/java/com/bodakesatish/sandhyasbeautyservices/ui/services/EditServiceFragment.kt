package com.bodakesatish.sandhyasbeautyservices.ui.services

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
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentAddEditServiceBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditServiceFragment : Fragment() {

    private var _binding: FragmentAddEditServiceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditServiceViewModel by viewModels()

    private val tag = this.javaClass.simpleName

    val args: EditServiceFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditServiceBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set initial data in ViewModel if it's the first creation (not a config change)
        // ViewModel will retain this across config changes
        if (savedInstanceState == null) { // Only set from args if it's not a recreation
            args.category.let {
                viewModel.setCategory(category = it)
            }
            // If service is passed, it means we are in edit mode.
            // ViewModel's setService will update the UI state accordingly.

            args.service?.let {
                viewModel.setService(service = it) // This will now also update input flows
            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initListeners()     // Sets up listeners to update ViewModel
        observeUiState()
        observeNavigationCommands()
        onBackPressed()

    }

    private fun initView() {
        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_back_24)
        // ProgressBar will be controlled by UI state
//        binding.progressBar.isVisible = false // Default to hidden
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }

    private fun initListeners() {
        binding.headerGeneric.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.evServiceName.editText?.doAfterTextChanged { editable ->
            viewModel.onServiceNameChanged(editable?.toString() ?: "")
        }

        binding.evServicePrice.editText?.doAfterTextChanged { editable ->
            viewModel.onServicePriceChanged(editable?.toString() ?: "")
        }

        binding.btnNewService.setOnClickListener {
            viewModel.addOrUpdateService()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    Log.d(tag, "UI State collected: $state")

                    // Update Header
                    binding.headerGeneric.tvHeader.text = state.headerText

                    // Update Input Fields (if text differs to avoid cursor jumps)
                    if (binding.evServiceName.editText?.text.toString() != state.serviceName) {
                        binding.evServiceName.editText?.setText(state.serviceName)
                        binding.evServiceName.editText?.setSelection(state.serviceName.length)
                    }
                    if (binding.evServicePrice.editText?.text.toString() != state.servicePrice) {
                        binding.evServicePrice.editText?.setText(state.servicePrice)
                        binding.evServicePrice.editText?.setSelection(state.servicePrice.length)
                    }

                    // Update Input Field Errors
                    binding.evServiceName.error = state.serviceNameError
                    binding.evServicePrice.error = state.servicePriceError

                    // Update Button Text (Optional, if you want "Update" vs "Add")
                    binding.btnNewService.text =
                        if (state.isEditMode) "Update Service" else "Add Service"

                    // Handle Save Result (Loading, Success, Error)
                    //  binding.progressBar.isVisible = state.saveResult is SaveResult.Loading
                    binding.btnNewService.isEnabled =
                        state.saveResult !is SaveResult.Loading // Disable button while loading
                    when (val saveResult = state.saveResult) {
                        is SaveResult.Success -> {
                            // Navigation is handled by observeNavigationCommands
                            // Snackbar    message can be shown here or after navigation
                             showSnackBar("Service saved successfully!")
                            viewModel.onSaveComplete()
                            observeNavigationCommands()
                            // ViewModel should reset saveResult to Idle after success or error is handled
                        }

                        is SaveResult.Error -> {
                            showSnackBar(saveResult.message)
                            viewModel.errorShown() // Notify ViewModel error has been shown
                        }

                        SaveResult.Idle -> { /* Do nothing specific, initial state */
                        }

                        SaveResult.Loading -> { /* ProgressBar is already visible */
                        }
                    }
                }
            }
        }
    }

    private fun observeNavigationCommands() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationCommands.collectLatest { command ->
                    when (command) {
                        is NavigationCommand.NavigateBack -> {
                            showSnackBar("Service saved successfully!") // Show success before navigating
                            findNavController().popBackStack()
                        }
                        // Handle other navigation commands if any
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(tag, "$tag->onDestroyView: Binding is nulled")
        _binding = null // Important to prevent memory leaks
    }

    private fun showSnackBar(message: String) {
        if (!isAdded || view == null) { // Check if fragment is added and view is available
            Log.w(
                tag,
                "Snackbar cannot be shown, fragment not attached or view is null. Message: $message"
            )
            return
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}