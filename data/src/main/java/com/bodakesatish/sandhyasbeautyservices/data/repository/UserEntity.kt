package com.bodakesatish.sandhyasbeautyservices.data.repository

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String, // Firebase Auth UID
    val fullName: String,
    val email: String,
    val registrationTimestamp: Long,
    val lastLoginTimestamp: Long? = null, // Example: for session time tracking
    // Add other local-specific fields if needed
)