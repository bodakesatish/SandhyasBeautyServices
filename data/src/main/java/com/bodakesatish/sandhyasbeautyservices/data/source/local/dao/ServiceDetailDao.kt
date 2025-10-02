package com.bodakesatish.sandhyasbeautyservices.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceDetailEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceDetailWithServiceData
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceEntity
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
    fun getServiceDetailsByAppointmentId(appointmentId: Long): Flow<List<ServiceDetailEntity>>

    @Query("""
        SELECT ${ServiceDetailEntity.Columns.SERVICE_ID}
        FROM ${ServiceDetailEntity.TABLE_NAME}
        WHERE appointmentId = :appointmentId
    """)
    fun getServiceIdsForAppointment(appointmentId: Long): Flow<List<Int>>

    // In your DAO
    @Query("SELECT sd.*, s.* FROM ${ServiceDetailEntity.TABLE_NAME} sd JOIN ${ServiceEntity.TABLE_NAME} s ON sd.serviceId = s.id WHERE sd.appointmentId = :appointmentId")
    fun getServiceDetailsWithServiceForAppointment(appointmentId: Int): Flow<List<ServiceDetailWithServiceData>>

    @Query("DELETE FROM ${ServiceDetailEntity.TABLE_NAME} WHERE ${ServiceDetailEntity.Columns.APPOINTMENT_ID} = :appointmentId")
    fun deleteServicesByAppointment(appointmentId: Long)

}