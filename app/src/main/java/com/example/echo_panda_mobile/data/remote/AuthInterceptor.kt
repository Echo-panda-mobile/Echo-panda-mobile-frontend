package com.example.echo_panda_mobile.data.remote

import com.example.echo_panda_mobile.data.repository.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenStorage: TokenStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStorage.getToken()
        val request = chain.request().newBuilder()
        
        if (!token.isNullOrBlank()) {
            request.addHeader("Authorization", "Bearer $token")
        }
        
        return chain.proceed(request.build())
    }
}
