package com.bodakesatish.sandhyasbeautyservices.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CategoryEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CategoryWithServices
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(categoryEntity: CategoryEntity): Long

    @Update
    suspend fun update(categoryEntity: CategoryEntity): Int

    @Query("DELETE FROM ${CategoryEntity.TABLE_NAME} WHERE ${CategoryEntity.Columns.FIRESTORE_DOC_ID} = :categoryId")
    fun delete(categoryId: String)

    @Query("SELECT * FROM ${CategoryEntity.TABLE_NAME}")
    fun getCategoryList(): List<CategoryEntity>

    @Query("SELECT * FROM ${CategoryEntity.TABLE_NAME}")
    fun observeCategoryList(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM ${CategoryEntity.TABLE_NAME}")
    suspend fun getCategoryListCount(): Int

    @Query("SELECT * FROM ${CategoryEntity.TABLE_NAME} WHERE ${CategoryEntity.Columns.FIRESTORE_DOC_ID} = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?

    @Transaction
    @Query("SELECT * FROM ${CategoryEntity.TABLE_NAME}")
    fun getCategoriesWithServices(): Flow<List<CategoryWithServices>>

    @Upsert
    suspend fun upsertAll(categories: MutableList<CategoryEntity>)

    @Upsert
    suspend fun upsertAllC(categories: List<CategoryEntity>)

    @Upsert
    fun upsert(categoryEntity: CategoryEntity)

    @Query("DELETE FROM ${CategoryEntity.TABLE_NAME}")
    suspend fun clearAll()

}