package com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
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

    fun setData(data: List<Service>) {
        itemList = data
        notifyItemRangeChanged(0, data.size)
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
}