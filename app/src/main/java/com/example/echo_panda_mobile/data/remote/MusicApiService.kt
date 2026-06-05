package com.example.echo_panda_mobile.data.remote

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import java.lang.reflect.Type

// ─── Base Response Wrapper ──────────────────────────────────────────────────

data class BaseResponse<T>(
    @SerializedName("data") val data: T,
    @SerializedName("message") val message: String? = null
)

data class PaginatedResponse<T>(
    @SerializedName("data") val data: List<T>,
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("last_page") val lastPage: Int? = null,
    @SerializedName("total") val total: Int? = null
)

// ─── Artist DTOs ──────────────────────────────────────────────────────────────

data class ArtistDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("cover_image_url") val coverImageUrl: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("monthly_listeners") val monthlyListeners: String? = null
)

/** Popular-artist payload (`/api/artists/popular`); id is a string in the API response. */
data class MbArtistDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("monthly_listeners") val monthlyListeners: String? = null
)

data class MbGenreDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("songs_count") val songsCount: Int? = null
)

data class MbTagDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("songs_count") val songsCount: Int? = null
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

data class AlbumArtistField(
    val id: String? = null,
    val name: String = "Unknown"
)

class AlbumArtistFieldAdapter : JsonDeserializer<AlbumArtistField> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): AlbumArtistField {
        if (json == null || json.isJsonNull) return AlbumArtistField()
        return when {
            json.isJsonPrimitive -> AlbumArtistField(name = json.asString)
            json.isJsonObject -> {
                val obj = json.asJsonObject
                val idElement = obj.get("id")
                val id = when {
                    idElement == null || idElement.isJsonNull -> null
                    idElement.isJsonPrimitive -> {
                        val prim = idElement.asJsonPrimitive
                        if (prim.isNumber) prim.asNumber.toString() else prim.asString
                    }
                    else -> null
                }
                val name = obj.get("name")?.takeIf { !it.isJsonNull }?.asString
                    ?: obj.get("stage_name")?.takeIf { !it.isJsonNull }?.asString
                    ?: obj.get("artist_name")?.takeIf { !it.isJsonNull }?.asString
                    ?: "Unknown"
                AlbumArtistField(id = id, name = name)
            }
            else -> AlbumArtistField()
        }
    }
}

data class AlbumDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("artist_id") val artistId: Int? = null,
    @SerializedName("artist_name") val artistName: String? = null,
    @SerializedName("artist")
    @JsonAdapter(AlbumArtistFieldAdapter::class)
    val artist: AlbumArtistField? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("cover_key") val coverKey: String? = null,
    @SerializedName("release_status") val releaseStatus: String? = "published",
    @SerializedName("is_active") val isActive: Boolean? = true,
    @SerializedName("songs_count") val songsCount: Int? = null,
    @SerializedName("cover_image") val coverImage: String? = null
) {
    fun getDisplayCoverUrl(): String? = coverUrl ?: coverImage ?: coverKey
}

data class ArtistAlbumsResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("data") val data: List<AlbumDto>? = null,
    @SerializedName("albums") val albums: List<AlbumDto>? = null
) {
    val allAlbums: List<AlbumDto> get() = data ?: albums ?: emptyList()
}

data class ArtistSongsResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("data") val data: List<SongDto>? = null,
    @SerializedName("songs") val songs: List<SongDto>? = null,
    @SerializedName("tracks") val tracks: List<SongDto>? = null
) {
    val allSongs: List<SongDto> get() = data ?: songs ?: tracks ?: emptyList()
}

data class CreateAlbumRequest(
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String,
    @SerializedName("artist_id") val artistId: Int? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("cover_key") val coverKey: String? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("release_status") val releaseStatus: String? = "published"
)

// ─── Song DTOs ────────────────────────────────────────────────────────────────

