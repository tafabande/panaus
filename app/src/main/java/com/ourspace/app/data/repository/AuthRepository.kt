package com.ourspace.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.util.DateUtils
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser get() = auth.currentUser

    suspend fun login(email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val userId = result.user?.uid ?: throw Exception("User ID is null")
            val partnerCode = generateUniquePartnerCode()

            val userProfile = UserProfile(
                userId = userId,
                name = name,
                email = email,
                partnerId = null,
                coupleId = null,
                createdAt = DateUtils.getCurrentIsoTime(),
                partnerCode = partnerCode
            )

            db.collection("users").document(userId).set(userProfile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun generateUniquePartnerCode(): String {
        var code: String
        var exists: Boolean
        do {
            code = (100000..999999).random().toString()
            // Check if this code already exists in the "users" collection
            val snapshot = db.collection("users")
                .whereEqualTo("partnerCode", code)
                .get()
                .await()
            exists = !snapshot.isEmpty
        } while (exists)
        return code
    }

    fun logout() {
        auth.signOut()
    }
}
