package com.bodakesatish.sandhyasbeautyservices.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

// Define Enums for status and payment status for type safety and clarity
enum class AppointmentDataStatus {
    PENDING,
    COMPLETED,
    UNKNOWN
}
enum class PaymentModeDataStatus {
    UPI,
    CASH,
    //    CARD,
//    UPI, // Unified Payments Interface - common in India
//    NOT_APPLICABLE, // e.g., for free consultations
    UNKNOWN,
    PENDING
}
enum class PaymentDataStatus {
    PAID,
    UNPAID,
    PARTIALLY_PAID,
    UNKNOWN
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
    @ColumnInfo(Columns.TOTAL_DISCOUNT)
    val totalDiscount: Double,
    @ColumnInfo(Columns.NET_TOTAL)
    val netTotal: Double,
    @ColumnInfo(Columns.PAYMENT_MODE)
    val paymentMode: PaymentModeDataStatus = PaymentModeDataStatus.PENDING,
    @ColumnInfo(Columns.APPOINTMENT_NOTES)
    val appointmentNotes: String = "",
    @ColumnInfo(Columns.APPOINTMENT_STATUS)
    val appointmentStatus: AppointmentDataStatus = AppointmentDataStatus.PENDING,
    @ColumnInfo(Columns.PAYMENT_STATUS)
    val paymentStatus: PaymentDataStatus = PaymentDataStatus.UNPAID,
    @ColumnInfo(Columns.PAYMENT_NOTES)
    val paymentNotes: String = ""
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
        internal const val TOTAL_DISCOUNT = "total_discount"
        internal const val NET_TOTAL = "net_total"
        internal const val APPOINTMENT_NOTES = "appointment_notes"
        internal const val APPOINTMENT_STATUS = "appointment_status"
        internal const val PAYMENT_MODE = "payment_mode"
        internal const val PAYMENT_STATUS = "payment_status"
        internal const val PAYMENT_NOTES = "payment_notes"
    }

}