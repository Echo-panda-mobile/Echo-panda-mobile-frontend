package com.example.echo_panda_mobile.util

import android.media.MediaMetadataRetriever
import java.io.File

object AudioMetadataExtractor {
    
    /**
     * Extract duration from audio file in seconds
     */
    fun getAudioDuration(file: File): Int {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            
            val duration = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: 0L
            
            retriever.release()
            (duration / 1000).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
    
    /**
     * Extract multiple metadata from audio file
     */
    fun getAudioMetadata(file: File): AudioMetadata {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            
            val durationMs = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: 0L
            
            val title = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_TITLE
            ) ?: "Unknown"
            
            val artist = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_ARTIST
            ) ?: "Unknown"
            
            val album = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_ALBUM
            ) ?: "Unknown"
            
            val genre = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_GENRE
            ) ?: "Unknown"
            
            retriever.release()
            
            AudioMetadata(
                duration = (durationMs / 1000).toInt(),
                title = title,
                artist = artist,
                album = album,
                genre = genre
            )
        } catch (e: Exception) {
            e.printStackTrace()
            AudioMetadata(duration = 0)
        }
    }
    
    /**
     * Get file size in MB
     */
    fun getFileSizeInMB(file: File): Double {
        return file.length().toDouble() / (1024 * 1024)
    }
}

data class AudioMetadata(
    val duration: Int,
    val title: String = "Unknown",
    val artist: String = "Unknown",
    val album: String = "Unknown",
    val genre: String = "Unknown"
)
