package com.example.echo_panda_mobile.data.remote

import com.example.echo_panda_mobile.data.debug.AdminAuthDebug
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
            if (isOurDomain && (url.contains("/admin/") || url.contains("/api/"))) {
                AdminAuthDebug.logIncomingResponse(url, response, response.peekBody(512).string())
            }
            response
        } catch (e: Exception) {
            AdminAuthDebug.log("HTTP-OUT", "Connection error for $url: ${e.message}")
            throw e
        }
    }
}
