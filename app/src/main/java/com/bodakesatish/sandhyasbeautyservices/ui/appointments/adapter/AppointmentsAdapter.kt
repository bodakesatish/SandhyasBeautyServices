package com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.databinding.ListRowAppointmentBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
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

    fun setData(data: List<AppointmentCustomer>) {
        itemList = data
        notifyItemRangeChanged(0, data.size)
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

            binding.tv1.text = "${position+1}"
            binding.tv2.text = "${data.customer.firstName}  ${data.customer.lastName}"
            binding.tv3.text = DateHelper.getFormattedDate(data.appointment.appointmentDate, "dd-MMM-yyyy")
            binding.root.setOnClickListener {
                onBatchSelected?.invoke(data)
            }
        }

    }
}