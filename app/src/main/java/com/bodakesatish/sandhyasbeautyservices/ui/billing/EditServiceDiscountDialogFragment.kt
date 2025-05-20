package com.bodakesatish.sandhyasbeautyservices.ui.billing

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.DialogEditServiceDiscountBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceBillingItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Locale

class EditServiceDiscountDialogFragment : DialogFragment() {

    private var _binding: DialogEditServiceDiscountBinding? = null
    private val binding get() = _binding!!

    private var serviceId: String = ""
    private var serviceName: String = ""
    private var originalPrice: Double = 0.0
    private var currentDiscountAmount: Double = 0.0
    private var currentDiscountPercentage: Double = 0.0
    private var currentIsPercentageDiscount: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditServiceDiscountBinding.inflate(LayoutInflater.from(context))

        arguments?.let {
            serviceId = it.getString(ARG_SERVICE_ID) ?: ""
            serviceName = it.getString(ARG_SERVICE_NAME) ?: "Service"
            originalPrice = it.getDouble(ARG_ORIGINAL_PRICE, 0.0)
            currentDiscountAmount = it.getDouble(ARG_CURRENT_DISCOUNT_AMOUNT, 0.0)
            currentDiscountPercentage = it.getDouble(ARG_CURRENT_DISCOUNT_PERCENTAGE, 0.0)
            currentIsPercentageDiscount = it.getBoolean(ARG_CURRENT_IS_PERCENTAGE, false)
        }

        setupViews()
        setupListeners()
        updatePreview() // Initial preview

        return MaterialAlertDialogBuilder(requireContext())//, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert
            .setView(binding.root)
            // Using custom buttons in the layout, so no need for setPositive/NegativeButton here
            // unless you want to override their behavior or use standard dialog buttons.
            .create()
    }

    private fun setupViews() {
        binding.tvDialogServiceName.text = "Service: $serviceName"
        binding.tvDialogOriginalPrice.text = "Original Price: ${formatCurrency(originalPrice)}"

        if (currentIsPercentageDiscount) {
            binding.rbPercentageDiscount.isChecked = true
            binding.etDiscountValue.setText(currentDiscountPercentage.format(1))
            binding.tilDiscountValue.suffixText = "%"
        } else {
            binding.rbFixedAmountDiscount.isChecked = true
            if (currentDiscountAmount > 0) { // Only set text if there's an actual fixed discount
                binding.etDiscountValue.setText(currentDiscountAmount.format(2))
            }
            binding.tilDiscountValue.suffixText = "₹"
        }
    }

    private fun setupListeners() {
        binding.rgDiscountType.setOnCheckedChangeListener { _, checkedId ->
            binding.etDiscountValue.text?.clear() // Clear input when type changes
            when (checkedId) {
                R.id.rb_percentage_discount -> {
                    binding.tilDiscountValue.suffixText = "%"
                    binding.etDiscountValue.hint = "Percentage"
                }
                R.id.rb_fixed_amount_discount -> {
                    binding.tilDiscountValue.suffixText = "₹"
                    binding.etDiscountValue.hint = "Amount"
                }
            }
            updatePreview()
        }

        binding.etDiscountValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePreview()
            }
        })

        binding.btnDialogApplyDiscount.setOnClickListener {
            val discountValueStr = binding.etDiscountValue.text.toString()
            val discountValue = discountValueStr.toDoubleOrNull() ?: 0.0
            val isPercentage = binding.rbPercentageDiscount.isChecked

            val (appliedDiscountAmount, appliedDiscountPercentage) = if (isPercentage) {
                Pair(0.0, discountValue) // Store percentage directly
            } else {
                Pair(discountValue, 0.0) // Store amount directly
            }

            setFragmentResult(REQUEST_KEY, bundleOf(
                RESULT_SERVICE_ID to serviceId,
                RESULT_DISCOUNT_AMOUNT to appliedDiscountAmount,
                RESULT_DISCOUNT_PERCENTAGE to appliedDiscountPercentage,
                RESULT_IS_PERCENTAGE to isPercentage
            ))
            dismiss()
        }

        binding.btnDialogCancelDiscount.setOnClickListener {
            dismiss()
        }
    }

    private fun updatePreview() {
        val discountValueStr = binding.etDiscountValue.text.toString()
        val discountInput = discountValueStr.toDoubleOrNull() ?: 0.0
        val isPercentage = binding.rbPercentageDiscount.isChecked

        val calculatedDiscount: Double
        val finalPrice: Double

        if (isPercentage) {
            val percentage = discountInput.coerceIn(0.0, 100.0) // Cap percentage
            calculatedDiscount = originalPrice * (percentage / 100.0)
        } else {
            calculatedDiscount = discountInput.coerceAtMost(originalPrice) // Cap discount at original price
        }
        finalPrice = (originalPrice - calculatedDiscount).coerceAtLeast(0.0)

        binding.tvDialogCalculatedDiscount.text = "Calculated Discount: -${formatCurrency(calculatedDiscount)}"
        binding.tvDialogFinalPricePreview.text = "New Final Price: ${formatCurrency(finalPrice)}"

        // Basic validation for Apply button
        binding.btnDialogApplyDiscount.isEnabled = discountInput >= 0 &&
                (!isPercentage || discountInput <= 100) && // Percentage validation
                (isPercentage || discountInput <= originalPrice) // Fixed amount validation
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // We are using onCreateDialog for MaterialAlertDialogBuilder, so this can be minimal or return binding.root.
        // However, for MaterialAlertDialogBuilder, the view is set in onCreateDialog.
        return binding.root // For safety, though MaterialAlertDialogBuilder handles it
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Important to prevent memory leaks
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount)
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)


    companion object {
        const val TAG = "EditServiceDiscountDialog"
        const val REQUEST_KEY = "EditServiceDiscountRequest"

        const val ARG_SERVICE_ID = "service_id"
        const val ARG_SERVICE_NAME = "service_name"
        const val ARG_ORIGINAL_PRICE = "original_price"
        const val ARG_CURRENT_DISCOUNT_AMOUNT = "current_discount_amount"
        const val ARG_CURRENT_DISCOUNT_PERCENTAGE = "current_discount_percentage"
        const val ARG_CURRENT_IS_PERCENTAGE = "current_is_percentage"

        const val RESULT_SERVICE_ID = "result_service_id"
        const val RESULT_DISCOUNT_AMOUNT = "result_discount_amount"
        const val RESULT_DISCOUNT_PERCENTAGE = "result_discount_percentage"
        const val RESULT_IS_PERCENTAGE = "result_is_percentage"


        fun newInstance(item: ServiceBillingItem): EditServiceDiscountDialogFragment {
            return EditServiceDiscountDialogFragment().apply {
                arguments = bundleOf(
                    ARG_SERVICE_ID to item.id,
                    ARG_SERVICE_NAME to item.serviceName,
                    ARG_ORIGINAL_PRICE to item.originalPrice,
                    ARG_CURRENT_DISCOUNT_AMOUNT to item.discountAmount,
                    ARG_CURRENT_DISCOUNT_PERCENTAGE to item.discountPercentage,
                    ARG_CURRENT_IS_PERCENTAGE to item.isPercentageDiscount
                )
            }
        }
    }
}