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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentServiceListBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.ui.services.adapter.ServiceListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class ServiceListFragment : Fragment() {

    private var _binding: FragmentServiceListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: ServiceListViewModel by viewModels()

    private var customerAdapter: ServiceListAdapter = ServiceListAdapter()

    private var category = Category()

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
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handle the back button event
            // e.g., navigate to the previous screen or pop the back stack
            //requireActivity().finish()
            findNavController().popBackStack()
        }

        // You can enable/disable the callback based on certain conditions
        // callback.isEnabled = someCondition
    }


    private fun initView() {
        binding.headerGeneric.btnBack.setImageResource(R.drawable.ic_back_24)
        binding.headerGeneric.tvHeader.text = category.categoryName +" Services"
    }

    private fun initListeners() {
        binding.btnNewService.setOnClickListener {
            val action = ServiceListFragmentDirections.actionFragmentServiceListToFragmentNewService(category)
            findNavController().navigate(action)
        }
        customerAdapter.setOnClickListener {
            val action = ServiceListFragmentDirections.actionFragmentServiceListToFragmentNewService(category,it)
            findNavController().navigate(action)
        }
        binding.headerGeneric.btnBack.setOnClickListener {
            findNavController().popBackStack()
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

        val args: ServiceListFragmentArgs by navArgs()
        category = args.category
        viewModel.getCategoryList(category.id)
        binding.headerGeneric.tvHeader.text = category.categoryName
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

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}