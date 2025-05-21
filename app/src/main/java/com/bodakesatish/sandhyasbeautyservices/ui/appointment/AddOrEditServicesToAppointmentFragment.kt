package com.bodakesatish.sandhyasbeautyservices.ui.appointment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentAddOrEditServicesToAppointmentBinding
import com.bodakesatish.sandhyasbeautyservices.ui.appointment.adapter.ServiceDialogAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddOrEditServicesToAppointmentFragment : Fragment() {

    private var _binding: FragmentAddOrEditServicesToAppointmentBinding? = null
    private val binding get() = _binding

    private val viewModel : AddOrEditServicesToAppointmentViewModel by viewModels()

    private lateinit var servicesAdapter: ServiceDialogAdapter

    val args: AddOrEditServicesToAppointmentFragmentArgs by navArgs()


    // Define keys for FragmentResultListener
    companion object {
        const val REQUEST_KEY_SELECTED_SERVICES = "request_key_selected_services"
        const val BUNDLE_KEY_SELECTED_SERVICES = "bundle_key_selected_services"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddOrEditServicesToAppointmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the appointmentId from navigation arguments
        // Only set if it's a non-zero ID (for edit mode)
        if (args.appointmentId != 0) {
            viewModel.setAppointmentId(args.appointmentId)
        }

        servicesAdapter = ServiceDialogAdapter {service, isSelected ->
            // Call the ViewModel function to update the selection state
            viewModel.updateServiceSelectionState(service.id, isSelected)
        }

        binding?.rvServicesList?.layoutManager = LinearLayoutManager(requireContext())
        binding?.rvServicesList?.adapter = servicesAdapter
        initObservers()
        initListeners()
    }

    private fun initListeners() {

        binding?.btnSubmitServiceSelection?.setOnClickListener {
            viewModel.saveSelectedServices()
            // Navigate back to the previous fragment
    //        findNavController().popBackStack()
        }
    }

    fun initObservers() {
        // Observe the ViewModel's categoryWithServiceListFlow to update the adapter
        // This ensures the dialog's UI reacts to selection changes made via the adapter's callback
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categoryWithServiceListFlow.collect { updatedList ->
                    Log.d(tag, "Dialog observing updated categoryWithServiceListFlow. Size: ${updatedList.size}")
                    // Submit the updated list to the ListAdapter
                    servicesAdapter.submitList(updatedList)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe for save result (success or failure)
                launch {
                    viewModel.saveResult.collectLatest { success ->
                        if (success) {
                            showSnackBar("Services saved successfully")
                            navigateToPreviousScreen()
                        } else {
                            showSnackBar("Services failed to save")
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


}