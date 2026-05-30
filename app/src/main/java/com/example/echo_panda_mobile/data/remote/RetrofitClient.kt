package com.example.echo_panda_mobile.data.remote

import com.example.echo_panda_mobile.BuildConfig
import com.example.echo_panda_mobile.data.repository.TokenStorage
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val baseUrl: String = BuildConfig.API_BASE_URL
    private var authService: AuthApiService? = null
    private var musicService: MusicApiService? = null
    private var adminService: AdminApiService? = null

    fun getOkHttpClient(tokenStorage: TokenStorage): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(tokenStorage))
            .build()
    }

    private fun getRetrofit(tokenStorage: TokenStorage): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpClient(tokenStorage))
            .build()
    }

    fun getAuthService(tokenStorage: TokenStorage): AuthApiService {
        return authService ?: synchronized(this) {
            getRetrofit(tokenStorage).create(AuthApiService::class.java).also { authService = it }
        }
    }

    fun getMusicService(tokenStorage: TokenStorage): MusicApiService {
        return musicService ?: synchronized(this) {
            getRetrofit(tokenStorage).create(MusicApiService::class.java).also { musicService = it }
        }
    }

    fun getAdminService(tokenStorage: TokenStorage): AdminApiService {
        return adminService ?: synchronized(this) {
            getRetrofit(tokenStorage).create(AdminApiService::class.java).also { adminService = it }
        }
    }

    // Keep old method for compatibility if needed, but updated to use new naming
    fun getInstance(tokenStorage: TokenStorage): AuthApiService = getAuthService(tokenStorage)
}
