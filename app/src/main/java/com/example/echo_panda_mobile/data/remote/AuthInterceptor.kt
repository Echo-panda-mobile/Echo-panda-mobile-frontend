package com.example.echo_panda_mobile.data.remote

import android.util.Log
import com.example.echo_panda_mobile.data.repository.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenStorage: TokenStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()
        val path = originalRequest.url.encodedPath
        val token = tokenStorage.getToken()
        
        val isS3 = url.contains("amazonaws.com")
        val isStorage = url.contains("/storage/")
        val isPublicAuthEndpoint = path.contains("/firebase/session")
            || path.endsWith("/login")
            || path.endsWith("/register")

        Log.d("AuthInterceptor", "━━━ REQUEST INTERCEPTOR ━━━")
        Log.d("AuthInterceptor", "URL: $url")
        Log.d("AuthInterceptor", "Token: ${if (token.isNullOrBlank()) "MISSING" else "PRESENT"}")

        if (isS3 || isStorage || isPublicAuthEndpoint) {
            Log.d("AuthInterceptor", "Public, S3, or Storage endpoint: Removing Auth headers")
            val cleanRequest = originalRequest.newBuilder()
                .removeHeader("Authorization")
                .build()
            return chain.proceed(cleanRequest)
        }

        val requestBuilder = originalRequest.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("X-Requested-With", "XMLHttpRequest")

        if (!token.isNullOrBlank()) {
            Log.d("AuthInterceptor", "Adding Bearer token")
            requestBuilder.header("Authorization", "Bearer $token")
        } else {
            Log.w("AuthInterceptor", "⚠️ No token found for authenticated endpoint!")
        }
        
        val request = requestBuilder.build()
        
        try {
            val response = chain.proceed(request)
            Log.d("AuthInterceptor", "Response status: ${response.code} for ${request.url}")
            return response
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Exception during request: ${e.message}")
            throw e
        }
    }
}
