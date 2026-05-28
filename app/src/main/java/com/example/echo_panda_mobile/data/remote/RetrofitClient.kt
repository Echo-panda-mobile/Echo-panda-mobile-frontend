package com.example.echo_panda_mobile.data.remote

import com.example.echo_panda_mobile.data.repository.TokenStorage
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.echopanda.me/api/"
    private var authService: AuthApiService? = null
    private var musicService: MusicApiService? = null

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
            .baseUrl(BASE_URL)
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

    // Keep old method for compatibility if needed, but updated to use new naming
    fun getInstance(tokenStorage: TokenStorage): AuthApiService = getAuthService(tokenStorage)
}
