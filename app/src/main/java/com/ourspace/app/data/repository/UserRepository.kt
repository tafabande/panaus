package com.ourspace.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.util.DateUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri

private const val TAG = "UserRepository"

class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    fun observeUser(userId: String): Flow<UserProfile?> = callbackFlow {
        Log.d(TAG, "Starting observeUser for: $userId")
        val listener = db.collection("users").document(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing user $userId", error)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(UserProfile::class.java)
                Log.d(TAG, "User profile updated for $userId: ${user?.name}")
                trySend(user)
            } else {
                Log.d(TAG, "User profile for $userId does not exist or snapshot is null")
                trySend(null)
            }
        }
        awaitClose { 
            Log.d(TAG, "Cleaning up observeUser listener for: $userId")
            listener.remove() 
        }
    }

    suspend fun pairWithPartner(currentUserId: String, partnerCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Search for partner by their 6-digit partnerCode
            val partnerQuery = db.collection("users")
                .whereEqualTo("partnerCode", partnerCode)
                .get()
                .await()

            if (partnerQuery.isEmpty) {
                return@withContext Result.failure(Exception("Invalid partner code. Partner not found."))
            }

            val partnerSnap = partnerQuery.documents.first()
            val partnerId = partnerSnap.id
            val partnerRef = partnerSnap.reference

            if (currentUserId == partnerId) {
                return@withContext Result.failure(Exception("You can't pair with yourself."))
            }

            val partnerData = partnerSnap.toObject(UserProfile::class.java)
            if (partnerData?.partnerId != null) {
                return@withContext Result.failure(Exception("This user is already paired with someone."))
            }

            val coupleId = listOf(currentUserId, partnerId).sorted().joinToString("_")

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
            if (e is com.google.firebase.firestore.FirebaseFirestoreException && e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                 return@withContext Result.failure(Exception("User not found, or they have disabled discoverability."))
            }
            Result.failure(e)
        }
    }

    suspend fun updateDiscoverability(userId: String, isDiscoverable: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            db.collection("users").document(userId).update("isDiscoverable", isDiscoverable).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(userId: String, name: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            db.collection("users").document(userId).update("name", name.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateExtendedProfile(userId: String, updates: Map<String, Any?>): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            db.collection("users").document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePicture(userId: String, localUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val storage = FirebaseStorage.getInstance()
            val fileName = "profiles/$userId/pfp_${System.currentTimeMillis()}.jpg"
            val fileRef = storage.reference.child(fileName)
            
            Log.d("UserRepository", "Uploading PFP to: $fileName from Uri: $localUri")
            
            // Upload
            fileRef.putFile(localUri).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            
            // Update Firestore
            db.collection("users").document(userId).update("avatarUrl", downloadUrl).await()
            
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to upload profile picture for $userId", e)
            Result.failure(e)
        }
    }

    suspend fun unlinkPartner(currentUserId: String, partnerId: String, coupleId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Remove couple document
            db.collection("couples").document(coupleId).delete().await()

            // Update both users
            db.collection("users").document(currentUserId).update(
                "partnerId", null,
                "coupleId", null
            ).await()

            db.collection("users").document(partnerId).update(
                "partnerId", null,
                "coupleId", null
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPairingRequest(fromUserId: String, toPartnerCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // 1. Find the partner by code
            val partnerQuery = db.collection("users")
                .whereEqualTo("partnerCode", toPartnerCode)
                .get()
                .await()

            if (partnerQuery.isEmpty) {
                return@withContext Result.failure(Exception("Partner code not found."))
            }

            val partnerSnap = partnerQuery.documents.first()
            val partnerId = partnerSnap.id

            if (fromUserId == partnerId) {
                return@withContext Result.failure(Exception("You cannot pair with yourself."))
            }

            // 2. Create the request
            val requestId = listOf(fromUserId, partnerId).sorted().joinToString("_")
            val requestData = hashMapOf(
                "fromId" to fromUserId,
                "toId" to partnerId,
                "status" to "PENDING",
                "createdAt" to DateUtils.getCurrentIsoTime()
            )

            db.collection("pairingRequests").document(requestId).set(requestData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observePairingRequest(userId: String): Flow<Map<String, Any>?> = callbackFlow {
        // Observe incoming requests WHERE toId == userId
        val incomingListener = db.collection("pairingRequests")
            .whereEqualTo("toId", userId)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val request = snapshot?.documents?.firstOrNull()?.data
                if (request != null) {
                    trySend(request)
                } else {
                    // If no incoming, check if we have an outgoing one
                    // In a real app, you'd combine these flows or use a more complex query
                    trySend(null)
                }
            }
        awaitClose { incomingListener.remove() }
    }

    suspend fun acceptPairingRequest(fromId: String, toId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val requestId = listOf(fromId, toId).sorted().joinToString("_")
            val coupleId = requestId // Same ID for simplicity

            val batch = db.batch()
            
            // 1. Update request status
            batch.update(db.collection("pairingRequests").document(requestId), "status", "ACCEPTED")
            
            // 2. Create couple
            val coupleData = hashMapOf(
                "coupleId" to coupleId,
                "user1Id" to fromId,
                "user2Id" to toId,
                "createdAt" to DateUtils.getCurrentIsoTime()
            )
            batch.set(db.collection("couples").document(coupleId), coupleData)
            
            // 3. Update users
            batch.update(db.collection("users").document(fromId), mapOf("partnerId" to toId, "coupleId" to coupleId))
            batch.update(db.collection("users").document(toId), mapOf("partnerId" to fromId, "coupleId" to coupleId))
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String, currentUserId: String): Result<List<UserProfile>> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (query.length < 3) return@withContext Result.success(emptyList())

            val results = db.collection("users")
                .whereEqualTo("isDiscoverable", true)
                .get()
                .await()

            val users = results.toObjects(UserProfile::class.java)
                .filter { user ->
                    user.userId != currentUserId && 
                    (user.name.contains(query, ignoreCase = true) || 
                     user.email.contains(query, ignoreCase = true))
                }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTimelineEvent(event: com.ourspace.app.data.model.TimelineEvent): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val docRef = db.collection("timeline").document()
            val eventWithId = event.copy(id = docRef.id)
            docRef.set(eventWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeTimelineEvents(coupleId: String): Flow<List<com.ourspace.app.data.model.TimelineEvent>> = callbackFlow {
        val listener = db.collection("timeline")
            .whereEqualTo("coupleId", coupleId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val events = snapshot?.toObjects(com.ourspace.app.data.model.TimelineEvent::class.java) ?: emptyList()
                // Sort by date manually if we don't have indexes yet
                trySend(events.sortedByDescending { it.date })
            }
        awaitClose { listener.remove() }
    }
}
