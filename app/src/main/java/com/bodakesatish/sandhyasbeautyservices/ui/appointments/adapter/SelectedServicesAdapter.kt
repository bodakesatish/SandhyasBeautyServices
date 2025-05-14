package com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.databinding.ListRowSelectedServiceBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service

class SelectedServicesAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemList: List<Service> = emptyList()
    var onBatchSelected: ((Service) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            ListRowSelectedServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {

            is AppointmentViewHolder -> {
                holder.bind(itemList[position], position)
            }

        }
    }

    // Use DiffUtil for efficient updates
    fun setData(data: List<Service>) {
        val diffResult = DiffUtil.calculateDiff(ServiceDiffCallback(this.itemList, data))
        this.itemList = data
        diffResult.dispatchUpdatesTo(this)
    }

    fun setOnClickListener(onBatchSelected: ((Service)) -> Unit) {
        this.onBatchSelected = onBatchSelected
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class AppointmentViewHolder(val binding: ListRowSelectedServiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Service, position: Int) {

            binding.tvServiceName.text = data.serviceName
            binding.tvServicePrice.text = "${data.servicePrice} Rs."

            binding.root.setOnClickListener {
                onBatchSelected?.invoke(data)
            }
        }

    }

    // DiffUtil.Callback for your Service list
    class ServiceDiffCallback(
        private val oldList: List<Service>,
        private val newList: List<Service>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Check if the items represent the same logical entity (compare service IDs)
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Check if the visual representation of the item has changed
            // Compare relevant properties like name, price, etc.
            // Assuming 'Service' is a data class and equals() compares content
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}