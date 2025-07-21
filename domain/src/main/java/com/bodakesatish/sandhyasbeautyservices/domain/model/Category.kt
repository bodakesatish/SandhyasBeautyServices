package com.bodakesatish.sandhyasbeautyservices.domain.model

import java.io.Serializable

data class Category(
    val firestoreDocId: String = "",
    var categoryName: String = "",
    var categoryDescription: String = "",
    val registrationTimestamp: Long = System.currentTimeMillis()
) : Serializable {
    fun toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "firestoreDocId" to firestoreDocId,
            "categoryName" to categoryName,
            "categoryDescription" to categoryDescription,
            "registrationTimestamp" to registrationTimestamp
        )
    }
}