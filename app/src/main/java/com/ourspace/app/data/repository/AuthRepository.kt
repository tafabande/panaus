package com.ourspace.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.util.DateUtils
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import android.util.Log

private const val TAG = "AuthRepository"

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser get() = auth.currentUser

    suspend fun login(email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Attempting login for: $email")
            auth.signInWithEmailAndPassword(email, pass).await()
            Log.d(TAG, "Login successful for: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed for: $email", e)
            Result.failure(e)
        }
    }

    suspend fun getEmailByUsername(username: String): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = db.collection("users")
                .whereEqualTo("username", username.lowercase().trim())
                .get()
                .await()
            snapshot.documents.firstOrNull()?.getString("email")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get email by username: $username", e)
            null
        }
    }

    suspend fun isUsernameUnique(username: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = db.collection("users")
                .whereEqualTo("username", username.lowercase().trim())
                .get()
                .await()
            snapshot.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check username uniqueness: $username", e)
            false
        }
    }

    suspend fun register(name: String, username: String, email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Attempting registration for: $email (Username: $username)")
            
            // Check username uniqueness first
            if (!isUsernameUnique(username)) {
                return@withContext Result.failure(Exception("Username already exists"))
            }

            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val userId = result.user?.uid ?: throw Exception("User ID is null")
            Log.d(TAG, "Auth user created: $userId. Generating partner code...")
            
            val partnerCode = generateUniquePartnerCode()
            Log.d(TAG, "Partner code generated: $partnerCode")

            val userProfile = UserProfile(
                userId = userId,
                name = name,
                username = username.lowercase().trim(),
                email = email,
                partnerId = null,
                coupleId = null,
                createdAt = DateUtils.getCurrentIsoTime(),
                partnerCode = partnerCode
            )

            Log.d(TAG, "Saving user profile to Firestore for UID: $userId...")
            db.collection("users").document(userId).set(userProfile).await()
            Log.d(TAG, "User profile saved successfully to Firestore.")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed for: $email", e)
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
