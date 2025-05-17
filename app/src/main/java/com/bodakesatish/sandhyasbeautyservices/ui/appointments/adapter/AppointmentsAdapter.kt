package com.bodakesatish.sandhyasbeautyservices.ui.appointments.adapter

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
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentCustomer
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.model.PaymentStatus
import com.bodakesatish.sandhyasbeautyservices.util.DateHelper
import java.text.NumberFormat
import java.util.Locale

class AppointmentsAdapter(
    private val onItemClicked: (AppointmentCustomer) -> Unit
) : ListAdapter<AppointmentCustomer, AppointmentsAdapter.AppointmentViewHolder>(AppointmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding =
            ListRowAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding, onItemClicked, parent.context)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val currentItem = getItem(position) // getItem() is provided by ListAdapter
        holder.bind(currentItem, position)
    }

    // No need for setData() anymore, ListAdapter handles it with submitList()
    // No need for a local itemList anymore

    // ViewHolder is now an inner class for better encapsulation if it's only used here.
    // Or can be a static nested class if preferred.
    class AppointmentViewHolder(
        private val binding: ListRowAppointmentBinding,
        private val onItemClicked: (AppointmentCustomer) -> Unit,
        private val context: Context // Needed for ContextCompat.getColor/getDrawable
    ) : RecyclerView.ViewHolder(binding.root) {

        // Optional: Define a time formatter if you have LocalTime or similar for appointment.appointmentTime
        // Adjust the pattern to your needs. If appointmentTime is already a formatted String, you don't need this here.
        // private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

        fun bind(data: AppointmentCustomer, itemNumber: Int) {
            // --- Basic Info ---
            binding.tvNumber.text = "${itemNumber+1}."
            binding.tvCustomerName.text = "${data.customer.firstName} ${data.customer.lastName}"

            // --- Date and Time ---
            val formattedDate = DateHelper.getFormattedDate(data.appointment.appointmentDate, "dd MMM yyyy") // Or your preferred format
            // Assuming data.appointment.appointmentTime is a pre-formatted String like "10:30 AM"
            // If it's a LocalTime object, you would format it here:
            // val formattedTime = data.appointment.appointmentTime?.format(timeFormatter) ?: ""
            val formattedTime = DateHelper.formatTime(data.appointment.appointmentTime) // Use as is if already a formatted string

            binding.tvAppointmentDatetime.text = if (formattedTime.toString().isNotBlank()) {
                "$formattedDate, $formattedTime"
            } else {
                formattedDate
            }
            binding.ivDatetimeIcon.setImageResource(R.drawable.ic_calendar_month) // Ensure you have this drawable

            // --- Services Summary ---
            if (!data.appointment.servicesSummary.isNullOrBlank()) {
                binding.tvAppointmentServicesSummary.text = data.appointment.servicesSummary
                binding.tvAppointmentServicesSummary.visibility = View.VISIBLE
            } else {
                binding.tvAppointmentServicesSummary.visibility = View.GONE
            }

            // --- Appointment Status ---
            data.appointment.status?.let { status ->
                binding.tvAppointmentStatus.text = status.name // Or a more user-friendly string from string resources
                val statusTextColorRes: Int
                val statusIndicatorColorRes: Int
                val statusCardStrokeColorRes: Int
                // val statusCardBackgroundColorRes: Int // If you decide to use card background color

                when (status) {
                    AppointmentStatus.SCHEDULED -> {
                        statusTextColorRes = R.color.colorScheduledText // Or use R.color.colorScheduled if text and border are same
                        statusIndicatorColorRes = R.color.colorScheduled
                        statusCardStrokeColorRes = R.color.colorScheduled
                    }
                    AppointmentStatus.COMPLETED -> {
                        statusTextColorRes = R.color.colorCompletedText
                        statusIndicatorColorRes = R.color.colorCompleted
                        statusCardStrokeColorRes = R.color.colorCompleted
                    }
                    AppointmentStatus.CANCELLED -> {
                        statusTextColorRes = R.color.colorCancelledText
                        statusIndicatorColorRes = R.color.colorCancelled
                        statusCardStrokeColorRes = R.color.colorCancelled
                    }
                    // Add other AppointmentStatus cases if any (e.g., IN_PROGRESS, NO_SHOW)
                    else -> { // Default for any unknown or new status
                        statusTextColorRes = R.color.textColorSecondary // A neutral default
                        statusIndicatorColorRes = android.R.color.transparent // No specific indicator color
                        statusCardStrokeColorRes = R.color.textColorSecondary
                    }
                }
                binding.tvAppointmentStatus.setTextColor(ContextCompat.getColor(context, statusTextColorRes))
                binding.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(context, statusIndicatorColorRes))
                binding.tvAppointmentStatusCard.strokeColor = ContextCompat.getColor(context, statusCardStrokeColorRes)
                // If using card background:
                // binding.tvAppointmentStatusCard.setCardBackgroundColor(ContextCompat.getColor(context, statusCardBackgroundColorRes))

            } ?: run {
                // Handle case where status is null (though ideally it shouldn't be)
                binding.tvAppointmentStatus.text = context.getString(R.string.status_not_available) // Define R.string.status_not_available as "N/A"
                binding.tvAppointmentStatus.setTextColor(ContextCompat.getColor(context, R.color.textColorSecondary))
                binding.viewStatusIndicator.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                binding.tvAppointmentStatusCard.strokeColor = ContextCompat.getColor(context, R.color.textColorSecondary)
            }

            // --- Total Amount ---
            data.appointment.totalBillAmount?.let { amount ->
                val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault()) // Or your app's specific locale
                binding.tvTotalAmount.text = format.format(amount)
                binding.tvTotalAmount.visibility = View.VISIBLE
            } ?: run {
                binding.tvTotalAmount.visibility = View.GONE // Or View.INVISIBLE to maintain layout space
            }

            // --- Payment Status Icon ---
            data.appointment.paymentStatus?.let { paymentStatus ->
                val paymentIconRes: Int?
                val paymentIconTintRes: Int?

                when (paymentStatus) {
                    PaymentStatus.PAID -> {
                        paymentIconRes = R.drawable.ic_payment_paid // e.g., a checkmark icon
                        paymentIconTintRes = R.color.colorPaid
                    }
                    PaymentStatus.UNPAID -> {
                        paymentIconRes = R.drawable.ic_payment_unpaid // e.g., an hourglass or exclamation icon
                        paymentIconTintRes = R.color.colorUnpaid
                    }
                    PaymentStatus.REFUNDED -> {
                        paymentIconRes = R.drawable.ic_payment_unpaid // e.g., a refresh or back arrow icon
                        paymentIconTintRes = R.color.colorRefunded
                    }
                    // Add other PaymentStatus cases if any
                    else -> {
                        paymentIconRes = null
                        paymentIconTintRes = null
                    }
                }

                if (paymentIconRes != null && paymentIconTintRes != null) {
                    binding.ivPaymentStatusIcon.setImageResource(paymentIconRes)
                    binding.ivPaymentStatusIcon.setColorFilter(ContextCompat.getColor(context, paymentIconTintRes))
                    binding.ivPaymentStatusIcon.visibility = View.VISIBLE
                } else {
                    binding.ivPaymentStatusIcon.visibility = View.GONE
                }
            } ?: run {
                binding.ivPaymentStatusIcon.visibility = View.GONE
            }

            // --- Click Listener ---
            binding.root.setOnClickListener {
                onItemClicked(data)
            }
        }
    }
    // DiffUtil.Callback remains largely the same, but now it's a static nested class
    // or a top-level class as it doesn't need access to adapter instance variables.
    class AppointmentDiffCallback : DiffUtil.ItemCallback<AppointmentCustomer>() {
        override fun areItemsTheSame(oldItem: AppointmentCustomer, newItem: AppointmentCustomer): Boolean {
            // Unique ID for the item itself (appointment ID is a good candidate)
            return oldItem.appointment.id == newItem.appointment.id
        }

        override fun areContentsTheSame(oldItem: AppointmentCustomer, newItem: AppointmentCustomer): Boolean {
            // Check if the visual representation of the item has changed.
            // Relies on AppointmentCustomer, Appointment, and Customer being data classes
            // or having proper equals() implementations.
            return oldItem == newItem
        }
    }
}