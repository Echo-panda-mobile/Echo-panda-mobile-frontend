package com.example.echo_panda_mobile.data.remote

import android.util.Log
import com.example.echo_panda_mobile.data.repository.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenStorage: TokenStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()
        val token = tokenStorage.getToken()
        
        val isOurDomain = url.contains("echopanda.me")
        val isPublicAuth = url.contains("/login") || url.contains("/register") || url.contains("/firebase/session")
        val isS3 = url.contains("amazonaws.com")

        val requestBuilder = originalRequest.newBuilder()

        if (isOurDomain && !token.isNullOrBlank() && !isPublicAuth) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val isImage = url.lowercase().run { 
            endsWith(".jpg") || endsWith(".jpeg") || endsWith(".png") || endsWith(".webp") 
        }
        
        if (!isImage && !isS3) {
            requestBuilder.addHeader("Accept", "application/json")
            requestBuilder.addHeader("X-Requested-With", "XMLHttpRequest")
        }
        
        val request = requestBuilder.build()
        return try {
            val response = chain.proceed(request)
            if (response.code == 403) {
                Log.e("AuthInterceptor", "403 Forbidden for URL: ${request.url}")
            }
            response
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Connection Error: ${e.message}")
            throw e
        }
    }
}