data class SongDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("artist_id") val artistId: Int? = null,
    @SerializedName("artist_name") val artistName: String? = null,
    @SerializedName("artist")
    @JsonAdapter(AlbumArtistFieldAdapter::class)
    val artist: AlbumArtistField? = null,
    @SerializedName("duration") val durationSeconds: Int? = null,
    @SerializedName("track_number") val trackNumber: Int? = null,
    @SerializedName("album_id") val albumId: Int? = null,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("cover_key") val coverKey: String? = null,
    @SerializedName("original_key") val originalKey: String? = null,
    @SerializedName("audio_url") val audioUrl: String? = null,
    @SerializedName("album") val album: AlbumDto? = null,
    @SerializedName("lyrics") val lyrics: String? = null,
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("tag_id") val tagId: Int? = null,
    @SerializedName("is_favorited") val isFavorite: Boolean? = null,
    @SerializedName("is_active") val isActive: Boolean? = true
) {
    fun getDisplayCoverUrl(): String? = coverUrl ?: coverKey ?: album?.getDisplayCoverUrl()
}

data class FavoriteItemDto(
    @SerializedName("song") val song: SongDto? = null,
    @SerializedName("favoritable") val favoritable: SongDto? = null,
    @SerializedName("song_id") val songId: Int? = null,
    @SerializedName("favoritable_id") val favoritableId: Int? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("artist_name") val artistName: String? = null,
    @SerializedName("duration") val durationSeconds: Int? = null,
    @SerializedName("track_number") val trackNumber: Int? = null,
    @SerializedName("album_id") val albumId: Int? = null,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("cover_key") val coverKey: String? = null,
    @SerializedName("album") val album: AlbumDto? = null,
    @SerializedName("is_favorited") val isFavorite: Boolean? = null
) {
    fun getDisplayCoverUrl(): String? = coverUrl ?: coverKey ?: album?.getDisplayCoverUrl() ?: favoritable?.getDisplayCoverUrl() ?: song?.getDisplayCoverUrl()
}

data class PlaylistDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("title") val title: String? = null,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("label") val label: String? = null
)

data class CreatePlaylistRequest(
    @SerializedName("name") val name: String
)

data class AddSongToPlaylistRequest(
    @SerializedName("song_id") val songId: Int
)

data class ListenHistoryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("song_id") val songId: Int,
    @SerializedName("song") val song: SongDto,
    @SerializedName("duration_listened") val durationListened: Int? = null,
    @SerializedName("completed") val completed: Boolean? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class PlayHistoryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("song_id") val songId: Int,
    @SerializedName("progress_seconds") val progressSeconds: Int,
    @SerializedName("song") val song: SongDto
)

data class CreateSongRequest(
    @SerializedName("album_id") val albumId: Int? = null,
    @SerializedName("title") val title: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("track_number") val trackNumber: Int,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("lyrics") val lyrics: String? = null,
    @SerializedName("original_key") val originalKey: String? = null, 
    @SerializedName("cover_key") val coverKey: String? = null,
    @SerializedName("preview_key") val previewKey: String? = null
)

data class UpdateSongRequest(
    @SerializedName("album_id") val albumId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String? = null,
    @SerializedName("duration") val duration: Int,
    @SerializedName("track_number") val trackNumber: Int,
    @SerializedName("lyrics") val lyrics: String? = null,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("tag_id") val tagId: Int? = null,
    @SerializedName("cover_key") val coverKey: String? = null,
    @SerializedName("original_key") val originalKey: String? = null,
)

data class UploadResponse(
    @SerializedName("key") val key: String,
    @SerializedName("url") val url: String
)

data class PresignedUrlResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("purpose") val purpose: String?,
    @SerializedName("key") val key: String,
    @SerializedName("url") val url: String?,
    @SerializedName("upload_url") val uploadUrl: String,
    @SerializedName("headers") val headers: Map<String, String>?
)

data class PresignUploadRequest(
    @SerializedName("purpose") val purpose: String,
    @SerializedName("filename") val fileName: String,
    @SerializedName("content_type") val contentType: String,
    @SerializedName("size") val size: Long
)

data class ArtistAnalyticsDto(
    @SerializedName("stats") val stats: DashboardStatsDto,
    @SerializedName("top_track") val topTrack: SongDto?,
    @SerializedName("recent_activities") val recentActivities: List<ActivityDto>
)

data class DashboardStatsDto(
    @SerializedName("monthly_revenue") val monthlyRevenue: Double,
    @SerializedName("revenue_growth") val revenueGrowth: Double,
    @SerializedName("streams") val streams: String,
    @SerializedName("listeners") val listeners: String,
    @SerializedName("followers") val followers: String,
    @SerializedName("published_songs") val publishedSongs: Int,
    @SerializedName("total_albums") val totalAlbums: Int,
    @SerializedName("total_likes") val totalLikes: String
)

