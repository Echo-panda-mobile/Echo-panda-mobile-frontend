package com.example.echo_panda_mobile.data.remote

import com.example.echo_panda_mobile.data.repository.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenStorage: TokenStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        
        // CRITICAL: If the URL is an AWS S3 Pre-Signed URL, we MUST NOT send the 
        // app's Authorization header. S3 URLs are self-contained.
        val isS3 = url.contains("amazonaws.com")
        
        val newRequestBuilder = request.newBuilder()
        
        if (isS3) {
            newRequestBuilder.removeHeader("Authorization")
        } else {
            val token = tokenStorage.getToken()
            if (!token.isNullOrBlank()) {
                newRequestBuilder.header("Authorization", "Bearer $token")
            }
        }
        
        return chain.proceed(newRequestBuilder.build())
    }
}
