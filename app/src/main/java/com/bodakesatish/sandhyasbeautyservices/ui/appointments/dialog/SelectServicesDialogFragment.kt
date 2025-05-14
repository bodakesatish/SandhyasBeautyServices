package com.bodakesatish.sandhyasbeautyservices.ui.appointments.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentSelectServicesDialogBinding
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.ViewModelNewAppointment
import com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter.ServiceDialogAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectServicesDialogFragment : DialogFragment() {

    private var _binding: FragmentSelectServicesDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ViewModelNewAppointment by viewModels(ownerProducer = { requireActivity() })
    private lateinit var serviceAdapter: ServiceDialogAdapter

    interface OnServicesSubmittedListener {
        fun onServicesSubmitted()
    }

    var onServicesSubmittedListener: OnServicesSubmittedListener? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            // Optional: Set dialog properties (e.g., no title)
            // window?.requestFeature(Window.FEATURE_NO_TITLE)
        }
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

        binding.recyclerViewServices.layoutManager = LinearLayoutManager(requireContext())
        serviceAdapter = ServiceDialogAdapter()
        binding.recyclerViewServices.adapter = serviceAdapter

        binding.buttonSubmit.setOnClickListener {
            viewModel.categoryWithServiceList = serviceAdapter.itemList
            onServicesSubmittedListener?.onServicesSubmitted()
            dismiss()
        }

        serviceAdapter.setData(viewModel.categoryWithServiceList)

//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                //viewModel.categoryWithServiceList.collect { categoryWithServices ->
//                    // Update UI with the received data
//                    //serviceAdapter.setData(data)
//                    val items = mutableListOf<CategoryWithServiceViewItem>()
//
//                    for (categoryWithService in viewModel.categoryWithServiceList) {
//                        val category = Category(categoryWithService.id, categoryWithService.categoryName)
//                        val categoryService = CategoryWithServiceViewItem.CategoryHeader(category)
//                        items.add(categoryService)
//                        for (service in categoryWithService.services) {
//                            items.add(CategoryWithServiceViewItem.ServiceItem(service))
//                        }
//                    }
//                    serviceAdapter.submitList(items)
//
////                    val items = mutableListOf<ServiceDialogItem>()
////                    groupedServices.forEach { (category, services) ->
////                        items.add(ServiceDialogItem.CategoryHeader(category))
////                        services.forEach { items.add(ServiceDialogItem.ServiceItem(it)) }
////                    }
////                    serviceAdapter.submitList(items)
//               // }
//            }
//        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

