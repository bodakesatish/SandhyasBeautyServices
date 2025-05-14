package com.bodakesatish.sandhyasbeautyservices.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = AppointmentsEntity.TABLE_NAME)
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
    @ColumnInfo(Columns.APPOINTMENT_PLANNED_TIME)
    val appointmentPlannedTime: Date,
    @ColumnInfo(Columns.APPOINTMENT_COMPLETED_TIME)
    val appointmentCompletedTime: Date,
    @ColumnInfo(Columns.TOTAL_BILL_AMOUNT)
    val totalBillAmount: Double,
    @ColumnInfo(Columns.APPOINTMENT_STATUS)
    val appointmentStatus: String,
    @ColumnInfo(Columns.PAYMENT_MODE)
    val paymentMode: String,
) {

    companion object {
        const val TABLE_NAME = "appointment"
    }

    internal object Columns {
        const val ID = "id"
        internal const val CUSTOMER_ID = "customerId"
        internal const val APPOINTMENT_DATE = "appointment_date"
        internal const val APPOINTMENT_TIME = "appointment_time"
        internal const val APPOINTMENT_PLANNED_TIME = "appointment_planned_time"
        internal const val APPOINTMENT_COMPLETED_TIME = "appointment_completed_time"
        internal const val TOTAL_BILL_AMOUNT = "total_bill_amount"
        internal const val APPOINTMENT_STATUS = "appointment_status"
        internal const val PAYMENT_MODE = "payment_mode"

    }
}