package com.bodakesatish.sandhyasbeautyservices.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(serviceDetail: ServiceDetailEntity): Long

    @Update
    suspend fun update(serviceDetail: ServiceDetailEntity): Int

    @Query("DELETE FROM ${ServiceDetailEntity.TABLE_NAME} WHERE ${ServiceDetailEntity.Columns.ID} = :serviceDetailId")
    fun delete(serviceDetailId: Int)

    @Query("SELECT * FROM ${ServiceDetailEntity.TABLE_NAME}")
    fun getServiceDetailList(): Flow<List<ServiceDetailEntity>>

    @Query("SELECT * FROM ${ServiceDetailEntity.TABLE_NAME} WHERE ${ServiceDetailEntity.Columns.APPOINTMENT_ID} = :appointmentId")
    suspend fun getServiceDetailsByAppointmentId(appointmentId: Long): ServiceDetailEntity?

}