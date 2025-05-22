package com.bodakesatish.sandhyasbeautyservices.ui.appointment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.ListRowAppointmentBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.CustomerAppointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentStatus
import com.bodakesatish.sandhyasbeautyservices.util.DateHelper
import java.text.NumberFormat
import java.util.Locale

class AppointmentListAdapter(
    private val onItemClicked: (CustomerAppointment) -> Unit
) : ListAdapter<CustomerAppointment, AppointmentListAdapter.AppointmentViewHolder>(AppointmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding =
            ListRowAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding, onItemClicked, parent.context)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, position)
    }

    class AppointmentViewHolder(
        private val binding: ListRowAppointmentBinding,
        private val onItemClicked: (CustomerAppointment) -> Unit,
        private val context: Context // Needed for ContextCompat.getColor/getDrawable
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CustomerAppointment, itemNumber: Int) {
            binding.tvNumber.text = "${itemNumber+1}."
            binding.tvCustomerName.text = "${data.customer.firstName} ${data.customer.lastName}"

            val formattedDate = DateHelper.getFormattedDate(data.appointment.appointmentDate, "dd MMM yyyy") // Or your preferred format
            val formattedTime = DateHelper.formatTime(data.appointment.appointmentTime) // Use as is if already a formatted string

            binding.tvAppointmentDatetime.text = if (formattedTime.toString().isNotBlank()) {
                "$formattedDate, $formattedTime"
            } else {
                formattedDate
            }
            binding.ivDatetimeIcon.setImageResource(R.drawable.ic_calendar_month) // Ensure you have this drawable

            if (!data.appointment.appointmentNotes.isNullOrBlank()) {
                binding.tvAppointmentServicesSummary.text = data.appointment.appointmentNotes
                binding.tvAppointmentServicesSummary.visibility = View.VISIBLE
            } else {
                binding.tvAppointmentServicesSummary.visibility = View.GONE
            }

            data.appointment.appointmentStatus?.let { status ->
                binding.tvAppointmentStatus.text = status.name // Or a more user-friendly string from string resources
                val statusTextColorRes: Int
                val statusIndicatorColorRes: Int
                val statusCardStrokeColorRes: Int

                when (status) {
                    AppointmentStatus.PENDING -> {
                        statusTextColorRes = R.color.colorScheduledText // Or use R.color.colorScheduled if text and border are same
                        statusIndicatorColorRes = R.color.colorScheduled
                        statusCardStrokeColorRes = R.color.colorScheduled
                    }
                    AppointmentStatus.COMPLETED -> {
                        statusTextColorRes = R.color.colorCompletedText
                        statusIndicatorColorRes = R.color.colorCompleted
                        statusCardStrokeColorRes = R.color.colorCompleted
                    }
                    else -> {
                        statusTextColorRes = R.color.textColorSecondary // A neutral default
                        statusIndicatorColorRes = android.R.color.transparent // No specific indicator color
                        statusCardStrokeColorRes = R.color.textColorSecondary
                    }
                }
                binding.tvAppointmentStatus.setTextColor(ContextCompat.getColor(context, statusTextColorRes))
                binding.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(context, statusIndicatorColorRes))
                binding.tvAppointmentStatusCard.strokeColor = ContextCompat.getColor(context, statusCardStrokeColorRes)

            } ?: run {
                binding.tvAppointmentStatus.text = context.getString(R.string.status_not_available) // Define R.string.status_not_available as "N/A"
                binding.tvAppointmentStatus.setTextColor(ContextCompat.getColor(context, R.color.textColorSecondary))
                binding.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                binding.tvAppointmentStatusCard.strokeColor = ContextCompat.getColor(context, R.color.textColorSecondary)
            }

            data.appointment.totalBillAmount?.let { amount ->
                val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault()) // Or your app's specific locale
                binding.tvTotalAmount.text = format.format(amount)
                binding.tvTotalAmount.visibility = View.VISIBLE
            } ?: run {
                binding.tvTotalAmount.visibility = View.GONE // Or View.INVISIBLE to maintain layout space
            }
            binding.root.setOnClickListener {
                onItemClicked(data)
            }
        }
    }
    class AppointmentDiffCallback : DiffUtil.ItemCallback<CustomerAppointment>() {
        override fun areItemsTheSame(oldItem: CustomerAppointment, newItem: CustomerAppointment): Boolean {
            return oldItem.appointment.id == newItem.appointment.id
        }
        override fun areContentsTheSame(oldItem: CustomerAppointment, newItem: CustomerAppointment): Boolean {
            return oldItem == newItem
        }
    }
}