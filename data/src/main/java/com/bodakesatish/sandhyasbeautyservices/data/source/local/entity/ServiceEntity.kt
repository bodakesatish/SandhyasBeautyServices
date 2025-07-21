package com.bodakesatish.sandhyasbeautyservices.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = ServiceEntity.TABLE_NAME)
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(Columns.ID)
    val id: Int = 0,
    @ColumnInfo(Columns.CATEGORY_ID)
    val categoryId: String,
    @ColumnInfo(Columns.SERVICE_NAME)
    val serviceName: String,
    @ColumnInfo(Columns.NORMAL_PRICE)
    val servicePrice: Double
) {

    companion object {
        const val TABLE_NAME = "services"
    }

    internal object Columns {
        const val ID = "id"
        internal const val CATEGORY_ID = "categoryId"
        internal const val SERVICE_NAME = "serviceName"
        internal const val NORMAL_PRICE = "normalPrice"
    }
}