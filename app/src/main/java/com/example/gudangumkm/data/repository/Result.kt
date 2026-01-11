package com.example.gudangumkm.data.repository

/**
 * Sealed class for handling async operation results
 */
sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
}
