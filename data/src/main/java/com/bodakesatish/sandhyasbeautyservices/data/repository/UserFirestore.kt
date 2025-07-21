package com.bodakesatish.sandhyasbeautyservices.data.repository

import kotlin.to

data class UserFirestore(
    val uid: String = "", // Matches Firebase Auth UID
    val fullName: String = "",
    val email: String = "",
    val isAdmin: Boolean = false, // Add other fields you want to store in Firestore, e.g., profileImageUrl, registrationDate
    val registrationTimestamp: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firestore deserialization
    constructor() : this("", "", "",false, System.currentTimeMillis())

    fun toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "uid" to uid,
            "fullName" to fullName,
            "email" to email,
            "isAdmin" to isAdmin,
            "registrationTimestamp" to registrationTimestamp
        )
    }
}