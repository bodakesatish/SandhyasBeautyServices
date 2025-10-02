package com.bodakesatish.sandhyasbeautyservices.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = CategoryEntity.TABLE_NAME)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(Columns.ID)
    val id: Int = 0,
    @ColumnInfo(Columns.CATEGORY_NAME)
    val categoryName: String,
    @ColumnInfo(Columns.CATEGORY_DESCRIPTION)
    val categoryDescription: String,

    ) {

    companion object {
        const val TABLE_NAME = "category"
    }

    internal object Columns {
        const val ID = "id"
        internal const val CATEGORY_NAME = "categoryName"
        internal const val CATEGORY_DESCRIPTION = "categoryDescription"
    }
}