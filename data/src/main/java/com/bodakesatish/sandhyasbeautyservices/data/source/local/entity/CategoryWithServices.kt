package com.bodakesatish.sandhyasbeautyservices.data.source.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithServices(
    @Embedded val category: CategoryEntity,
    @Relation(
        parentColumn = "firestoreDocId",
        entityColumn = "categoryId"
    )
    val services: List<ServiceEntity>
)