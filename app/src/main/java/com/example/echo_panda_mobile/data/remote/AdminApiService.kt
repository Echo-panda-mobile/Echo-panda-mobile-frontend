package com.example.echo_panda_mobile.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

data class CreateAdminArtistRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("artist_type") val artistType: String? = null,
    val gender: String? = null,
    @SerializedName("verification_status") val verificationStatus: String? = "pending"
)

data class CreatedAdminUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)

data class CreatedAdminArtist(
    val id: Int,
    val name: String,
    val slug: String,
    @SerializedName("verification_status") val verificationStatus: String?
)

data class CreateAdminArtistResponse(
    val message: String,
    @SerializedName("firebase_message") val firebaseMessage: String?,
    @SerializedName("firebase_provisioned") val firebaseProvisioned: Boolean? = null,
    val user: CreatedAdminUser,
    val artist: CreatedAdminArtist
)

interface AdminApiService {
    @POST("admin/artists")
    suspend fun createArtist(@Body body: CreateAdminArtistRequest): CreateAdminArtistResponse
}
