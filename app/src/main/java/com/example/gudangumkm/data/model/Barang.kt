package com.example.gudangumkm.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Barang model for Firebase Firestore
 * Collection: "users/{userId}/barang"
 */
data class Barang(
    @DocumentId
    val id: String = "",
    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",
    val nama: String = "",
    val kategori: String = "",
    val stok: Int = 0,
    @get:PropertyName("stok_minimum")
    @set:PropertyName("stok_minimum")
    var stokMinimum: Int = 5,
    @get:PropertyName("tanggal_produksi")
    @set:PropertyName("tanggal_produksi")
    var tanggalProduksi: String = "",
    @get:PropertyName("tanggal_expired")
    @set:PropertyName("tanggal_expired")
    var tanggalExpired: String = "",
    val deskripsi: String = "",
    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor required for Firestore deserialization
    constructor() : this("", "", "", "", 0, 5, "", "", "", System.currentTimeMillis())
}
