package com.bodakesatish.sandhyasbeautyservices.ui.appointments.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentSelectServicesDialogBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.ViewModelNewAppointment
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter.CategoryWithServiceViewItem
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter.ServiceDialogAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectServicesDialogFragment : DialogFragment() {

    private var _binding: FragmentSelectServicesDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ViewModelNewAppointment by viewModels(ownerProducer = { requireActivity() })
    private lateinit var serviceAdapter: ServiceDialogAdapter

    interface OnServicesSubmittedListener {
        fun onServicesSubmitted(selectedServices: List<Service>)
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

        binding.rvServicesList.layoutManager = LinearLayoutManager(requireContext())
        serviceAdapter = ServiceDialogAdapter { updatedService ->
            // Call a new ViewModel function to update the state based on the updated service
            viewModel.updateServiceSelectionState(updatedService)

        }
        binding.rvServicesList.adapter = serviceAdapter

        binding.btnSelectService.setOnClickListener {
            val selectedServices = serviceAdapter.getSelectedServices()
            onServicesSubmittedListener?.onServicesSubmitted(selectedServices)
            dismiss()
        }

        serviceAdapter.setData(viewModel.categoryWithServiceListFlow.value as ArrayList<CategoryWithServiceViewItem>)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