data class ActivityDto(
    @SerializedName("id") val id: String,
    @SerializedName("text") val text: String,
    @SerializedName("timestamp") val timestamp: Long
)

data class NotificationDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("is_read") val isRead: Boolean
)

// ─── Service Interface ────────────────────────────────────────────────────────

interface MusicApiService {

    @GET("api/artists")
    suspend fun getArtists(): Response<BaseResponse<List<ArtistDto>>>

    @GET("api/artists/popular")
    suspend fun getPopularArtists(
        @Query("limit") limit: Int? = null
    ): Response<BaseResponse<List<MbArtistDto>>>

    @GET("api/mb/genres")
    suspend fun getGenres(
        @Query("limit") limit: Int? = null
    ): Response<BaseResponse<List<MbGenreDto>>>

    @GET("api/genres")
    suspend fun getPublicGenres(): Response<BaseResponse<List<MbGenreDto>>>

    @GET("api/mb/tags")
    suspend fun getTags(
        @Query("limit") limit: Int? = null
    ): Response<BaseResponse<List<MbTagDto>>>

    @GET("api/tags")
    suspend fun getPublicTags(): Response<BaseResponse<List<MbTagDto>>>

    @GET("api/artists/{id}")
    suspend fun getArtistDetail(
        @Path("id") artistId: String
    ): Response<ArtistDto>

    @GET("api/artists/{artist}/image-url")
    suspend fun getArtistImageUrl(
        @Path("artist") artistId: String
    ): Response<ArtistImageUrlResponse>

    @GET("api/genres/{genre}/image-url")
    suspend fun getGenreImageUrl(
        @Path("genre") genreId: String
    ): Response<ArtistImageUrlResponse>

    @GET("api/tags/{tag}/image-url")
    suspend fun getTagImageUrl(
        @Path("tag") tagId: String
    ): Response<ArtistImageUrlResponse>

    // ─── Albums ───
    @GET("api/albums")
    suspend fun getAlbums(
        @Query("search") search: String? = null,
        @Query("sort_by") sortBy: String? = "latest",
        @Query("per_page") perPage: Int? = null
    ): Response<PaginatedResponse<AlbumDto>>

    @GET("api/albums/{id}")
    suspend fun getAlbumDetail(
        @Path("id") albumId: String
    ): Response<AlbumDto>

    @POST("api/albums")
    suspend fun createAlbum(
        @Body request: CreateAlbumRequest
    ): Response<BaseResponse<AlbumDto>>

    @PUT("api/albums/{id}")
    suspend fun updateAlbum(
        @Path("id") albumId: String,
        @Body request: CreateAlbumRequest
    ): Response<BaseResponse<AlbumDto>>

    @DELETE("api/albums/{id}")
    suspend fun deleteAlbum(
        @Path("id") albumId: String
    ): Response<BaseResponse<Unit>>

