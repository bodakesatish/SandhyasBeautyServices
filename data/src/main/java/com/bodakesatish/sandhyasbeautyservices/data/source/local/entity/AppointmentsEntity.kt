package com.bodakesatish.sandhyasbeautyservices.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

// Define Enums for status and payment status for type safety and clarity
enum class AppointmentDataStatus {
    PENDING, CONFIRMED, COMPLETED, CANCELLED
}

enum class PaymentDataStatus {
    PAID, UNPAID, PARTIALLY_PAID
}

@Entity(
    tableName = AppointmentsEntity.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE // Optional: Define what happens on customer deletion
        )]
)
data class AppointmentsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(Columns.ID)
    val id: Int = 0,
    @ColumnInfo(Columns.CUSTOMER_ID)
    val customerId: Int,
    @ColumnInfo(Columns.APPOINTMENT_DATE)
    val appointmentDate: Date,
    @ColumnInfo(Columns.APPOINTMENT_TIME)
    val appointmentTime: Date,
    @ColumnInfo(Columns.TOTAL_BILL_AMOUNT)
    val totalBillAmount: Double,
    @ColumnInfo(Columns.PAYMENT_MODE)
    val paymentMode: String,
    @ColumnInfo(Columns.SERVICE_SUMMARY)
    val servicesSummary: String = "",
    @ColumnInfo(Columns.APPOINTMENT_STATUS)
    val status: AppointmentDataStatus = AppointmentDataStatus.PENDING,
    @ColumnInfo(Columns.PAYMENT_STATUS)
    val paymentStatus: PaymentDataStatus = PaymentDataStatus.UNPAID,
    @ColumnInfo(Columns.NOTES)
    val notes: String = ""
) {

    companion object {
        const val TABLE_NAME = "appointment"
    }

    internal object Columns {
        const val ID = "id"
        internal const val CUSTOMER_ID = "customerId"
        internal const val APPOINTMENT_DATE = "appointment_date"
        internal const val APPOINTMENT_TIME = "appointment_time"
        internal const val TOTAL_BILL_AMOUNT = "total_bill_amount"
        internal const val SERVICE_SUMMARY = "service_summary"
        internal const val APPOINTMENT_STATUS = "appointment_status"
        internal const val PAYMENT_MODE = "payment_mode"
        internal const val PAYMENT_STATUS = "payment_status"
        internal const val NOTES = "notes"
    }
}