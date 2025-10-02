package com.bodakesatish.sandhyasbeautyservices.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(customerEntity: CustomerEntity): Long

    @Update
    suspend fun update(customerEntity: CustomerEntity): Int

    @Query("DELETE FROM ${CustomerEntity.TABLE_NAME} WHERE ${CustomerEntity.Columns.ID} = :customerId")
    fun delete(customerId: Int)

    @Query("SELECT * FROM ${CustomerEntity.TABLE_NAME}")
    fun getCustomerList(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM ${CustomerEntity.TABLE_NAME} WHERE ${CustomerEntity.Columns.ID} = :customerId")
    suspend fun getCustomerById(customerId: Long): CustomerEntity?


}