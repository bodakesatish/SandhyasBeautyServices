package com.bodakesatish.sandhyasbeautyservices.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServicesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateService(serviceEntity: ServiceEntity): Long

    @Query("DELETE FROM ${ServiceEntity.TABLE_NAME} WHERE ${ServiceEntity.Columns.ID} = :serviceId")
    fun deleteService(serviceId: Int)

    @Query("SELECT * FROM ${ServiceEntity.TABLE_NAME}")
    fun getServiceList(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM ${ServiceEntity.TABLE_NAME} WHERE ${ServiceEntity.Columns.ID} = :serviceId")
    suspend fun getServiceById(serviceId: Int): ServiceEntity?

}