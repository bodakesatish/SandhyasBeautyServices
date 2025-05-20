package com.bodakesatish.sandhyasbeautyservices.ui.appointment.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentSelectServicesDialogBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.ui.appointment.CreateAppointmentViewModel
import com.bodakesatish.sandhyasbeautyservices.ui.appointment.adapter.CategoryWithServiceViewItem
import com.bodakesatish.sandhyasbeautyservices.ui.appointment.adapter.ServiceDialogAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ServiceSelectionDialogFragment : DialogFragment() {

    private var _binding: FragmentSelectServicesDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateAppointmentViewModel by viewModels(ownerProducer = { requireActivity() })
    private lateinit var serviceAdapter: ServiceDialogAdapter
    private val tag = "Beauty->"+this.javaClass.simpleName

    // Define keys for FragmentResultListener
    companion object {
        const val REQUEST_KEY_SELECTED_SERVICES = "request_key_selected_services"
        const val BUNDLE_KEY_SELECTED_SERVICES = "bundle_key_selected_services"
        const val BUNDLE_KEY_INITIAL_SERVICES = "bundle_key_initial_services" // Key for initial data
    }

    interface OnServicesSubmittedListener {
        fun onServicesSubmitted(selectedServices: List<Service>)
    }

    // Factory method to create a new instance with initial data
    fun newInstance(initialServices: List<CategoryWithServiceViewItem>): ServiceSelectionDialogFragment {
        val fragment = ServiceSelectionDialogFragment()
        fragment.arguments =
            bundleOf(BUNDLE_KEY_INITIAL_SERVICES to ArrayList(initialServices)) // Pass list via arguments
        return fragment
    }

    var onServicesSubmittedListener: OnServicesSubmittedListener? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectServicesDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        serviceAdapter = ServiceDialogAdapter {service, isSelected ->
            // Call the ViewModel function to update the selection state
            viewModel.updateServiceSelectionState(service.copy(isSelected = isSelected))
        }

        binding.rvServicesList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvServicesList.adapter = serviceAdapter


        // Get initial data from arguments
        val initialServices = arguments?.getSerializable(BUNDLE_KEY_INITIAL_SERVICES) as? List<CategoryWithServiceViewItem>

        if (initialServices != null) {
            // Submit the initial list to the adapter
            serviceAdapter.submitList(initialServices)
        } else {
            // Handle the case where initial data is missing (e.g., show error)
            Log.e(tag, "Initial services data not found in arguments")
            // Optionally dismiss the dialog or show an error message
        }

        binding.btnCancelServiceSelection.setOnClickListener {
            dismiss()
        }

        binding.btnSubmitServiceSelection.setOnClickListener { view ->
            // Get the list of currently selected services from the adapter's current list
            // We derive this from the data the adapter is currently displaying.
            val selectedServices = serviceAdapter.currentList
                .filterIsInstance<CategoryWithServiceViewItem.ServiceItem>()
                .filter { it.service.isSelected }
                .map { it.service }

            Log.i(tag, "Selected Services on submission -> $selectedServices")

            // Use FragmentResultListener to pass the selected services back to the hosting Fragment
            setFragmentResult(REQUEST_KEY_SELECTED_SERVICES, bundleOf(BUNDLE_KEY_SELECTED_SERVICES to ArrayList(selectedServices)))

            dismiss() // Dismiss the dialog
        }

        // Observe the ViewModel's categoryWithServiceListFlow to update the adapter
        // This ensures the dialog's UI reacts to selection changes made via the adapter's callback
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categoryWithServiceListFlow.collect { updatedList ->
                    Log.d(tag, "Dialog observing updated categoryWithServiceListFlow. Size: ${updatedList.size}")
                    // Submit the updated list to the ListAdapter
                    serviceAdapter.submitList(updatedList)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

