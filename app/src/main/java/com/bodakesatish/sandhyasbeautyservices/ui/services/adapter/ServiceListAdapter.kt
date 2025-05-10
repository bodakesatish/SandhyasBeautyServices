package com.bodakesatish.sandhyasbeautyservices.ui.services.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.databinding.ListRowServiceBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service

class ServiceListAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemList: List<Service> = emptyList()
    var onBatchSelected: ((Service) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ListRowServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {

            is ServiceViewHolder -> {
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

    inner class ServiceViewHolder(val binding: ListRowServiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Service, position: Int) {

            binding.tvServiceName.text = "${position + 1}. ${data.serviceName}"
            binding.tvServiceDescription.text = data.serviceDescription
            binding.tvServicePrice.text = "₹${data.servicePrice}"

            binding.root.setOnClickListener {
                onBatchSelected?.invoke(data)
            }
        }

    }
}