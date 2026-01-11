package com.example.gudangumkm.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Transaksi model for Firebase Firestore
 * Collection: "users/{userId}/transaksi"
 */
data class Transaksi(
    @DocumentId
    val id: String = "",
    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",
    @get:PropertyName("barang_id")
    @set:PropertyName("barang_id")
    var barangId: String = "",
    val tipe: String = "", // "MASUK" atau "KELUAR"
    val jumlah: Int = 0,
    val tanggal: String = "",
    val keterangan: String = "",
    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor required for Firestore deserialization
    constructor() : this("", "", "", "", 0, "", "", System.currentTimeMillis())
}
