package com.bodakesatish.sandhyasbeautyservices.data.repository

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun getUserById(uid: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserByIdOnce(uid: String): UserEntity?

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun deleteUser(uid: String)

    @Query("DELETE FROM users")
    suspend fun clearAllUsers() // For logout
}