package com.example.gudangumkm.data.repository

import com.example.gudangumkm.data.model.Transaksi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Repository for Transaksi operations with Firebase Firestore
 */
class TransaksiRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private fun getTransaksiCollection(userId: String) = 
        firestore.collection("users").document(userId).collection("transaksi")

    /**
     * Insert new transaksi
     */
    suspend fun insert(userId: String, transaksi: Transaksi): Result<Transaksi> {
        return try {
            val collection = getTransaksiCollection(userId)
            val docRef = collection.document()
            val newTransaksi = transaksi.copy(id = docRef.id, userId = userId)
            docRef.set(newTransaksi).await()
            Result.Success(newTransaksi)
        } catch (e: Exception) {
            Result.Error("Gagal menyimpan transaksi: ${e.message}", e)
        }
    }

    /**
     * Get all transaksi for a user
     */
    suspend fun getAll(userId: String): Result<List<Transaksi>> {
        return try {
            val querySnapshot = getTransaksiCollection(userId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .await()
            val transaksiList = querySnapshot.toObjects(Transaksi::class.java)
            Result.Success(transaksiList)
        } catch (e: Exception) {
            Result.Error("Gagal memuat transaksi: ${e.message}", e)
        }
    }

    /**
     * Get transaksi by barang ID
     */
    suspend fun getByBarangId(userId: String, barangId: String): Result<List<Transaksi>> {
        return try {
            val querySnapshot = getTransaksiCollection(userId)
                .whereEqualTo("barang_id", barangId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .await()
            val transaksiList = querySnapshot.toObjects(Transaksi::class.java)
            Result.Success(transaksiList)
        } catch (e: Exception) {
            Result.Error("Gagal memuat transaksi: ${e.message}", e)
        }
    }

    /**
     * Get transaksi by type (MASUK/KELUAR)
     */
    suspend fun getByTipe(userId: String, tipe: String): Result<List<Transaksi>> {
        return try {
            val querySnapshot = getTransaksiCollection(userId)
                .whereEqualTo("tipe", tipe)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .await()
            val transaksiList = querySnapshot.toObjects(Transaksi::class.java)
            Result.Success(transaksiList)
        } catch (e: Exception) {
            Result.Error("Gagal memuat transaksi: ${e.message}", e)
        }
    }

    /**
     * Get transaksi by date range
     */
    suspend fun getByDateRange(userId: String, startDate: String, endDate: String): Result<List<Transaksi>> {
        return try {
            val querySnapshot = getTransaksiCollection(userId)
                .whereGreaterThanOrEqualTo("tanggal", startDate)
                .whereLessThanOrEqualTo("tanggal", endDate)
                .orderBy("tanggal", Query.Direction.DESCENDING)
                .get()
                .await()
            val transaksiList = querySnapshot.toObjects(Transaksi::class.java)
            Result.Success(transaksiList)
        } catch (e: Exception) {
            Result.Error("Gagal memuat transaksi: ${e.message}", e)
        }
    }

    /**
     * Get count of MASUK transactions
     */
    suspend fun getCountMasuk(userId: String): Result<Int> {
        return try {
            val querySnapshot = getTransaksiCollection(userId)
                .whereEqualTo("tipe", "MASUK")
                .get()
                .await()
            Result.Success(querySnapshot.size())
        } catch (e: Exception) {
            Result.Error("Gagal menghitung transaksi masuk: ${e.message}", e)
        }
    }

    /**
     * Get count of KELUAR transactions
     */
    suspend fun getCountKeluar(userId: String): Result<Int> {
        return try {
            val querySnapshot = getTransaksiCollection(userId)
                .whereEqualTo("tipe", "KELUAR")
                .get()
                .await()
            Result.Success(querySnapshot.size())
        } catch (e: Exception) {
            Result.Error("Gagal menghitung transaksi keluar: ${e.message}", e)
        }
    }

    /**
     * Update transaksi
     */
    suspend fun update(userId: String, transaksi: Transaksi): Result<Transaksi> {
        return try {
            getTransaksiCollection(userId).document(transaksi.id).set(transaksi).await()
            Result.Success(transaksi)
        } catch (e: Exception) {
            Result.Error("Gagal update transaksi: ${e.message}", e)
        }
    }

    /**
     * Delete transaksi
     */
    suspend fun delete(userId: String, transaksiId: String): Result<Boolean> {
        return try {
            getTransaksiCollection(userId).document(transaksiId).delete().await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error("Gagal hapus transaksi: ${e.message}", e)
        }
    }
}
