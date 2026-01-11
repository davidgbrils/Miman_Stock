package com.example.gudangumkm.data.repository

import com.example.gudangumkm.data.model.Barang
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Repository for Barang operations with Firebase Firestore
 */
class BarangRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private fun getBarangCollection(userId: String) = 
        firestore.collection("users").document(userId).collection("barang")

    /**
     * Insert new barang
     */
    suspend fun insert(userId: String, barang: Barang): Result<Barang> {
        return try {
            val collection = getBarangCollection(userId)
            val docRef = collection.document()
            val newBarang = barang.copy(id = docRef.id, userId = userId)
            docRef.set(newBarang).await()
            Result.Success(newBarang)
        } catch (e: Exception) {
            Result.Error("Gagal menyimpan barang: ${e.message}", e)
        }
    }

    /**
     * Get all barang for a user
     */
    suspend fun getAll(userId: String): Result<List<Barang>> {
        return try {
            val querySnapshot = getBarangCollection(userId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .await()
            val barangList = querySnapshot.toObjects(Barang::class.java)
            Result.Success(barangList)
        } catch (e: Exception) {
            Result.Error("Gagal memuat barang: ${e.message}", e)
        }
    }

    /**
     * Get barang by ID
     */
    suspend fun getById(userId: String, barangId: String): Result<Barang> {
        return try {
            val document = getBarangCollection(userId).document(barangId).get().await()
            val barang = document.toObject(Barang::class.java)
            if (barang != null) {
                Result.Success(barang)
            } else {
                Result.Error("Barang tidak ditemukan")
            }
        } catch (e: Exception) {
            Result.Error("Gagal memuat barang: ${e.message}", e)
        }
    }

    /**
     * Get count of barang
     */
    suspend fun getCount(userId: String): Result<Int> {
        return try {
            val querySnapshot = getBarangCollection(userId).get().await()
            Result.Success(querySnapshot.size())
        } catch (e: Exception) {
            Result.Error("Gagal menghitung barang: ${e.message}", e)
        }
    }

    /**
     * Get total stok
     */
    suspend fun getTotalStok(userId: String): Result<Int> {
        return try {
            val querySnapshot = getBarangCollection(userId).get().await()
            val barangList = querySnapshot.toObjects(Barang::class.java)
            val totalStok = barangList.sumOf { it.stok }
            Result.Success(totalStok)
        } catch (e: Exception) {
            Result.Error("Gagal menghitung stok: ${e.message}", e)
        }
    }

    /**
     * Get count of barang with low stock
     */
    suspend fun getCountStokMenipis(userId: String): Result<Int> {
        return try {
            val querySnapshot = getBarangCollection(userId).get().await()
            val barangList = querySnapshot.toObjects(Barang::class.java)
            val count = barangList.count { it.stok <= it.stokMinimum }
            Result.Success(count)
        } catch (e: Exception) {
            Result.Error("Gagal menghitung stok menipis: ${e.message}", e)
        }
    }

    /**
     * Get barang with low stock
     */
    suspend fun getStokMenipis(userId: String): Result<List<Barang>> {
        return try {
            val querySnapshot = getBarangCollection(userId).get().await()
            val barangList = querySnapshot.toObjects(Barang::class.java)
                .filter { it.stok <= it.stokMinimum }
                .sortedBy { it.stok }
            Result.Success(barangList)
        } catch (e: Exception) {
            Result.Error("Gagal memuat stok menipis: ${e.message}", e)
        }
    }

    /**
     * Get barang approaching expiry
     */
    suspend fun getMendekatExpired(userId: String, tanggal: String): Result<List<Barang>> {
        return try {
            val querySnapshot = getBarangCollection(userId).get().await()
            val barangList = querySnapshot.toObjects(Barang::class.java)
                .filter { it.tanggalExpired.isNotEmpty() && it.tanggalExpired <= tanggal }
                .sortedBy { it.tanggalExpired }
            Result.Success(barangList)
        } catch (e: Exception) {
            Result.Error("Gagal memuat barang expired: ${e.message}", e)
        }
    }

    /**
     * Search barang by name
     */
    suspend fun search(userId: String, query: String): Result<List<Barang>> {
        return try {
            val querySnapshot = getBarangCollection(userId).get().await()
            val barangList = querySnapshot.toObjects(Barang::class.java)
                .filter { it.nama.contains(query, ignoreCase = true) }
                .sortedBy { it.nama }
            Result.Success(barangList)
        } catch (e: Exception) {
            Result.Error("Gagal mencari barang: ${e.message}", e)
        }
    }

    /**
     * Get barang by kategori
     */
    suspend fun getByKategori(userId: String, kategori: String): Result<List<Barang>> {
        return try {
            val querySnapshot = getBarangCollection(userId)
                .whereEqualTo("kategori", kategori)
                .orderBy("nama")
                .get()
                .await()
            val barangList = querySnapshot.toObjects(Barang::class.java)
            Result.Success(barangList)
        } catch (e: Exception) {
            Result.Error("Gagal memuat kategori: ${e.message}", e)
        }
    }

    /**
     * Update barang
     */
    suspend fun update(userId: String, barang: Barang): Result<Barang> {
        return try {
            getBarangCollection(userId).document(barang.id).set(barang).await()
            Result.Success(barang)
        } catch (e: Exception) {
            Result.Error("Gagal update barang: ${e.message}", e)
        }
    }

    /**
     * Delete barang
     */
    suspend fun delete(userId: String, barangId: String): Result<Boolean> {
        return try {
            getBarangCollection(userId).document(barangId).delete().await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error("Gagal hapus barang: ${e.message}", e)
        }
    }

    /**
     * Add stock to barang
     */
    suspend fun tambahStok(userId: String, barangId: String, jumlah: Int): Result<Boolean> {
        return try {
            val docRef = getBarangCollection(userId).document(barangId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentStok = snapshot.getLong("stok")?.toInt() ?: 0
                transaction.update(docRef, "stok", currentStok + jumlah)
            }.await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error("Gagal menambah stok: ${e.message}", e)
        }
    }

    /**
     * Reduce stock from barang
     */
    suspend fun kurangiStok(userId: String, barangId: String, jumlah: Int): Result<Boolean> {
        return try {
            val docRef = getBarangCollection(userId).document(barangId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentStok = snapshot.getLong("stok")?.toInt() ?: 0
                if (currentStok < jumlah) {
                    throw Exception("Stok tidak mencukupi")
                }
                transaction.update(docRef, "stok", currentStok - jumlah)
            }.await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error("Gagal mengurangi stok: ${e.message}", e)
        }
    }
}
