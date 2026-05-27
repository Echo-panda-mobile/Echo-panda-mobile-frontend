package com.example.echo_panda_mobile.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class FirebaseLoginRequest(
    val email: String,
    val name: String,
    val firebase_uid: String,
    val provider: String
)

data class BackendUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)

data class FirebaseLoginResponse(
    val user: BackendUser,
    val token: String
)

interface AuthApiService {
    @POST("firebase/login")
    suspend fun firebaseLogin(@Body body: FirebaseLoginRequest): FirebaseLoginResponse

    @POST("logout")
    suspend fun logout()
}
