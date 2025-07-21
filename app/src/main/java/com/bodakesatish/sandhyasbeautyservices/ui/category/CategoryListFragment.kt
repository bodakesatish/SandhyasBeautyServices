package com.bodakesatish.sandhyasbeautyservices.ui.category

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentCategoryListBinding
import com.bodakesatish.sandhyasbeautyservices.ui.category.adapter.CategoryListAdapter
import com.google.android.material.snackbar.Snackbar
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

    private lateinit var customerAdapter: CategoryListAdapter // Make lateinit, initialize in onViewCreated

    // private val tag = this.javaClass.simpleName // You can define tag if needed for logging
    // Using default Fragment tag or passed tag is also fine.
    // For consistency with your previous code, let's use the Fragment's simple name.
    private val localTag: String = this.javaClass.simpleName


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
        initObservers() // Call this after adapter is initialized
        initData()      // Call this after adapter is initialized, or combine with initView
        onBackPressed()


        // Initial data load if not handled by ViewModel's init block
        if (savedInstanceState == null) { // Load only once, not on config changes
            //viewModel.getCategoryList()
        }
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
        // Initialize adapter here, once binding is available
        customerAdapter = CategoryListAdapter()
        binding.rvCategoryList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvCategoryList.adapter = customerAdapter
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
        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d(tag, "Swipe to refresh triggered.")
            viewModel.getCategoryList(forceToRefresh = true)
            // Optionally clear search when swipe refreshing
            // binding.searchScheme.text?.clear()
            // viewModel.onSearchQueryChanged("")
        }
//        binding.headerGeneric.btnBack.setOnClickListener {
//            findNavController().popBackStack()
//        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    Log.d(localTag, "$localTag->initObservers collect->uiState: $uiState")

                    // --- Loading State Handling ---
                    // For SwipeRefreshLayout, it's usually always tied to the overall loading state.
                    binding.swipeRefreshLayout.isRefreshing = uiState.isLoading

                    // More granular control for a central ProgressBar:
                    // Show central ProgressBar ONLY when:
                    // 1. isLoading is true
                    // 2. AND there are no categories currently displayed (initial load or error previously cleared list)
                    // This prevents the ProgressBar from showing over existing list items during a refresh.
                    binding.progressBar.isVisible = uiState.isLoading && uiState.categories.isEmpty() && uiState.errorMessage == null

                    // --- Error Message Handling ---
                    uiState.errorMessage?.let { message ->
                        // If categories are empty, it's a full-screen error. Otherwise, it might be a Snackbar for a failed refresh.
                        if (uiState.categories.isEmpty()) {
                            binding.tvErrorMessage.text = message // Assuming you have a TextView for full errors
                            binding.tvErrorMessage.isVisible = true
                            binding.rvCategoryList.isVisible = false // Hide RecyclerView
                            binding.tvEmptyMessage.isVisible = false // Hide empty message
                        } else {

                            binding.tvErrorMessage.isVisible = false // Ensure full error message is hidden
                        }
                        // Show Snackbar for errors when data is already present (e.g., refresh failed)
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                            .setAction("Dismiss") { /* Optional action */ }
                            .show()
                        // Important: Notify the ViewModel that the message has been shown
                        viewModel.userMessageShown()
                    } ?: run {
                        // No error, hide the error message view
                        binding.tvErrorMessage.isVisible = false
                    }

                    // --- Empty State Handling (after loading and no error) ---
                    // Show empty message ONLY if:
                    // 1. Not loading
                    // 2. No error message is being shown
                    // 3. Categories list is actually empty
                    val isListEmpty = uiState.categories.isEmpty()
                    binding.tvEmptyMessage.isVisible = !uiState.isLoading && uiState.errorMessage == null && isListEmpty

                    // --- RecyclerView Visibility and Data Submission ---
                    // RecyclerView should be visible if:
                    // 1. Not loading (unless you want to show stale data during refresh, then remove !uiState.isLoading)
                    // 2. No full-screen error is shown
                    // 3. And ideally, if the list is not empty (though ListAdapter handles empty lists fine)
                    //    Let's make it visible if there's no error and it's not the initial full load.
                    if (uiState.errorMessage == null || !uiState.categories.isEmpty()) {
                        binding.rvCategoryList.isVisible = true
                    }


                    // Submit list to adapter.
                    // ListAdapter handles diffing efficiently. It's okay to submit an empty list
                    // or the same list if nothing changed.
                    customerAdapter.submitList(uiState.categories)

                    // If you want to explicitly hide RecyclerView during initial load and show it only when data arrives:
                    // binding.rvCategoryList.isVisible = !uiState.isLoading && uiState.errorMessage == null && !uiState.categories.isEmpty()
                    // However, the above logic for progressBar and tvEmptyMessage often covers this visually.
                    // The main goal is to avoid showing an empty RecyclerView briefly before data loads.
                    // The ListAdapter itself handles empty states gracefully by showing nothing.
                }
            }
        }
    }

    private fun initData() {

//        binding.rvCategoryList.addItemDecoration(
//            DividerItemDecoration(
//                requireContext(),
//                DividerItemDecoration.VERTICAL
//            )
//        )
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}