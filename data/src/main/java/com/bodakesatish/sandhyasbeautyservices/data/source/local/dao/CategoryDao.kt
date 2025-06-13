package com.bodakesatish.sandhyasbeautyservices.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CategoryEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CategoryWithServices
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(categoryEntity: CategoryEntity): Long

    @Update
    suspend fun update(categoryEntity: CategoryEntity): Int

    @Query("DELETE FROM ${CategoryEntity.TABLE_NAME} WHERE ${CategoryEntity.Columns.ID} = :categoryId")
    fun delete(categoryId: Int)

    @Query("SELECT * FROM ${CategoryEntity.TABLE_NAME}")
    fun getCategoryList(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM ${CategoryEntity.TABLE_NAME} WHERE ${CategoryEntity.Columns.ID} = :categoryId")
    suspend fun getCategoryById(categoryId: Long): CategoryEntity?

    @Transaction
    @Query("SELECT * FROM ${CategoryEntity.TABLE_NAME}")
    fun getCategoriesWithServices(): Flow<List<CategoryWithServices>>

}