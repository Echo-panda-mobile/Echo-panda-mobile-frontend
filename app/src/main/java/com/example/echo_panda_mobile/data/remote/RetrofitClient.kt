package com.example.echo_panda_mobile.data.remote

import com.example.echo_panda_mobile.data.repository.TokenStorage
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.echopanda.me/api/"
    private var apiService: AuthApiService? = null

    fun getInstance(tokenStorage: TokenStorage): AuthApiService {
        return apiService ?: synchronized(this) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(AuthInterceptor(tokenStorage))
                .build()

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(AuthApiService::class.java)
                .also { apiService = it }
        }
    }
}
