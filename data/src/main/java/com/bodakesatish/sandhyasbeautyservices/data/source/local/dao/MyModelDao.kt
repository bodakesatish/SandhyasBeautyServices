package com.bodakesatish.sandhyasbeautyservices.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.MyModelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MyModelDao {
    @Query("SELECT * FROM mymodelentity ORDER BY uid DESC LIMIT 10")
    fun getMyModels(): Flow<List<MyModelEntity>>

    @Insert
    suspend fun insertMyModel(item: MyModelEntity)

    @Query("DELETE FROM mymodelentity where name = :name")
    suspend fun deleteMyModel(name: String)
}