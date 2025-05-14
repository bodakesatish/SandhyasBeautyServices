package com.bodakesatish.sandhyasbeautyservices.data.source.local.entity

import androidx.room.Embedded
import androidx.room.Relation

// Define this data class in your data layer (e.g., within a 'models' or 'entities' package)
//data class AppointmentCustomer(
//    @Embedded val customer: CustomerEntity,
//    @Relation(
//        parentColumn = "id",
//        entityColumn = "customerId"
//    )
//    val appointments: List<AppointmentsEntity>
//)

// Define this data class in your data layer (e.g., within a 'models' or 'entities' package)
data class AppointmentCustomer(
    @Embedded val appointment: AppointmentsEntity, // Appointment is the 'parent' in terms of holding the foreign key
    @Relation(
        parentColumn = AppointmentsEntity.Columns.CUSTOMER_ID, // Column in the parent (Appointment)
        entityColumn = CustomerEntity.Columns.ID // Column in the child (Customer)
    )
    val customer: CustomerEntity // Customer is the 'child'
)