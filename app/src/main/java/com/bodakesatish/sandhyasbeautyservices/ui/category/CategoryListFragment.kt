package com.bodakesatish.sandhyasbeautyservices.ui.category

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
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentCategoryListBinding
import com.bodakesatish.sandhyasbeautyservices.ui.category.adapter.CategoryListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class CategoryListFragment : Fragment() {

    private var _binding: FragmentCategoryListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: CategoryListViewModel by viewModels()

    private var customerAdapter : CategoryListAdapter = CategoryListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCategoryListBinding.inflate(inflater, container, false)
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
            //requireActivity().finish()
            findNavController().popBackStack()
        }

        // You can enable/disable the callback based on certain conditions
        // callback.isEnabled = someCondition
    }

    private fun initView() {
        binding.headerGeneric.tvHeader.text = "All Category"
    }

    private fun initListeners() {
        binding.btnNewCategory.setOnClickListener {
            findNavController().navigate(R.id.navigation_add_category)
        }
        customerAdapter.setOnClickListener { category ->
            val action = CategoryListFragmentDirections.actionFragmentCategoryListToFragmentServiceList(category)
            findNavController().navigate(action)
        }
        customerAdapter.setOnLongClickListener { category ->
            val action = CategoryListFragmentDirections.actionFragmentCategoryListToFragmentEditCategory(category)
            findNavController().navigate(action)
        }
//        binding.headerGeneric.btnBack.setOnClickListener {
//            findNavController().popBackStack()
//        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categoryList.collect { data ->
                    Log.d(tag, "$tag->initObservers collect->categoryList")
                    // Update UI with the received data
                    customerAdapter.setData(data)
                }
            }
        }
    }

    private fun initData() {
        binding.rvCategoryList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvCategoryList.adapter = customerAdapter
//        binding.rvCategoryList.addItemDecoration(
//            DividerItemDecoration(
//                requireContext(),
//                DividerItemDecoration.VERTICAL
//            )
//        )
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