package com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.databinding.ItemCategoryHeaderDialogBinding
import com.bodakesatish.sandhyasbeautyservices.databinding.ItemServiceDialogBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service

class ServiceDialogAdapter(
    private val onServiceSelectedChange: (Service) -> Unit // Add a lambda for the callback
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {//(ServiceItemDiffCallback()) {

    var itemList = ArrayList<CategoryWithServiceViewItem>()

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
                ServiceViewHolder(binding,onServiceSelectedChange)
            }

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList.get(position)
        when (holder) {
            is CategoryViewHolder -> {
                val headerItem = item as CategoryWithServiceViewItem.CategoryHeader
                holder.bind(headerItem.category)
            }

            is ServiceViewHolder -> {
                val serviceItem = item as CategoryWithServiceViewItem.ServiceItem
                holder.bind(serviceItem.service)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList.get(position)) {
            is CategoryWithServiceViewItem.CategoryHeader -> VIEW_TYPE_HEADER
            is CategoryWithServiceViewItem.ServiceItem -> VIEW_TYPE_SERVICE
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setData(items: ArrayList<CategoryWithServiceViewItem>) {
        itemList = items
        notifyDataSetChanged()
    }

    fun getSelectedServices() : List<Service> {
        val selectedArrayList = ArrayList<Service>()
        itemList.filterIsInstance<CategoryWithServiceViewItem.ServiceItem>()
            .filter { it.service.isSelected }
            .forEach { selectedArrayList.add(it.service) }

        return selectedArrayList
    }

    class CategoryViewHolder(private val binding: ItemCategoryHeaderDialogBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.textViewCategoryName.text = category.categoryName
        }
    }

    class ServiceViewHolder(
        private val binding: ItemServiceDialogBinding,
        private val onServiceSelectedChange: (Service) -> Unit // Pass the lambda to the ViewHolder
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(service: Service) {
            binding.textViewServiceName.text = service.serviceName
            binding.textViewServicePrice.text = "$${service.servicePrice}"
            binding.checkBoxService.setOnCheckedChangeListener { _, isChecked ->
                service.isSelected = isChecked // Update the isSelected property
                // Instead, create a new Service object with the updated state
                val updatedService = service.copy(isSelected = isChecked)
                // Call the callback with the updated service
                onServiceSelectedChange.invoke(updatedService)
            }
            // Set initial state
            binding.checkBoxService.isChecked = service.isSelected // Set initial state if needed
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