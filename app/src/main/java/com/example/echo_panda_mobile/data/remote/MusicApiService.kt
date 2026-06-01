package com.example.echo_panda_mobile.data.remote

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*
import java.lang.reflect.Type

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

/** API may return artist as a string (lists) or nested object (album detail). */
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
    @SerializedName("artist_name") val artistName: String? = null,
    @SerializedName("artist")
    @JsonAdapter(AlbumArtistFieldAdapter::class)
    val artist: AlbumArtistField? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("cover_url") val coverUrl: String? = null
)

// ─── Song DTOs ────────────────────────────────────────────────────────────────

data class SongDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("artist_name") val artistName: String? = null,
    @SerializedName("duration") val durationSeconds: Int? = null,
    @SerializedName("track_number") val trackNumber: Int? = null,
    @SerializedName("album_id") val albumId: Int? = null,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("album") val album: AlbumDto? = null,
    @SerializedName("is_favorited") val isFavorite: Boolean? = null
)

/** Favorites may return a flat song or `{ "song": { ... } }` — this DTO handles both. */
data class FavoriteItemDto(
    @SerializedName("song") val song: SongDto? = null,
    @SerializedName("song_id") val songId: Int? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("artist_name") val artistName: String? = null,
    @SerializedName("duration") val durationSeconds: Int? = null,
    @SerializedName("track_number") val trackNumber: Int? = null,
    @SerializedName("album_id") val albumId: Int? = null,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("album") val album: AlbumDto? = null,
    @SerializedName("is_favorited") val isFavorite: Boolean? = null
)

// Playlist DTOs
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

// ─── Service Interface ────────────────────────────────────────────────────────

interface MusicApiService {

    @GET("api/artists")
    suspend fun getArtists(): Response<BaseResponse<List<ArtistDto>>>

    @GET("api/artists/{id}")
    suspend fun getArtistDetail(
        @Path("id") artistId: String
    ): Response<ArtistDto>

    @GET("api/artists/{artist}/image-url")
    suspend fun getArtistImageUrl(
        @Path("artist") artistId: String
    ): Response<ArtistImageUrlResponse>

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

    @GET("api/songs")
    suspend fun getSongs(
        @Query("search") search: String? = null,
        @Query("album_id") albumId: Int? = null
    ): Response<PaginatedResponse<SongDto>>

    @GET("api/songs/{id}")
    suspend fun getSongDetail(
        @Path("id") songId: String
    ): Response<SongDto>

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

    @GET("api/favorites")
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
