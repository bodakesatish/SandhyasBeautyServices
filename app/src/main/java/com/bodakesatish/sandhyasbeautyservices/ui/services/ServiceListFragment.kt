package com.bodakesatish.sandhyasbeautyservices.ui.services

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentServiceListBinding
import com.bodakesatish.sandhyasbeautyservices.ui.services.adapter.ServiceListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ServiceListFragment : Fragment() {

    private var _binding: FragmentServiceListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: ServiceListViewModel by viewModels()

    private var customerAdapter: ServiceListAdapter = ServiceListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentServiceListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initListeners()
        initObservers()
        initData()
        onBackPressed()
    }

    private fun onBackPressed() {
        // This callback will only be called when FragmentCustomerList is at least Started.
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handle the back button event
            // e.g., navigate to the previous screen or pop the back stack
            requireActivity().finish()
        }

        // You can enable/disable the callback based on certain conditions
        // callback.isEnabled = someCondition
    }

    private fun initView() {
//        binding.headerGeneric.tvHeader.text = "List of Customers"
//        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_menu_24)
    }

    private fun initListeners() {
        binding.btnNewService.setOnClickListener {
            findNavController().navigate(R.id.navigation_add_service)
        }
        customerAdapter.setOnClickListener {
//            val action = FragmentCustomerListDirections.actionFragmentCustomerListToFragmentAddOrUpdateCustomer(it)
//            findNavController().navigate(action)
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.serviceList.collect { data ->
                    Log.d(tag, "$tag->initObservers collect->serviceList")
                    // Update UI with the received data
                    customerAdapter.setData(data)
                }
            }
        }
    }

    private fun initData() {
        binding.rvServiceList.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvServiceList.adapter = customerAdapter
        binding.rvServiceList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCategoryList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}