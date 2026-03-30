package com.ourspace.app.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "MediaRepository"

class MediaRepository {
    private val storage = FirebaseStorage.getInstance()

    /**
     * Uploads a file to Firebase Storage.
     * @param folder The folder path (e.g., "profiles/userId" or "memories/coupleId")
     * @param fileName The name of the file
     * @param localUri The URI of the local file to upload
     * @return Result containing the download URL on success
     */
    suspend fun uploadMedia(folder: String, fileName: String, localUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val path = "$folder/$fileName"
            val fileRef = storage.reference.child(path)
            
            Log.d(TAG, "Uploading media to: $path from Uri: $localUri")
            
            fileRef.putFile(localUri).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            
            Result.success(downloadUrl)
        } catch (e: com.google.firebase.storage.StorageException) {
            Log.e(TAG, "StorageException uploading media: HttpCode: ${e.httpResultCode}, ErrorCode: ${e.errorCode}, Message: ${e.message}", e)
            val isAppCheckOrAuth = e.httpResultCode == 401 || e.httpResultCode == 403
            val authStatus = if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) "Logged IN" else "Logged OUT"
            val customMessage = if (isAppCheckOrAuth) {
                "Authentication rejected by Firebase (HTTP ${e.httpResultCode}). AuthState: $authStatus. If using physical device, verify App Check Debug Token is registered in Console."
            } else {
                "Upload failed: ${e.message}"
            }
            Result.failure(Exception(customMessage, e))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload media to $folder/$fileName", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a file from Firebase Storage.
     * @param url The download URL of the file to delete
     */
    suspend fun deleteMedia(url: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val fileRef = storage.getReferenceFromUrl(url)
            fileRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete media: $url", e)
            Result.failure(e)
        }
    }
}
