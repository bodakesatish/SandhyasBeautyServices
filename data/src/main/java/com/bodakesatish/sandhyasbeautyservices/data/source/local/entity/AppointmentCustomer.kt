package com.bodakesatish.sandhyasbeautyservices.data.source.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class AppointmentCustomer(
    @Embedded val customer: CustomerEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val appointments: List<AppointmentsEntity>
)