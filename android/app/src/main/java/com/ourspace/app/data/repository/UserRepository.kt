package com.ourspace.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.util.DateUtils
import kotlinx.coroutines.channels.awaitClose

class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    fun observeUser(userId: String): Flow<UserProfile?> = callbackFlow {
        val listener = db.collection("users").document(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(UserProfile::class.java)
                trySend(user)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun pairWithPartner(currentUserId: String, partnerCode: String): Result<Unit> {
        return try {
            if (currentUserId == partnerCode) {
                return Result.failure(Exception("You can't pair with yourself."))
            }

            val partnerRef = db.collection("users").document(partnerCode)
            val partnerSnap = partnerRef.get().await()

            if (!partnerSnap.exists()) {
                return Result.failure(Exception("Invalid invite code. Partner not found."))
            }

            val partnerData = partnerSnap.toObject(UserProfile::class.java)
            if (partnerData?.partnerId != null) {
                return Result.failure(Exception("This user is already paired with someone."))
            }

            val coupleId = listOf(currentUserId, partnerCode).sorted().joinToString("_")

            val coupleData = hashMapOf(
                "coupleId" to coupleId,
                "user1Id" to currentUserId,
                "user2Id" to partnerCode,
                "createdAt" to DateUtils.getCurrentIsoTime()
            )

            // Execute all writes
            db.collection("couples").document(coupleId).set(coupleData).await()
            
            db.collection("users").document(currentUserId).update(
                "partnerId", partnerCode,
                "coupleId", coupleId
            ).await()

            partnerRef.update(
                "partnerId", currentUserId,
                "coupleId", coupleId
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
