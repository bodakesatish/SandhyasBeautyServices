package com.bodakesatish.sandhyasbeautyservices.ui.billing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentBillingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BillingFragment : Fragment() {

    private var _binding: FragmentBillingBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: BillingViewModel by viewModels()
    private lateinit var billingServicesAdapter: BillingServicesAdapter



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBillingBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupFragmentResultListener()

    }

    private fun setupRecyclerView() {
        billingServicesAdapter = BillingServicesAdapter { serviceItem ->
            // Handle click to edit discount for a service item
            val dialog = EditServiceDiscountDialogFragment.newInstance(serviceItem)
            dialog.show(childFragmentManager, EditServiceDiscountDialogFragment.TAG)
        }
        binding.rvServicesForBilling.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = billingServicesAdapter
            // Optional: Add ItemDecoration for dividers if not handled by item layout
            // addItemDecoration(MaterialDividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun observeViewModel() {
        viewModel.serviceItems.observe(viewLifecycleOwner) { items ->
            billingServicesAdapter.submitList(items)
        }

        viewModel.subTotal.observe(viewLifecycleOwner) { subTotal ->
            binding.tvValueSubtotal.text = viewModel.formatCurrency(subTotal)
        }

        viewModel.totalDiscount.observe(viewLifecycleOwner) { discount ->
            binding.tvValueTotalDiscount.text = "- ${viewModel.formatCurrency(discount)}"
            // Show/hide discount row based on value
            binding.tvValueTotalDiscount.visibility = if (discount > 0) View.VISIBLE else View.GONE
        }

        viewModel.netAmount.observe(viewLifecycleOwner) { netAmount ->
            binding.tvValueGrandTotal.text = viewModel.formatCurrency(netAmount)
        }

        // You might have other LiveData for customer name, date, etc.
        // binding.tvCustomerNameBilling.text = "Customer: Sample Name"
        // binding.tvBillingDate.text = "Date: ${java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}"
    }

    private fun setupFragmentResultListener() {
        childFragmentManager.setFragmentResultListener(
            EditServiceDiscountDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val serviceId = bundle.getString(EditServiceDiscountDialogFragment.RESULT_SERVICE_ID) ?: return@setFragmentResultListener
            val discountAmount = bundle.getDouble(EditServiceDiscountDialogFragment.RESULT_DISCOUNT_AMOUNT, 0.0)
            val discountPercentage = bundle.getDouble(EditServiceDiscountDialogFragment.RESULT_DISCOUNT_PERCENTAGE, 0.0)
            val isPercentage = bundle.getBoolean(EditServiceDiscountDialogFragment.RESULT_IS_PERCENTAGE, false)

            viewModel.updateServiceDiscount(serviceId, discountAmount, discountPercentage, isPercentage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvServicesForBilling.adapter = null // Clear adapter to prevent memory leaks
        _binding = null
    }

}