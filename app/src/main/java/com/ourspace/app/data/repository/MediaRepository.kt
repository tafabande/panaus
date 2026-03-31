package com.ourspace.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private const val TAG = "MediaRepository"

// Max dimension for any uploaded image. A 1080px image is plenty for a mobile app.
private const val MAX_IMAGE_DIMENSION = 1080
// JPEG quality (0-100). 80% gives great quality at a fraction of the original size.
private const val JPEG_QUALITY = 80

class MediaRepository {
    private val storage = FirebaseStorage.getInstance()

    /**
     * Compresses and uploads a file to Firebase Storage.
     * Images are downscaled to MAX_IMAGE_DIMENSION and JPEG-compressed on Dispatchers.IO
     * to avoid any main-thread work.
     *
     * @param context  Application context (needed to read the URI stream)
     * @param folder   The folder path (e.g., "profiles/userId" or "memories/coupleId")
     * @param fileName The name of the file
     * @param localUri The URI of the local file to upload
     * @return Result containing the download URL on success
     */
    suspend fun uploadMedia(context: Context, folder: String, fileName: String, localUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val path = "$folder/$fileName"
            val fileRef = storage.reference.child(path)

            Log.d(TAG, "Compressing image before upload: $localUri")
            val compressedBytes = compressImage(context, localUri)
            Log.d(TAG, "Compressed to ${compressedBytes.size / 1024} KB. Uploading to: $path")

            fileRef.putBytes(compressedBytes).await()
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
     * Reads a URI from the content resolver, downscales the bitmap to fit within
     * MAX_IMAGE_DIMENSION x MAX_IMAGE_DIMENSION, corrects EXIF orientation, then
     * compresses to JPEG. All work is performed on whatever thread this is called from
     * (callers must be on Dispatchers.IO).
     */
    private fun compressImage(context: Context, uri: Uri): ByteArray {
        // --- Step 1: Decode bounds only to calculate the sample size ---
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        val (srcWidth, srcHeight) = options.outWidth to options.outHeight
        var sampleSize = 1
        var w = srcWidth
        var h = srcHeight
        while (w > MAX_IMAGE_DIMENSION * 2 || h > MAX_IMAGE_DIMENSION * 2) {
            sampleSize *= 2
            w /= 2
            h /= 2
        }

        // --- Step 2: Decode with the sample size to save memory ---
        val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            })
        } ?: throw IllegalStateException("Could not open input stream for URI: $uri")

        // --- Step 3: Correct EXIF orientation so photos aren't rotated ---
        val rotation = try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90  -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        } catch (e: Exception) {
            Log.w(TAG, "Could not read EXIF data, skipping rotation correction", e)
            0f
        }

        val rotated = if (rotation != 0f) {
            val matrix = Matrix().apply { postRotate(rotation) }
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                .also { if (it !== decoded) decoded.recycle() }
        } else {
            decoded
        }

        // --- Step 4: Final scale down to MAX_IMAGE_DIMENSION if still too large ---
        val scaled = if (rotated.width > MAX_IMAGE_DIMENSION || rotated.height > MAX_IMAGE_DIMENSION) {
            val scale = MAX_IMAGE_DIMENSION.toFloat() / maxOf(rotated.width, rotated.height)
            Bitmap.createScaledBitmap(
                rotated,
                (rotated.width * scale).toInt(),
                (rotated.height * scale).toInt(),
                true
            ).also { if (it !== rotated) rotated.recycle() }
        } else {
            rotated
        }

        // --- Step 5: Compress to JPEG ByteArray ---
        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos)
        scaled.recycle()

        return baos.toByteArray()
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
