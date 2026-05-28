package com.example.echo_panda_mobile.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

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
    @SerializedName("url") val url: String
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
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String?,
    @SerializedName("duration") val durationSeconds: Int,
    @SerializedName("track_number") val trackNumber: Int,
    @SerializedName("album_id") val albumId: Int?,
    @SerializedName("cover_url") val coverUrl: String? = null
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
