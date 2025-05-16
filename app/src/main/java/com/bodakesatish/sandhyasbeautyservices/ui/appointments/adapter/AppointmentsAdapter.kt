package com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.databinding.ListRowAppointmentBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentCustomer
import com.bodakesatish.sandhyasbeautyservices.util.DateHelper

class AppointmentsAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemList: List<AppointmentCustomer> = emptyList()
    var onBatchSelected: ((AppointmentCustomer) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            ListRowAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AppointmentViewHolder -> {
                holder.bind(itemList[position], position)
            }
        }
    }

    // Modified setData method to use DiffUtil
    fun setData(data: List<AppointmentCustomer>) {
        val diffResult = DiffUtil.calculateDiff(AppointmentDiffCallback(this.itemList, data))
        itemList = data // Update the internal data list
        diffResult.dispatchUpdatesTo(this) // Dispatch the calculated updates to the adapter
    }

    fun setOnClickListener(onBatchSelected: ((AppointmentCustomer)) -> Unit) {
        this.onBatchSelected = onBatchSelected
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class AppointmentViewHolder(val binding: ListRowAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: AppointmentCustomer, position: Int) {

            binding.tvNumber.text = "${position+1}"
            binding.tvCustomerName.text = "${data.customer.firstName}  ${data.customer.lastName}"
            binding.tv3.text = DateHelper.getFormattedDate(data.appointment.appointmentDate, "dd-MMM-yyyy")
            binding.root.setOnClickListener {
                onBatchSelected?.invoke(data)
            }
        }
    }

    // DiffUtil.Callback for your AppointmentCustomer list
    class AppointmentDiffCallback(
        private val oldList: List<AppointmentCustomer>,
        private val newList: List<AppointmentCustomer>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Check if the items represent the same logical entity (compare appointment IDs)
            return oldList[oldItemPosition].appointment.id == newList[newItemPosition].appointment.id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Check if the visual representation of the item has changed
            // Compare relevant properties like appointment details, customer name, etc.
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return oldItem.appointment == newItem.appointment &&
                    oldItem.customer == newItem.customer
            // You might need to compare more specific fields if equality check on the
            // data classes themselves isn't sufficient (e.g., if they contain lists
            // that need deep comparison).
        }
    }

}

