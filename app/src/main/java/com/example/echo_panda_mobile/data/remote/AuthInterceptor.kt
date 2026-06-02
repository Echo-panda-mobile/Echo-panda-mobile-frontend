package com.example.echo_panda_mobile.data.remote

import android.util.Log
import com.example.echo_panda_mobile.data.repository.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenStorage: TokenStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val path = request.url.encodedPath
        
        // CRITICAL: If the URL is an AWS S3 Pre-Signed URL, we MUST NOT send the 
        // app's Authorization header. S3 URLs are self-contained.
        val isS3 = url.contains("amazonaws.com")
        
        // Check path instead of full URL to avoid issues with query parameters
        val isPublicAuthEndpoint = path.contains("/firebase/session")
            || path.endsWith("/login")
            || path.endsWith("/register")

        val newRequestBuilder = request.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("X-Requested-With", "XMLHttpRequest")

        if (isS3 || isPublicAuthEndpoint) {
            newRequestBuilder.removeHeader("Authorization")
        } else {
            val token = tokenStorage.getToken()
            if (!token.isNullOrBlank()) {
                // Restore sending Bearer token for ALL protected routes.
                // We will rely on the backend being updated to use 'auth:sanctum' 
                // for /admin/* routes, which is the best practice for mobile.
                newRequestBuilder.header("Authorization", "Bearer $token")
            }
        }
        
        return chain.proceed(newRequestBuilder.build())
    }
}
