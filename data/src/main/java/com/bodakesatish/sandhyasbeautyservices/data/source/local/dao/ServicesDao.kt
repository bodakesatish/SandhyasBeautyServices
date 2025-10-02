package com.bodakesatish.sandhyasbeautyservices.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServicesDao {

    @Upsert
    fun insertOrUpdateService(serviceEntity: ServiceEntity): Long

    @Query("DELETE FROM ${ServiceEntity.TABLE_NAME} WHERE ${ServiceEntity.Columns.ID} = :serviceId")
    fun deleteService(serviceId: Int)

    @Query("SELECT * FROM ${ServiceEntity.TABLE_NAME}")
    fun getServiceList(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM ${ServiceEntity.TABLE_NAME} WHERE ${ServiceEntity.Columns.CATEGORY_ID} = :categoryId")
    fun getServiceListByCategoryId(categoryId: Int): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM ${ServiceEntity.TABLE_NAME} WHERE ${ServiceEntity.Columns.ID} = :serviceId")
    suspend fun getServiceById(serviceId: Int): ServiceEntity?

}