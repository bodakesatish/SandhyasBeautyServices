package com.bodakesatish.sandhyasbeautyservices.ui.customerlist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.databinding.ListRowCustomerBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer

class CustomerListAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemList: List<Customer> = emptyList()
    var onBatchSelected: ((Customer) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            ListRowCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PatientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {

            is PatientViewHolder -> {
                holder.bind(itemList[position], position)
            }

        }
    }

    fun setData(data: List<Customer>) {
        itemList = data
        notifyItemRangeChanged(0, data.size)
    }

    fun setOnClickListener(onBatchSelected: ((Customer)) -> Unit) {
        this.onBatchSelected = onBatchSelected
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class PatientViewHolder(val binding: ListRowCustomerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Customer, position: Int) {

            binding.tvNumber.text = "${position + 1}."
            binding.tvCustomerName.text = "${data.firstName}"
            binding.tvCustomerPhone.text = "${data.lastName}"

            binding.root.setOnClickListener {
                onBatchSelected?.invoke(data)
            }
        }

    }
}