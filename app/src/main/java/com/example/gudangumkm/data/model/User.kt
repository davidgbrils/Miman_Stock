package com.example.gudangumkm.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * User model for Firebase Firestore
 * Collection: "users"
 */
data class User(
    @DocumentId
    val id: String = "",
    val nama: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val phone: String = "",
    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor required for Firestore deserialization
    constructor() : this("", "", "", "", "", "", System.currentTimeMillis())
}
