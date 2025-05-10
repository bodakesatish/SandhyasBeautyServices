package com.bodakesatish.sandhyasbeautyservices.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.AppointmentsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(appointment: AppointmentsEntity): Long

    @Update
    suspend fun update(appointment: AppointmentsEntity): Int

    @Query("DELETE FROM ${AppointmentsEntity.TABLE_NAME} WHERE ${AppointmentsEntity.Columns.ID} = :appointmentId")
    fun delete(appointmentId: Int)

    @Query("SELECT * FROM ${AppointmentsEntity.TABLE_NAME}")
    fun getAppointmentList(): Flow<List<AppointmentsEntity>>

    @Query("SELECT * FROM ${AppointmentsEntity.TABLE_NAME} WHERE ${AppointmentsEntity.Columns.ID} = :appointmentId")
    suspend fun getAppointmentById(appointmentId: Long): AppointmentsEntity?

}