package com.example.echo_panda_mobile.util

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Manages file uploads to S3 with pre-signed URLs
 * Handles progress tracking and error handling
 */
class S3UploadManager(private val httpClient: OkHttpClient = OkHttpClient()) {
    
    /**
     * Upload file to S3 using pre-signed URL
     * @param uploadUrl Pre-signed URL from backend
     * @param file File to upload
     * @param contentType MIME type of the file
     * @param onProgress Callback for upload progress (0-100)
     * @return True if successful, false otherwise
     */
    suspend fun uploadToS3(
        uploadUrl: String,
        file: File,
        contentType: String = "application/octet-stream",
        headers: Map<String, String>? = null,
        onProgress: (Int) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val progressRequestBody = ProgressRequestBody(
                file.asRequestBody(contentType.toMediaType()),
                file.length(),
                onProgress
            )
            
            val requestBuilder = Request.Builder()
                .url(uploadUrl)
                .put(progressRequestBody)

            // បន្ថែម Headers ដែលទទួលបានពី S3 Presigned URL
            headers?.forEach { (name, value) ->
                requestBuilder.addHeader(name, value)
            }
            
            val request = requestBuilder.build()
            
            val response = httpClient.newCall(request).execute()
            val isSuccessful = response.isSuccessful
            
            if (!isSuccessful) {
                val errorBody = response.body?.string()
                android.util.Log.e("S3UploadManager", "S3 Upload Failed: ${response.code} ${response.message}")
                android.util.Log.e("S3UploadManager", "Error Body: $errorBody")
            }
            
            response.close()
            isSuccessful
        } catch (e: Exception) {
            android.util.Log.e("S3UploadManager", "S3 Upload Exception: ${e.message}", e)
            false
        }
    }
    
    /**
     * Upload audio file to S3
     */
    suspend fun uploadAudio(
        uploadUrl: String,
        audioFile: File,
        headers: Map<String, String>? = null,
        onProgress: (Int) -> Unit = {}
    ): Boolean {
        return uploadToS3(uploadUrl, audioFile, "audio/mpeg", headers, onProgress)
    }
    
    /**
     * Upload image file to S3
     */
    suspend fun uploadImage(
        uploadUrl: String,
        imageFile: File,
        headers: Map<String, String>? = null,
        onProgress: (Int) -> Unit = {}
    ): Boolean {
        return uploadToS3(uploadUrl, imageFile, "image/jpeg", headers, onProgress)
    }
}

/**
 * Custom RequestBody that tracks upload progress
 */
private class ProgressRequestBody(
    private val delegate: RequestBody,
    private val totalSize: Long,
    private val onProgress: (Int) -> Unit
) : RequestBody() {
    
    override fun contentType() = delegate.contentType()
    
    override fun contentLength() = totalSize
    
    override fun writeTo(sink: BufferedSink) {
        val progressSink = ProgressSink(sink, totalSize, onProgress)
        val bufferedSink = progressSink.buffer()
        delegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }
}

/**
 * Sink that tracks write progress
 */
private class ProgressSink(
    delegate: Sink,
    private val totalSize: Long,
    private val onProgress: (Int) -> Unit
) : ForwardingSink(delegate) {
    
    private var uploadedSize = 0L
    
    override fun write(source: okio.Buffer, byteCount: Long) {
        super.write(source, byteCount)
        uploadedSize += byteCount
        val progress = if (totalSize > 0) {
            ((uploadedSize * 100) / totalSize).toInt().coerceIn(0, 100)
        } else {
            0
        }
        onProgress(progress)
    }
}
