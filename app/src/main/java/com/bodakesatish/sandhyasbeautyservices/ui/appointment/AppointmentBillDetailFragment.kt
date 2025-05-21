package com.bodakesatish.sandhyasbeautyservices.ui.appointment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentAppointmentDetailsBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.ui.appointment.dialog.ServiceSelectionDialogFragment
import com.bodakesatish.sandhyasbeautyservices.util.DateHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AppointmentBillDetailFragment  : Fragment() {

    private var _binding: FragmentAppointmentDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val args: AppointmentBillDetailFragmentArgs by navArgs()

    val viewModel: AppointmentBillDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAppointmentDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()

        // Set the appointmentId from navigation arguments
        // Only set if it's a non-zero ID (for edit mode)
        if (args.appointmentId != 0) {
            viewModel.setAppointmentId(args.appointmentId)
        }

        setupListeners()
        setupObservers()
        setupFragmentResultListeners()
    }

    private fun setupListeners() {
        binding.btnEditAppointment.setOnClickListener {
            // Handle item click
            val action = AppointmentBillDetailFragmentDirections.actionAppointmentBillDetailFragmentToNavigationCreateAppointment(
                viewModel.appointmentIdFlow.value
            )
            findNavController().navigate(action)
        }
        binding.btnEditServices.setOnClickListener {
            // Handle item click
            val action = AppointmentBillDetailFragmentDirections.actionAppointmentBillDetailFragmentToAddServicesToAppointmentFragment(
                viewModel.appointmentIdFlow.value
            )
            findNavController().navigate(action)
//            findNavController().navigate(R.id.navigation_add_services_to_appointment)
        }
        binding.btnProceedToBilling.setOnClickListener {
            // Navigate to Billing Screen (passing appointmentId)
            // findNavController().navigate(R.id.action_fragmentAppointmentDetails_to_billingFragment, bundleOf("appointmentId" to appointmentId))
        }

        binding.btnEditBill.setOnClickListener {
            // Navigate to Billing Screen for editing (passing appointmentId and perhaps billingId)
            // findNavController().navigate(R.id.action_fragmentAppointmentDetails_to_billingFragment, bundleOf("appointmentId" to appointmentId, "billingId" to billing?.id))
            // Handle item click
//            val action = AppointmentBillDetailFragmentDirections.navToBilling(
//                viewModel.appointmentIdFlow.value
//            )
//            findNavController().navigate(action)
        }

        binding.btnViewInvoice.setOnClickListener {
            // Navigate to Invoice Screen or trigger invoice generation
            // findNavController().navigate(R.id.action_fragmentAppointmentDetails_to_invoiceFragment, bundleOf("billingId" to billing?.id))
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.appointmentDetailFlow
                        .filterNotNull() // Ensure we only process non-null details
                        .collectLatest { detail ->
                            // Update UI fields with initial appointment details
                            binding.tvDetailCustomerName.text = detail.customer?.firstName + " " + detail.customer?.lastName
                            binding.tvDetailAppointmentDatetime.text = DateHelper.getFormattedDate(detail.appointment.appointmentDate,
                                "dd MMM yyyy") +", "+ DateHelper.formatTime(detail.appointment.appointmentTime)

                            // --- Appointment Status ---
                                binding.tvDetailAppointmentStatus.text = detail.appointment.appointmentStatus.name // Or a more user-friendly string from string resources

                            var services = ""
                            detail.serviceDetailsWithServices.map {
                               services = services.plus("• ${it.serviceName}  (₹${it.servicePrice.toInt()})\n")
                                Log.i(tag, services)
                            }
                            Log.i(tag, services)
                            binding.tvServiceWithPrice.text = services
//                            binding.tvAppointmentTime.text = details.appointment.appointmentTime
//                            // Update the customer field based on the loaded customer
//                            details.customer?.let { customer ->
//                                binding.tvCustomerName.text =
//                                    "${customer.firstName} ${customer.lastName}"
//                            }
                            // The selected services for the RecyclerView are handled by the categoryWithServiceListFlow observer
                            // because the viewModel.updateSelectedServicesFromDialog() is called from the dialog result.
                        }
                }
            }
        }
    }

    private fun setupHeader() {
        binding.headerGeneric.tvHeader.setText(getString(R.string.appointment_and_billing_details))
        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_back_24)
        binding.headerGeneric.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupFragmentResultListeners() {
        // Listen for the result from the SelectServicesDialogFragment
        setFragmentResultListener(AddOrEditServicesToAppointmentFragment.REQUEST_KEY_SELECTED_SERVICES) { requestKey, bundle ->
            // Ensure we handle the correct request key
            if (requestKey == AddOrEditServicesToAppointmentFragment.REQUEST_KEY_SELECTED_SERVICES) {
                val selectedServices =
                    bundle.getSerializable(AddOrEditServicesToAppointmentFragment.BUNDLE_KEY_SELECTED_SERVICES) as? ArrayList<Service>
                selectedServices?.let {
                    Log.i(tag, "Received Selected Services from Dialog: $it")
                    // Update the ViewModel with the new list of selected services
                    // This assumes you have a function in your ViewModel to handle this update
                    val selectedServicesIds = it.map { service -> service.id }.toSet()
                  //  viewModel.updateCategoryServiceSelection(selectedServicesIds)
                }
            }
        }
    }


}