    // ─── Songs ───
    @GET("api/songs")
    suspend fun getSongs(
        @Query("search") search: String? = null,
        @Query("album_id") albumId: Int? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("tag_id") tagId: Int? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<PaginatedResponse<SongDto>>

    @GET("api/songs/{id}")
    suspend fun getSongDetail(
        @Path("id") songId: String
    ): Response<SongDto>

    @POST("api/songs")
    suspend fun createSong(
        @Body request: CreateSongRequest
    ): Response<BaseResponse<SongDto>>

    @PUT("api/songs/{id}")
    suspend fun updateSong(
        @Path("id") songId: String,
        @Body request: UpdateSongRequest
    ): Response<BaseResponse<SongDto>>

    @DELETE("api/songs/{id}")
    suspend fun deleteSong(
        @Path("id") songId: String
    ): Response<BaseResponse<Unit>>

    @GET("api/songs/{id}/cover-url")
    suspend fun getSongCoverUrl(
        @Path("id") songId: String
    ): Response<SongCoverUrlResponse>

    @GET("api/albums/{id}/cover-url")
    suspend fun getAlbumCoverUrl(
        @Path("id") albumId: String
    ): Response<SongCoverUrlResponse>

    @GET("api/songs/{id}/signed-url")
    suspend fun getStreamTicket(
        @Path("id") songId: String
    ): Response<StreamTicketResponse>

    @GET("api/playback/recent")
    suspend fun getRecentlyPlayed(): Response<BaseResponse<List<SongDto>>>

    @GET("api/profile/favorite-songs")
    suspend fun getFavorites(): Response<PaginatedResponse<FavoriteItemDto>>

    @GET("api/playlists/{playlist}/songs")
    suspend fun getPlaylistSongs(
        @Path("playlist") playlistId: String,
        @Query("per_page") perPage: Int? = null
    ): Response<PaginatedResponse<SongDto>>

    // Playlists endpoints
    @GET("api/playlists")
    suspend fun getPlaylists(
        @Query("per_page") perPage: Int? = null
    ): Response<PaginatedResponse<PlaylistDto>>

    @POST("api/playlists")
    suspend fun createPlaylist(
        @Body request: CreatePlaylistRequest
    ): Response<BaseResponse<PlaylistDto>>

    @POST("api/playlists/{playlist}/songs")
    suspend fun addSongToPlaylist(
        @Path("playlist") playlistId: String,
        @Body request: AddSongToPlaylistRequest
    ): Response<Unit>

    @DELETE("api/playlists/{playlist}/songs/{songId}")
    suspend fun removeSongFromPlaylist(
        @Path("playlist") playlistId: String,
        @Path("songId") songId: String
    ): Response<Unit>

    @GET("api/playback/continue")
    suspend fun getContinueListening(): Response<BaseResponse<List<PlayHistoryDto>>>

    @GET("api/listen-history")
    suspend fun getListenHistory(
        @Query("per_page") perPage: Int? = null
    ): Response<PaginatedResponse<ListenHistoryDto>>

    @POST("api/favorites/songs")
    suspend fun toggleFavorite(
        @Body request: CheckFavoriteRequest
    ): Response<Unit>

    @POST("api/favorites/songs/remove")
    suspend fun removeFavorite(
        @Body request: CheckFavoriteRequest
    ): Response<Unit>

    @POST("api/favorites/songs/check")
    suspend fun checkIsFavorite(
        @Body request: CheckFavoriteRequest
    ): Response<CheckFavoriteResponse>

    @POST("api/listen-history")
    suspend fun addToListenHistory(
        @Body request: ListenHistoryRequest
    ): Response<Unit>

    @Multipart
    @POST("api/upload/media")
    suspend fun uploadMedia(
        @Part file: MultipartBody.Part,
        @Part("purpose") purpose: RequestBody 
    ): Response<UploadResponse>

    @POST("api/upload/media/presign")
    suspend fun getUploadUrl(
        @Body request: PresignUploadRequest
    ): Response<PresignedUrlResponse>

    @GET("api/songs")
    suspend fun getMySongs(
        @Query("per_page") perPage: Int = 500,
        @Query("sort_by") sortBy: String = "latest"
    ): Response<ArtistSongsResponse>

    @GET("api/albums")
    suspend fun getMyAlbums(
        @Query("per_page") perPage: Int = 500,
        @Query("sort_by") sortBy: String = "latest"
    ): Response<ArtistAlbumsResponse>

    @GET("api/artist/analytics")
    suspend fun getArtistAnalytics(): Response<BaseResponse<ArtistAnalyticsDto>>

    @GET("api/mb/artist/top-listened-songs")
    suspend fun getArtistTopListenedSongs(
        @Query("limit") limit: Int = 6
    ): Response<BaseResponse<List<MbArtistTopSongDto>>>

    @PUT("api/artist/profile")
    suspend fun updateArtistProfile(
        @Body profile: ArtistDto
    ): Response<BaseResponse<ArtistDto>>

    @GET("api/artist/notifications")
    suspend fun getNotifications(): Response<BaseResponse<List<NotificationDto>>>

    @GET("api/stats/most-played-albums")
    suspend fun getMostPlayedAlbums(
        @Query("limit") limit: Int? = 10
    ): Response<BaseResponse<List<MostPlayedAlbumDto>>>

    @GET("api/stats/most-played-songs")
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

data class MbArtistTopSongDto(
    @SerializedName("song") val song: SongDto,
    @SerializedName("listen_count") val listenCount: Int? = null,
    @SerializedName("stream_count") val streamCount: Int? = null,
    @SerializedName("play_count") val playCount: Int? = null
)
