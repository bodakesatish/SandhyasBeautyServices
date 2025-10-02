package com.bodakesatish.sandhyasbeautyservices.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = ServiceDetailEntity.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE // Optional: Define what happens on customer deletion
        ),
        ForeignKey(
            entity = AppointmentsEntity::class,
            parentColumns = ["id"],
            childColumns = ["appointmentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ServiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.RESTRICT // Optional: Prevent deletion if a service detail exists
        )
    ]
)
data class ServiceDetailEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(Columns.ID)
    val id: Int = 0,
    @ColumnInfo(Columns.CUSTOMER_ID)
    val customerId: Int,
    @ColumnInfo(Columns.APPOINTMENT_ID)
    val appointmentId: Int,
    @ColumnInfo(Columns.SERVICE_ID)
    val serviceId: Int,
    @ColumnInfo(Columns.AMOUNT)
    val originalAmount: Double,
    @ColumnInfo(Columns.DISCOUNT)
    val discount: Double,
    @ColumnInfo(Columns.DISCOUNT_PERCENTAGE)
    val discountPercentage: Double,
    @ColumnInfo(Columns.PRICE_AFTER_DISCOUNT)
    val priceAfterDiscount: Double,
    @ColumnInfo(Columns.SERVICE_SUMMARY)
    val serviceSummary: String
) {

    companion object {
        const val TABLE_NAME = "service_detail"
    }

    internal object Columns {
        const val ID = "id"
        internal const val CUSTOMER_ID = "customerId"
        internal const val APPOINTMENT_ID = "appointmentId"
        internal const val SERVICE_ID = "serviceId"
        internal const val AMOUNT = "amount"
        internal const val DISCOUNT = "discount"
        internal const val DISCOUNT_PERCENTAGE = "percentage"
        internal const val PRICE_AFTER_DISCOUNT = "priceAfterDiscount"
        internal const val SERVICE_SUMMARY = "serviceSummary"
    }

}