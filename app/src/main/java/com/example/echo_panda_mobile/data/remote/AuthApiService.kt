package com.example.echo_panda_mobile.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.Response

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
    @SerializedName("cover_image_url") val coverImageUrl: String? = null,
    val firebase_uid: String? = null,
    val provider: String? = null,
    val id_token: String? = null
) {
    fun profileImageSource(): String? =
        image_url?.takeIf { it.isNotBlank() && it != "null" }
            ?: coverImageUrl?.takeIf { it.isNotBlank() && it != "null" }
}

data class BackendUser(
    val id: Int,
    val user_id: Int? = null,
    val name: String,
    val email: String,
    val role: String,
    @SerializedName("image_url") val imageUrl: String? = null,
    val artist_id: Int? = null,
    val artist: BackendArtist? = null
)

data class UpdateProfileRequest(
    val name: String,
    val email: String,
    @SerializedName("image_url") val imageUrl: String? = null
)

data class UpdateProfileResponse(
    val message: String? = null,
    val user: BackendUser
)

data class UserPresignRequest(
    val filename: String,
    @SerializedName("content_type") val contentType: String,
    val size: Long
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

    @GET("api/profile")
    suspend fun profile(): MeResponse

    @PUT("api/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<UpdateProfileResponse>

    @POST("api/upload/user-image/presign")
    suspend fun presignUserImage(@Body body: UserPresignRequest): Response<PresignedUrlResponse>

    @GET("api/users/{user}/image-url")
    suspend fun getUserImageUrl(@Path("user") userId: Int): Response<ArtistImageUrlResponse>

    @GET("api/users/by-role")
    suspend fun usersByRole(): UsersByRoleResponse

    @POST("api/login")
    suspend fun login(@Body body: com.example.echo_panda_mobile.data.model.LoginRequest): com.example.echo_panda_mobile.data.model.AuthResponse

    @POST("api/logout")
    suspend fun logout()
}
