package com.bodakesatish.sandhyasbeautyservices.ui.billing

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.databinding.ListItemServiceBillingBinding // Ensure this matches your XML file name
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetailWithService
import java.text.NumberFormat
import java.util.Locale

class BillServiceListAdapter(
    private val onEditDiscountClicked: (ServiceDetailWithService) -> Unit
) : ListAdapter<ServiceDetailWithService, BillServiceListAdapter.ServiceViewHolder>(ServiceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ListItemServiceBillingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onEditDiscountClicked)
    }

    inner class ServiceViewHolder(private val binding: ListItemServiceBillingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ServiceDetailWithService, onEditDiscountClicked: (ServiceDetailWithService) -> Unit) {
            binding.tvServiceNameBilling.text = item.serviceName
            val formattedOriginalPrice = formatCurrency(item.originalPrice)

            binding.tvServiceOriginalPriceBilling.text = formattedOriginalPrice
            // Clear strikethrough initially
            binding.tvServiceOriginalPriceBilling.paintFlags =
                binding.tvServiceOriginalPriceBilling.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            val calculatedDiscountValue = item.calculatedDiscountValue

            if (calculatedDiscountValue > 0) {
                binding.tvServiceOriginalPriceBilling.paintFlags =
                    binding.tvServiceOriginalPriceBilling.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                val discountValue = item.calculatedDiscountValue
                val discountText = "(-${formatCurrency(discountValue)})"
                binding.tvServiceDiscountInfoCompact.text = discountText
                binding.tvServiceDiscountInfoCompact.visibility = View.VISIBLE

                binding.tvServiceFinalPriceDisplay.text =
                    "→ Final: ${formatCurrency(item.finalPrice)}"
                binding.tvServiceFinalPriceDisplay.visibility = View.VISIBLE

            } else {
                binding.tvServiceDiscountInfoCompact.visibility = View.GONE
                // Show original price as final if no discount, or adjust as needed
                binding.tvServiceFinalPriceDisplay.text = "Final: ${formatCurrency(item.originalPrice)}"
                // Or if you want to hide it completely when no discount:
                // binding.tvServiceFinalPriceDisplay.visibility = View.GONE
            }

            binding.btnEditServiceDiscount.setOnClickListener {
                onEditDiscountClicked(item)
            }
        }
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount) // For INR
    }

    class ServiceDiffCallback : DiffUtil.ItemCallback<ServiceDetailWithService>() {
        override fun areItemsTheSame(oldItem: ServiceDetailWithService, newItem: ServiceDetailWithService): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ServiceDetailWithService, newItem: ServiceDetailWithService): Boolean {
            return oldItem == newItem
        }
    }
}