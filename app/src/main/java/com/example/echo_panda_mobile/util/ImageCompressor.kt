package com.example.echo_panda_mobile.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {
    fun compress(context: Context, uri: Uri, filePrefix: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return null

            val maxSize = 800
            val width = bitmap.width
            val height = bitmap.height
            val resizedBitmap = if (width > maxSize || height > maxSize) {
                val ratio = width.toFloat() / height.toFloat()
                val newWidth = if (width > height) maxSize else (maxSize * ratio).toInt()
                val newHeight = if (height > width) maxSize else (maxSize / ratio).toInt()
                android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }

            val file = File(context.cacheDir, "${filePrefix}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream ->
                resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
            }
            file
        } catch (_: Exception) {
            null
        }
    }
}
