package com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.databinding.ListRowSelectedServiceBinding // Generated binding class
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service // Your Service model

class SelectedServicesAdapter(
    private val onRemoveClicked: (Service) -> Unit // Lambda for remove action
) : ListAdapter<Service, SelectedServicesAdapter.ServiceViewHolder>(ServiceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ListRowSelectedServiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServiceViewHolder(binding, onRemoveClicked)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = getItem(position)
        holder.bind(service)
    }

    class ServiceViewHolder(
        private val binding: ListRowSelectedServiceBinding,
        private val onRemoveClicked: (Service) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(service: Service) {
            binding.tvServiceNameRow.text = service.serviceName
            // Ensure you have a currency symbol defined in your strings.xml
            // e.g., <string name="currency_symbol_pkr">₹%s</string>
            binding.tvServicePriceRow.text = ""+service.servicePrice
//                binding.root.context.getString(
//                R.string.currency_symbol_pkr, // Or your general currency string
//                String.format("%.2f", service.servicePrice)
//            )

            binding.ivRemoveService.setOnClickListener {
                onRemoveClicked(service)
            }
        }
    }

    class ServiceDiffCallback : DiffUtil.ItemCallback<Service>() {
        override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean {
            return oldItem.id == newItem.id // Assuming 'id' is unique
        }

        override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean {
            return oldItem == newItem // If Service is a data class, this works for content comparison
        }
    }

}