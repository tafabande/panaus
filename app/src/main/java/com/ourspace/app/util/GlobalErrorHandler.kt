package com.ourspace.app.util


import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object GlobalErrorHandler {
    private val _errorEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errorEvents = _errorEvents.asSharedFlow()

    fun recordException(throwable: Throwable, message: String? = null) {
        throwable.printStackTrace()
        // Record non-crashing errors to Crashlytics
                // Record non-crashing errors internally if needed
                // FirebaseCrashlytics.getInstance().recordException(throwable)
        val errorMessage = message ?: throwable.message ?: "An unknown error occurred"
        _errorEvents.tryEmit(errorMessage)
    }

    fun showMessage(message: String) {
        _errorEvents.tryEmit(message)
    }

    /**
     * Helper to safely run suspend blocks and report errors.
     */
    suspend fun <T> runWithCatch(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            recordException(e)
            Result.failure(e)
        }
    }
}
