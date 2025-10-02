package com.bodakesatish.sandhyasbeautyservices.ui.appointment.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.databinding.ListServiceBilBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetailWithService
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class ServiceDiscountAdapter() : ListAdapter<ServiceDetailWithService, ServiceDiscountAdapter.ServiceViewHolder>(ServiceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ListServiceBilBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(position+1,item)
    }

    inner class ServiceViewHolder(private val binding: ListServiceBilBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int,item: ServiceDetailWithService) {
            binding.tvServiceNameBilling.text = "$position) ${item.serviceName}"
            val formattedOriginalPrice = formatCurrency(item.originalPrice)

            binding.tvServiceOriginalPriceBilling.text = formattedOriginalPrice
            // Clear strikethrough initially
            binding.tvServiceOriginalPriceBilling.paintFlags =
                binding.tvServiceOriginalPriceBilling.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            val calculatedDiscountValue = item.calculatedDiscountValue

            if (calculatedDiscountValue > 0) {
                binding.tvServiceOriginalPriceBilling.visibility = View.VISIBLE
                binding.tvServiceOriginalPriceBilling.paintFlags =
                    binding.tvServiceOriginalPriceBilling.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                val discountValue = item.calculatedDiscountValue
                val discountText = "(-${formatCurrency(discountValue)})"
                binding.tvServiceDiscountInfoCompact.text = discountText
                binding.tvServiceDiscountInfoCompact.visibility = View.VISIBLE

                binding.tvServiceFinalPriceDisplay.text =
                    "→${formatCurrency(item.finalPrice)}"
                binding.tvServiceFinalPriceDisplay.visibility = View.VISIBLE

            } else {
                binding.tvServiceOriginalPriceBilling.visibility = View.INVISIBLE
                binding.tvServiceDiscountInfoCompact.visibility = View.GONE
                // Show original price as final if no discount, or adjust as needed
                binding.tvServiceFinalPriceDisplay.text = "${formatCurrency(item.originalPrice)}"
                // Or if you want to hide it completely when no discount:
                // binding.tvServiceFinalPriceDisplay.visibility = View.GONE
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

    fun formatDoubleRemoveTrailingZeros(number: Double): String {
        // Option A: Ensure at least one decimal place, remove unnecessary trailing zeros
        val df = DecimalFormat("#,##0.0###") // Shows at least one decimal, up to 4.
        // Adjust the number of '#' after '.' as needed.
        // Using "," for grouping separator, "." for decimal.
        // To use system's default locale for separators:
        // val df = NumberFormat.getInstance() as DecimalFormat
        // df.applyPattern("#,##0.0###")


        // Option B: More flexible if you only want to remove .00 -> .0 but keep .50 as .50
        // This is a bit trickier with DecimalFormat directly for the *exact* "100.00 -> 100.0"
        // and "100.50 -> 100.5" but "100.55 -> 100.55"
        // For that, you might format to a fixed number of decimals and then trim.

        return df.format(number)
    }
}