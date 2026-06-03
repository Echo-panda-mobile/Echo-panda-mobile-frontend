package com.example.echo_panda_mobile.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class FirebaseSessionRequest(
    val id_token: String,
    val email: String? = null,
    val name: String? = null,
    val provider: String? = null
)

data class BackendArtist(
    val id: Int,
    val name: String,
    val image_url: String? = null,
    val firebase_uid: String? = null,
    val provider: String? = null,
    val id_token: String? = null
)

data class BackendUser(
    val id: Int,
    val user_id: Int? = null,
    val name: String,
    val email: String,
    val role: String,
    val artist_id: Int? = null,
    val artist: BackendArtist? = null
)

data class FirebaseSessionResponse(
    val message: String? = null,
    val user: BackendUser,
    val token: String,
    val token_type: String? = null,
    val firebase_uid: String? = null,
    val provider: String? = null,
    val redirect_to: String? = null
)

data class MeResponse(
    val user: BackendUser
)

data class UsersByRoleResponse(
    @SerializedName("normal_users") val normalUsers: List<BackendUser> = emptyList(),
    @SerializedName("artist_users") val artistUsers: List<BackendUser> = emptyList(),
    @SerializedName("admin_users") val adminUsers: List<BackendUser> = emptyList()
)

interface AuthApiService {
    @POST("api/firebase/session")
    suspend fun firebaseSession(@Body body: FirebaseSessionRequest): FirebaseSessionResponse

    @GET("api/me")
    suspend fun me(): MeResponse

    @GET("api/users/by-role")
    suspend fun usersByRole(): UsersByRoleResponse

    @POST("api/login")
    suspend fun login(@Body body: com.example.echo_panda_mobile.data.model.LoginRequest): com.example.echo_panda_mobile.data.model.AuthResponse

    @POST("api/logout")
    suspend fun logout()
}
