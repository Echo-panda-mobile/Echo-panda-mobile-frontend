package com.example.echo_panda_mobile.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

data class AdminModerationReportRequest(
    val reason: String? = null
)

// Dashboard Stats (computed from multiple sources)
data class DashboardStats(
    @SerializedName("total_users")
    val totalUsers: Int = 0,
    @SerializedName("active_artists")
    val activeArtists: Int = 0,
    @SerializedName("total_admins")
    val totalAdmins: Int = 0,
    @SerializedName("total_songs")
    val totalSongs: Int = 0,
    @SerializedName("total_albums")
    val totalAlbums: Int = 0,
    @SerializedName("flagged_content")
    val flaggedContent: Int = 0,
    @SerializedName("pending_reports")
    val pendingReports: Int = 0,
    @SerializedName("total_genres")
    val totalGenres: Int = 0,
    @SerializedName("total_tags")
    val totalTags: Int = 0
)

// Tags and Genres
data class TagResponse(
    val data: List<TagData> = emptyList()
)

data class TagData(
    val id: Int,
    val name: String,
    val slug: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("show_as_row")
    val showAsRow: Boolean = false,
    @SerializedName("songs_count")
    val songsCount: Int = 0
)

data class GenreResponse(
    val data: List<GenreData> = emptyList()
)

data class AlbumResponse(
    val data: List<AlbumDto> = emptyList()
)

data class GenreData(
    val id: Int,
    val name: String,
    val slug: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("show_as_row")
    val showAsRow: Boolean = false,
    @SerializedName("songs_count")
    val songsCount: Int = 0
)

data class CreateAdminArtistRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("artist_type") val artistType: String? = null,
    val gender: String? = null,
    @SerializedName("verification_status") val verificationStatus: String? = "pending"
)

data class CreateTagRequest(
    val name: String
)

data class CreateGenreRequest(
    val name: String
)

data class UpdateActiveStatusRequest(
    @SerializedName("is_active") val isActive: Boolean
)

data class UpdateShowAsRowRequest(
    @SerializedName("show_as_row") val showAsRow: Boolean
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

data class AdminAnalyticsResponse(
    val data: List<AnalyticsPoint> = emptyList()
)

data class AnalyticsPoint(
    val date: String,
    val count: Int
)

interface AdminApiService {
    // Mobile admin API — Sanctum Bearer token (backend: routes/api/mobile-admin.php)
    @GET("api/mb/admin/analytics")
    suspend fun getAnalytics(): AdminAnalyticsResponse

    @GET("api/mb/admin/tags")
    suspend fun getTags(): TagResponse

    @GET("api/mb/admin/genres")
    suspend fun getGenres(): GenreResponse

    @GET("api/mb/admin/albums")
    suspend fun getAlbums(
        @Query("tag_id") tagId: Int? = null,
        @Query("genre_id") genreId: Int? = null
    ): AlbumResponse

    @POST("api/mb/admin/artists")
    suspend fun createArtist(@Body body: CreateAdminArtistRequest): CreateAdminArtistResponse

    @POST("api/mb/admin/tags")
    suspend fun createTag(@Body body: CreateTagRequest): TagData

    @POST("api/mb/admin/genres")
    suspend fun createGenre(@Body body: CreateGenreRequest): GenreData

    @PUT("api/mb/admin/tags/{tag}")
    suspend fun updateTag(@Path("tag") tagId: Int, @Body body: CreateTagRequest): TagData

    @PATCH("api/mb/admin/tags/{tag}/status")
    suspend fun updateTagStatus(@Path("tag") tagId: Int, @Body body: UpdateActiveStatusRequest): TagData

    @PATCH("api/mb/admin/tags/{tag}/show-as-row")
    suspend fun updateTagShowAsRow(@Path("tag") tagId: Int, @Body body: UpdateShowAsRowRequest): TagData

    @DELETE("api/mb/admin/tags/{tag}")
    suspend fun deleteTag(@Path("tag") tagId: Int): Response<Unit>

    @PUT("api/mb/admin/albums/{album}")
    suspend fun updateAlbum(@Path("album") albumId: Int, @Body body: Map<String, @JvmSuppressWildcards Any>): AlbumDto

    @PUT("api/mb/admin/genres/{genre}")
    suspend fun updateGenre(@Path("genre") genreId: Int, @Body body: CreateGenreRequest): GenreData

    @PATCH("api/mb/admin/genres/{genre}/status")
    suspend fun updateGenreStatus(@Path("genre") genreId: Int, @Body body: UpdateActiveStatusRequest): GenreData

    @PATCH("api/mb/admin/genres/{genre}/show-as-row")
    suspend fun updateGenreShowAsRow(@Path("genre") genreId: Int, @Body body: UpdateShowAsRowRequest): GenreData

    @DELETE("api/mb/admin/genres/{genre}")
    suspend fun deleteGenre(@Path("genre") genreId: Int): Response<Unit>

    @PATCH("api/mb/admin/songs/{song}/status")
    suspend fun updateSongStatus(
        @Path("song") songId: String,
        @Body body: UpdateActiveStatusRequest
    ): Response<Unit>

    @POST("api/mb/admin/songs/{song}/approve")
    suspend fun approveSong(@Path("song") songId: String): Response<Unit>

    @POST("api/mb/admin/songs/{song}/hide")
    suspend fun hideSong(@Path("song") songId: String): Response<Unit>

    @POST("api/mb/admin/songs/{song}/report")
    suspend fun reportSong(
        @Path("song") songId: String,
        @Body body: AdminModerationReportRequest
    ): Response<Unit>

    @PATCH("api/mb/admin/albums/{album}/status")
    suspend fun updateAlbumStatus(
        @Path("album") albumId: String,
        @Body body: UpdateActiveStatusRequest
    ): Response<Unit>

    @POST("api/mb/admin/albums/{album}/approve")
    suspend fun approveAlbum(@Path("album") albumId: String): Response<Unit>

    @POST("api/mb/admin/albums/{album}/hide")
    suspend fun hideAlbum(@Path("album") albumId: String): Response<Unit>

    @POST("api/mb/admin/albums/{album}/report")
    suspend fun reportAlbum(
        @Path("album") albumId: String,
        @Body body: AdminModerationReportRequest
    ): Response<Unit>
}
