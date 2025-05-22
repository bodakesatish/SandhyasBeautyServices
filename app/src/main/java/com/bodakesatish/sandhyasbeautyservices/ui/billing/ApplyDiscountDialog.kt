package com.bodakesatish.sandhyasbeautyservices.ui.billing

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.bodakesatish.sandhyasbeautyservices.databinding.DialogEditServiceDiscountBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetailWithService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Locale

class ApplyDiscountDialog : DialogFragment() {

    private var _binding: DialogEditServiceDiscountBinding? = null
    private val binding get() = _binding!!

    private var serviceId: Int = 0
    private var serviceName: String = ""
    private var originalPrice: Double = 0.0
    private var currentDiscountAmount: Double = 0.0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditServiceDiscountBinding.inflate(LayoutInflater.from(context))

        arguments?.let {
            serviceId = it.getInt(ARG_SERVICE_ID, 0)
            serviceName = it.getString(ARG_SERVICE_NAME, "Service")
            originalPrice = it.getDouble(ARG_ORIGINAL_PRICE, 0.0)
            currentDiscountAmount = it.getDouble(ARG_CURRENT_DISCOUNT_AMOUNT, 0.0)
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

        if (currentDiscountAmount > 0) { // Only set text if there's an actual fixed discount
            binding.etDiscountValue.setText(currentDiscountAmount.format(2))
        }
        binding.tilDiscountValue.suffixText = "₹"

    }

    private fun setupListeners() {
       // binding.etDiscountValue.hint = "Amount"
        binding.etDiscountValue.text?.clear() // Clear input when type changes
        updatePreview()

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

            val (appliedDiscountAmount, appliedDiscountPercentage) = Pair(discountValue, 0.0)

            setFragmentResult(
                REQUEST_KEY, bundleOf(
                    RESULT_SERVICE_ID to serviceId,
                    RESULT_DISCOUNT_AMOUNT to appliedDiscountAmount,
                    RESULT_DISCOUNT_PERCENTAGE to appliedDiscountPercentage,
                )
            )
            dismiss()
        }

        binding.btnDialogCancelDiscount.setOnClickListener {
            dismiss()
        }
    }

    private fun updatePreview() {
        val discountValueStr = binding.etDiscountValue.text.toString()
        val discountInput = discountValueStr.toDoubleOrNull() ?: 0.0

        val calculatedDiscount: Double
        val finalPrice: Double


        calculatedDiscount =
            discountInput.coerceAtMost(originalPrice) // Cap discount at original price

        finalPrice = (originalPrice - calculatedDiscount).coerceAtLeast(0.0)

        binding.tvDialogCalculatedDiscount.text =
            "Calculated Discount: -${formatCurrency(calculatedDiscount)}"
        binding.tvDialogFinalPricePreview.text = "New Final Price: ${formatCurrency(finalPrice)}"

        // Basic validation for Apply button
        binding.btnDialogApplyDiscount.isEnabled = discountInput >= 0 && discountInput <= originalPrice

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        const val RESULT_SERVICE_ID = "result_service_id"
        const val RESULT_DISCOUNT_AMOUNT = "result_discount_amount"
        const val RESULT_DISCOUNT_PERCENTAGE = "result_discount_percentage"


        fun newInstance(item: ServiceDetailWithService): ApplyDiscountDialog {
            return ApplyDiscountDialog().apply {
                arguments = bundleOf(
                    ARG_SERVICE_ID to item.serviceId,
                    ARG_SERVICE_NAME to item.serviceName,
                    ARG_ORIGINAL_PRICE to item.originalPrice,
                    ARG_CURRENT_DISCOUNT_AMOUNT to item.discountAmount,
                )
            }
        }
    }
}