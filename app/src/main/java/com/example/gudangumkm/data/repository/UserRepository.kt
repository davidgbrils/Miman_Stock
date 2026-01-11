package com.example.gudangumkm.data.repository

import com.example.gudangumkm.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository for User operations with Firebase Firestore
 */
class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    /**
     * Register new user
     */
    suspend fun register(user: User): Result<User> {
        return try {
            // Check if username already exists
            val existingUser = usersCollection
                .whereEqualTo("username", user.username)
                .get()
                .await()

            if (!existingUser.isEmpty) {
                return Result.Error("Username sudah digunakan")
            }

            // Create new user document
            val docRef = usersCollection.document()
            val newUser = user.copy(id = docRef.id)
            docRef.set(newUser).await()

            Result.Success(newUser)
        } catch (e: Exception) {
            Result.Error("Gagal mendaftar: ${e.message}", e)
        }
    }

    /**
     * Login user with username and password
     */
    suspend fun login(username: String, password: String): Result<User> {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Result.Error("Username atau password salah")
            } else {
                val user = querySnapshot.documents[0].toObject(User::class.java)
                if (user != null) {
                    Result.Success(user)
                } else {
                    Result.Error("Gagal memuat data user")
                }
            }
        } catch (e: Exception) {
            Result.Error("Gagal login: ${e.message}", e)
        }
    }

    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.Success(user)
            } else {
                Result.Error("User tidak ditemukan")
            }
        } catch (e: Exception) {
            Result.Error("Gagal memuat user: ${e.message}", e)
        }
    }

    /**
     * Get user by username
     */
    suspend fun getByUsername(username: String): Result<User?> {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("username", username)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Result.Success(null)
            } else {
                val user = querySnapshot.documents[0].toObject(User::class.java)
                Result.Success(user)
            }
        } catch (e: Exception) {
            Result.Error("Gagal mencari user: ${e.message}", e)
        }
    }

    /**
     * Update user
     */
    suspend fun updateUser(user: User): Result<User> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error("Gagal update user: ${e.message}", e)
        }
    }

    /**
     * Delete user
     */
    suspend fun deleteUser(userId: String): Result<Boolean> {
        return try {
            usersCollection.document(userId).delete().await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error("Gagal hapus user: ${e.message}", e)
        }
    }

    /**
     * Get all users
     */
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val querySnapshot = usersCollection.get().await()
            val users = querySnapshot.toObjects(User::class.java)
            Result.Success(users)
        } catch (e: Exception) {
            Result.Error("Gagal memuat daftar user: ${e.message}", e)
        }
    }
}
