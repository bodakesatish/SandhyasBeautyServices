package com.bodakesatish.sandhyasbeautyservices.ui.appointment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.ItemCategoryHeaderDialogBinding
import com.bodakesatish.sandhyasbeautyservices.databinding.ItemServiceDialogBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service

class ServiceDialogAdapter(
    private val onServiceSelected: (Service, Boolean) -> Unit // Callback for selection changes
) : ListAdapter<CategoryWithServiceViewItem, RecyclerView.ViewHolder>(ServiceItemDiffCallback()) { // Use ListAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemCategoryHeaderDialogBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                CategoryViewHolder(binding)
            }

            VIEW_TYPE_SERVICE -> {
                val binding = ItemServiceDialogBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ServiceViewHolder(binding, onServiceSelected) // Pass the listener
            }

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryViewHolder -> {
                val headerItem = getItem(position) as CategoryWithServiceViewItem.CategoryHeader
                holder.bind(headerItem.category)
            }

            is ServiceViewHolder -> {
                val serviceItem = getItem(position) as CategoryWithServiceViewItem.ServiceItem
                holder.bind(serviceItem.service)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CategoryWithServiceViewItem.CategoryHeader -> VIEW_TYPE_HEADER
            is CategoryWithServiceViewItem.ServiceItem -> VIEW_TYPE_SERVICE
        }
    }

    // DiffUtil for efficient list updates
    class ServiceItemDiffCallback : DiffUtil.ItemCallback<CategoryWithServiceViewItem>() {
        override fun areItemsTheSame(
            oldItem: CategoryWithServiceViewItem,
            newItem: CategoryWithServiceViewItem
        ): Boolean {
            return when {
                oldItem is CategoryWithServiceViewItem.CategoryHeader && newItem is CategoryWithServiceViewItem.CategoryHeader ->
                    oldItem.category == newItem.category

                oldItem is CategoryWithServiceViewItem.ServiceItem && newItem is CategoryWithServiceViewItem.ServiceItem ->
                    oldItem.service == newItem.service // Compare based on unique ID
                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: CategoryWithServiceViewItem,
            newItem: CategoryWithServiceViewItem
        ): Boolean {
            // Data classes provide an automatic equals implementation based on properties
            return oldItem == newItem
        }
    }

    class CategoryViewHolder(private val binding: ItemCategoryHeaderDialogBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.textViewCategoryName.text = category.categoryName
        }
    }

    class ServiceViewHolder(
        private val binding: ItemServiceDialogBinding,
        private val onServiceSelected: (Service, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {

        }

        fun bind(serviceItem: Service) {
            binding.textViewServiceName.text = serviceItem.serviceName
            binding.textViewServicePrice.text = "Rs. ${serviceItem.servicePrice}"

            // Temporarily remove listener to prevent unwanted calls during re-binding
            binding.checkBoxService.setOnCheckedChangeListener(null)

            // Set initial state based on the model
            binding.checkBoxService.isChecked = serviceItem.isSelected

            binding.checkBoxService.setOnCheckedChangeListener { _, isChecked ->
                // Call the callback with the service and the new checked state
                onServiceSelected(serviceItem, isChecked)
            }

            // Make the entire row clickable to toggle the checkbox
            binding.root.setOnClickListener {
                // Toggle the checkbox state and invoke the callback
                val newCheckedState = !binding.checkBoxService.isChecked
                // binding.checkBoxService.isChecked = newCheckedState // Let bind handle this to avoid listener loops
                onServiceSelected(
                    /* pass the current service item here, you'll need to hold a reference to it */
                    serviceItem, newCheckedState,
                )
            }

            if (serviceItem.isSelected) {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.selected_service_background
                    )
                ) // Define this color

            } else {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        android.R.color.transparent
                    )
                )
            }

        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_SERVICE = 1
    }
}

sealed class CategoryWithServiceViewItem {
    data class CategoryHeader(val category: Category) : CategoryWithServiceViewItem()
    data class ServiceItem(val service: Service) : CategoryWithServiceViewItem()
}