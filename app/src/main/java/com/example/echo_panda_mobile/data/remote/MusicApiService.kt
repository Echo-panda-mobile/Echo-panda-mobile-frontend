package com.example.echo_panda_mobile.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

// ─── Base Response Wrapper ──────────────────────────────────────────────────

data class BaseResponse<T>(
    @SerializedName("data") val data: T
)

data class PaginatedResponse<T>(
    @SerializedName("data") val data: List<T>,
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("last_page") val lastPage: Int? = null,
    @SerializedName("total") val total: Int? = null
)

// ─── Artist DTOs ──────────────────────────────────────────────────────────────

data class ArtistDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("monthly_listeners") val monthlyListeners: String? = null
)

data class ArtistImageUrlResponse(
    @SerializedName("url") val url: String? = null,
    @SerializedName("signed_url") val signedUrl: String? = null,
    @SerializedName("artist_id") val artistId: Int? = null,
    @SerializedName("expires_in_seconds") val expiresInSeconds: Int? = null
)

data class SongCoverUrlResponse(
    @SerializedName("url") val url: String? = null,
    @SerializedName("signed_url") val signedUrl: String? = null,
    @SerializedName("expires_in_seconds") val expiresInSeconds: Int? = null
)

data class CheckFavoriteRequest(
    @SerializedName("song_id") val songId: Int
)

data class CheckFavoriteResponse(
    @SerializedName("is_favorited") val isFavorite: Boolean
)

data class ListenHistoryRequest(
    @SerializedName("song_id") val songId: Int,
    @SerializedName("duration_listened") val durationListened: Int? = null,
    @SerializedName("completed") val completed: Boolean? = null
)

data class StreamTicketResponse(
    @SerializedName("song_id") val songId: Int? = null,
    @SerializedName("quality") val quality: String? = null,
    @SerializedName("expires_in_seconds") val expiresInSeconds: Int? = null,
    @SerializedName("signed_url") val signedUrl: String? = null,
    @SerializedName("stream_url") val streamUrl: String? = null,
    @SerializedName("url") val url: String? = null
)

// ─── Album DTOs ───────────────────────────────────────────────────────────────

data class AlbumDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("cover_url") val coverUrl: String? = null
)

// ─── Song DTOs ────────────────────────────────────────────────────────────────

data class SongDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("artist_name") val artistName: String?,
    @SerializedName("duration") val durationSeconds: Int,
    @SerializedName("track_number") val trackNumber: Int,
    @SerializedName("album_id") val albumId: Int?,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("album") val album: AlbumDto? = null
)

// ─── Service Interface ────────────────────────────────────────────────────────

interface MusicApiService {

    @GET("artists")
    suspend fun getArtists(): Response<BaseResponse<List<ArtistDto>>>

    @GET("artists/{id}")
    suspend fun getArtistDetail(
        @Path("id") artistId: String
    ): Response<ArtistDto>

    @GET("artists/{artist}/image-url")
    suspend fun getArtistImageUrl(
        @Path("artist") artistId: String
    ): Response<ArtistImageUrlResponse>

    @GET("albums")
    suspend fun getAlbums(
        @Query("search") search: String? = null,
        @Query("sort_by") sortBy: String? = "latest",
        @Query("per_page") perPage: Int? = null
    ): Response<PaginatedResponse<AlbumDto>>

    @GET("albums/{id}")
    suspend fun getAlbumDetail(
        @Path("id") albumId: String
    ): Response<AlbumDto>

    @GET("songs")
    suspend fun getSongs(
        @Query("search") search: String? = null,
        @Query("album_id") albumId: Int? = null
    ): Response<PaginatedResponse<SongDto>>

    @GET("songs/{id}")
    suspend fun getSongDetail(
        @Path("id") songId: String
    ): Response<SongDto>

    @GET("songs/{id}/cover-url")
    suspend fun getSongCoverUrl(
        @Path("id") songId: String
    ): Response<SongCoverUrlResponse>

    @GET("albums/{id}/cover-url")
    suspend fun getAlbumCoverUrl(
        @Path("id") albumId: String
    ): Response<SongCoverUrlResponse>

    @GET("songs/{id}/signed-url")
    suspend fun getStreamTicket(
        @Path("id") songId: String
    ): Response<StreamTicketResponse>

    @POST("favorites/songs")
    suspend fun toggleFavorite(
        @Body request: CheckFavoriteRequest
    ): Response<Unit>

    @POST("favorites/songs/check")
    suspend fun checkIsFavorite(
        @Body request: CheckFavoriteRequest
    ): Response<CheckFavoriteResponse>

    @POST("listen-history")
    suspend fun addToListenHistory(
        @Body request: ListenHistoryRequest
    ): Response<Unit>

    @GET("stats/most-played-albums")
    suspend fun getMostPlayedAlbums(
        @Query("limit") limit: Int? = 10
    ): Response<BaseResponse<List<MostPlayedAlbumDto>>>

    @GET("stats/most-played-songs")
    suspend fun getMostPlayedSongs(
        @Query("limit") limit: Int? = 10
    ): Response<BaseResponse<List<MostPlayedSongDto>>>
}

data class MostPlayedAlbumDto(
    @SerializedName("album") val album: AlbumDto,
    @SerializedName("play_count") val playCount: Int? = null
)

data class MostPlayedSongDto(
    @SerializedName("song") val song: SongDto,
    @SerializedName("play_count") val playCount: Int? = null
)
