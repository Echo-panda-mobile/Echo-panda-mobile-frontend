package com.example.echo_panda_mobile

import android.app.Application
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.TokenStorage
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import okhttp3.OkHttpClient

class EchoPandaApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        val tokenStorage = TokenStorage(this)
        
        return ImageLoader.Builder(this)
            .logger(DebugLogger())
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .okHttpClient {
                // Use the shared client which HAS the AuthInterceptor.
                // This allows Coil to call GET /api/songs/1/cover-url successfully.
                RetrofitClient.getOkHttpClient(tokenStorage)
            }
            .crossfade(true)
            .build()
    }
}
