package com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.databinding.ListRowSelectedServiceBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service

class SelectedServicesAdapter() : ListAdapter<Service, SelectedServicesAdapter.AppointmentViewHolder>(ServiceDiffCallback()) {

    // No need for a separate itemList variable; ListAdapter manages the list internally.
    // No need for a separate setData function; use submitList() provided by ListAdapter.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding =
            ListRowSelectedServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        // getItem(position) is provided by ListAdapter to get the item at the current position.
        val service = getItem(position)
        holder.bind(service)
    }

    // No need for getItemCount(); ListAdapter handles this.
    // No need for setOnClickListener(); it's passed in the constructor.

    // ViewHolder can be an inner class or a top-level class.
    // If it doesn't need access to SelectedServicesAdapter's members other than what's passed,
    // it could even be a static nested class (by removing `inner`).

    inner class AppointmentViewHolder(
        private val binding: ListRowSelectedServiceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // The 'position' parameter is often not needed in bind if the click listener
        // already receives the specific 'Service' object.
        fun bind(service: Service) {
            binding.tvServiceName.text = service.serviceName
            binding.tvServicePrice.text = "${service.servicePrice} Rs." // Consider using string resources for "Rs."
            binding.root.setOnClickListener {
              //  onItemClicked(service)
            }
        }
    }

    // DiffUtil.ItemCallback is used with ListAdapter.
    // It can be a companion object or a separate top-level class.
    private class ServiceDiffCallback : DiffUtil.ItemCallback<Service>() {
        override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean {
            // Check if the items represent the same logical entity (e.g., compare unique IDs)
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean {
            // Check if the visual representation of the item has changed.
            // If 'Service' is a data class, its auto-generated equals() will compare all properties.
            return oldItem == newItem
        }
    }

}