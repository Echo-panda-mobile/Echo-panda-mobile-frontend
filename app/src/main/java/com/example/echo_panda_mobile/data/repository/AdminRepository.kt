package com.example.echo_panda_mobile.data.repository

import com.example.echo_panda_mobile.data.remote.AdminApiService
import com.example.echo_panda_mobile.data.remote.AdminModerationReportRequest
import com.example.echo_panda_mobile.data.remote.CreateAdminArtistRequest
import com.example.echo_panda_mobile.data.remote.CreateAdminArtistResponse
import com.example.echo_panda_mobile.data.remote.CreateGenreRequest
import com.example.echo_panda_mobile.data.remote.CreateTagRequest
import com.example.echo_panda_mobile.data.remote.GenreData
import com.example.echo_panda_mobile.data.remote.TagData
import com.example.echo_panda_mobile.data.remote.BackendUser
import com.example.echo_panda_mobile.data.remote.AuthApiService
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.remote.AnalyticsPoint
import com.example.echo_panda_mobile.data.remote.AlbumDto
import retrofit2.HttpException

sealed class AdminResult<out T> {
    data class Success<T>(val data: T) : AdminResult<T>()
    data class Error(val message: String) : AdminResult<Nothing>()
}

enum class AdminModerationAction {
    APPROVE,
    HIDE,
    REPORT
}

class AdminRepository(private val tokenStorage: TokenStorage) {
    private val api: AdminApiService
        get() = RetrofitClient.getAdminService(tokenStorage)

    private val authApi: AuthApiService
        get() = RetrofitClient.getAuthService(tokenStorage)

    data class AdminDirectoryData(
        val normalUsers: List<BackendUser>,
        val artistUsers: List<BackendUser>,
        val adminUsers: List<BackendUser>
    )

    suspend fun createArtist(
        name: String,
        email: String,
        password: String,
        artistType: String?,
        gender: String?
    ): AdminResult<CreateAdminArtistResponse> {
        if (name.isBlank()) return AdminResult.Error("Name is required.")
        if (email.isBlank()) return AdminResult.Error("Email is required.")
        if (password.length < 8) return AdminResult.Error("Password must be at least 8 characters.")

        return try {
            val response = api.createArtist(
                CreateAdminArtistRequest(
                    name = name.trim(),
                    email = email.trim(),
                    password = password,
                    artistType = artistType?.lowercase(),
                    gender = gender?.lowercase(),
                    verificationStatus = "pending"
                )
            )
            val role = response.user.role.trim().lowercase()
            if (role != "artist" && role != "publicer") {
                return AdminResult.Error(
                    "Server saved this email as \"$role\", not artist. " +
                        "Deploy the latest API or remove the old account and try again."
                )
            }
            val firebaseMsg = response.firebaseMessage.orEmpty()
            if (firebaseMsg.contains("failed", ignoreCase = true) ||
                response.firebaseProvisioned == false
            ) {
                return AdminResult.Error(firebaseMsg.ifBlank { "Firebase account was not created." })
            }
            AdminResult.Success(response)
        } catch (e: HttpException) {
            AdminResult.Error(parseHttpError(e))
        } catch (e: Exception) {
            AdminResult.Error(e.message ?: "Could not create artist.")
        }
    }

    suspend fun getAdminDirectory(): AdminResult<AdminDirectoryData> {
        return try {
            val response = authApi.usersByRole()
            AdminResult.Success(
                AdminDirectoryData(
                    normalUsers = response.normalUsers,
                    artistUsers = response.artistUsers,
                    adminUsers = response.adminUsers
                )
            )
        } catch (e: HttpException) {
            AdminResult.Error(parseHttpError(e))
        } catch (e: Exception) {
            AdminResult.Error(e.message ?: "Could not load admin users.")
        }
    }

    suspend fun findUserByIdAndRole(userId: String, role: String): AdminResult<BackendUser?> {
        return when (val result = getAdminDirectory()) {
            is AdminResult.Success -> {
                val normalizedRole = role.trim().lowercase()
                val user = when (normalizedRole) {
                    "artist" -> result.data.artistUsers.find { it.id.toString() == userId }
                    "admin" -> result.data.adminUsers.find { it.id.toString() == userId }
                    else -> result.data.normalUsers.find { it.id.toString() == userId }
                }
                AdminResult.Success(user)
            }
            is AdminResult.Error -> result
        }
    }

    suspend fun approveSong(songId: String): AdminResult<Boolean> = moderateSong(songId, AdminModerationAction.APPROVE)

    suspend fun hideSong(songId: String): AdminResult<Boolean> = moderateSong(songId, AdminModerationAction.HIDE)

    suspend fun reportSong(songId: String, reason: String? = null): AdminResult<Boolean> =
        moderateSong(songId, AdminModerationAction.REPORT, reason)

    suspend fun approveAlbum(albumId: String): AdminResult<Boolean> = moderateAlbum(albumId, AdminModerationAction.APPROVE)

    suspend fun hideAlbum(albumId: String): AdminResult<Boolean> = moderateAlbum(albumId, AdminModerationAction.HIDE)

    suspend fun reportAlbum(albumId: String, reason: String? = null): AdminResult<Boolean> =
        moderateAlbum(albumId, AdminModerationAction.REPORT, reason)

    private suspend fun moderateSong(
        songId: String,
        action: AdminModerationAction,
        reason: String? = null
    ): AdminResult<Boolean> {
        return try {
            val response = when (action) {
                AdminModerationAction.APPROVE -> api.approveSong(songId)
                AdminModerationAction.HIDE -> api.hideSong(songId)
                AdminModerationAction.REPORT -> api.reportSong(songId, AdminModerationReportRequest(reason))
            }
            if (response.isSuccessful) {
                AdminResult.Success(true)
            } else {
                AdminResult.Error("Song moderation failed (${response.code()}).")
            }
        } catch (e: HttpException) {
            AdminResult.Error(parseHttpError(e))
        } catch (e: Exception) {
            AdminResult.Error(e.message ?: "Could not update song.")
        }
    }

    private suspend fun moderateAlbum(
        albumId: String,
        action: AdminModerationAction,
        reason: String? = null
    ): AdminResult<Boolean> {
        return try {
            val response = when (action) {
                AdminModerationAction.APPROVE -> api.approveAlbum(albumId)
                AdminModerationAction.HIDE -> api.hideAlbum(albumId)
                AdminModerationAction.REPORT -> api.reportAlbum(albumId, AdminModerationReportRequest(reason))
            }
            if (response.isSuccessful) {
                AdminResult.Success(true)
            } else {
                AdminResult.Error("Album moderation failed (${response.code()}).")
            }
        } catch (e: HttpException) {
            AdminResult.Error(parseHttpError(e))
        } catch (e: Exception) {
            AdminResult.Error(e.message ?: "Could not update album.")
        }
    }

    private fun parseHttpError(e: HttpException): String = when (e.code()) {
        404 -> "API endpoint not found. Please ensure the server is up to date."
        403 -> "You do not have permission to perform this action."
        401 -> "Session expired. Please log in again as admin."
        else -> {
            val body = e.response()?.errorBody()?.string().orEmpty()
            if (body.contains("email", ignoreCase = true) &&
                (body.contains("taken", ignoreCase = true) || body.contains("unique", ignoreCase = true))
            ) {
                "This email is already registered."
            } else {
                body.takeIf { it.isNotBlank() } ?: "Request failed (${e.code()})."
            }
        }
    }

    suspend fun getDashboard() = try {
        val directoryResult = getAdminDirectory()
        val tagsResult = getTags()
        val genresResult = getGenres()

        if (directoryResult is AdminResult.Success) {
            val allUsers = directoryResult.data
            val normalUsers = allUsers.normalUsers
            val artistUsers = allUsers.artistUsers
            val adminUsers = allUsers.adminUsers

            val tagsCount = if (tagsResult is AdminResult.Success) tagsResult.data.size else 0
            val genresCount = if (genresResult is AdminResult.Success) genresResult.data.size else 0

            // Compose dashboard stats from directory data and content counts
            com.example.echo_panda_mobile.data.remote.DashboardStats(
                totalUsers = normalUsers.size,
                activeArtists = artistUsers.size,
                totalAdmins = adminUsers.size,
                totalTags = tagsCount,
                totalGenres = genresCount,
                flaggedContent = 0, // Not exposed in current API
                pendingReports = 0  // Not exposed in current API
            )
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    suspend fun getAnalytics(): AdminResult<List<AnalyticsPoint>> = try {
        AdminResult.Success(api.getAnalytics().data)
    } catch (e: HttpException) {
        AdminResult.Error(parseHttpError(e))
    } catch (e: Exception) {
        AdminResult.Error(e.message ?: "Could not load analytics.")
    }

    suspend fun getTags(): AdminResult<List<TagData>> = try {
        AdminResult.Success(api.getTags().data)
    } catch (e: HttpException) {
        AdminResult.Error(parseHttpError(e))
    } catch (e: Exception) {
        AdminResult.Error(e.message ?: "Could not load tags.")
    }

    suspend fun getAlbumsByTag(tagId: Int): AdminResult<List<AlbumDto>> = try {
        AdminResult.Success(api.getAlbums(tagId = tagId).data)
    } catch (e: HttpException) {
        AdminResult.Error(parseHttpError(e))
    } catch (e: Exception) {
        AdminResult.Error(e.message ?: "Could not load albums.")
    }

    suspend fun getAlbumsByGenre(genreId: Int): AdminResult<List<AlbumDto>> = try {
        AdminResult.Success(api.getAlbums(genreId = genreId).data)
    } catch (e: HttpException) {
        AdminResult.Error(parseHttpError(e))
    } catch (e: Exception) {
        AdminResult.Error(e.message ?: "Could not load albums.")
    }

    suspend fun createTag(name: String): AdminResult<TagData> {
        return try {
            val response = api.createTag(CreateTagRequest(name))
            AdminResult.Success(response)
        } catch (e: HttpException) {
            AdminResult.Error(parseHttpError(e))
        } catch (e: Exception) {
            AdminResult.Error(e.message ?: "Could not create tag.")
        }
    }

    suspend fun updateTag(id: Int, name: String): AdminResult<TagData> {
        return try {
            val response = api.updateTag(id, CreateTagRequest(name))
            AdminResult.Success(response)
        } catch (e: HttpException) {
            AdminResult.Error(parseHttpError(e))
        } catch (e: Exception) {
            AdminResult.Error(e.message ?: "Could not update tag.")
        }
    }

    suspend fun deleteTag(id: Int): AdminResult<Boolean> {
        return try {
            val response = api.deleteTag(id)
            if (response.isSuccessful) {
                AdminResult.Success(true)
            } else {
                AdminResult.Error("Could not delete tag.")
            }
        } catch (e: HttpException) {
            AdminResult.Error(parseHttpError(e))
        } catch (e: Exception) {
            AdminResult.Error(e.message ?: "Could not delete tag.")
        }
    }

    suspend fun updateAlbum(albumId: Int, body: Map<String, Any>): AdminResult<AlbumDto> {
        return try {
            val response = api.updateAlbum(albumId, body)
            AdminResult.Success(response)
        } catch (e: HttpException) {
            AdminResult.Error(parseHttpError(e))
        } catch (e: Exception) {
            AdminResult.Error(e.message ?: "Could not update album.")
        }
    }

    suspend fun removeAlbumFromTag(albumId: Int, tagId: Int): AdminResult<Boolean> {
        return try {
            // Best-effort payload; backend may expect a different field name.
            val response = updateAlbum(albumId, mapOf("remove_tag_id" to tagId))
            when (response) {
                is AdminResult.Success -> AdminResult.Success(true)
                is AdminResult.Error -> AdminResult.Error(response.message)
            }
        } catch (e: Exception) {
            AdminResult.Error(e.message ?: "Could not remove tag from album.")
        }
    }

    suspend fun removeAlbumFromGenre(albumId: Int, genreId: Int): AdminResult<Boolean> {
        return try {
            val response = updateAlbum(albumId, mapOf("remove_genre_id" to genreId))
            when (response) {
                is AdminResult.Success -> AdminResult.Success(true)
                is AdminResult.Error -> AdminResult.Error(response.message)
            }
        } catch (e: Exception) {
            AdminResult.Error(e.message ?: "Could not remove category from album.")
        }
    }

    suspend fun getGenres(): AdminResult<List<GenreData>> = try {
        AdminResult.Success(api.getGenres().data)
    } catch (e: HttpException) {
        AdminResult.Error(parseHttpError(e))
    } catch (e: Exception) {
        AdminResult.Error(e.message ?: "Could not load categories.")
    }

    suspend fun createGenre(name: String): AdminResult<GenreData> {
        return try {
            val response = api.createGenre(CreateGenreRequest(name))
            AdminResult.Success(response)
        } catch (e: HttpException) {
            AdminResult.Error(parseHttpError(e))
        } catch (e: Exception) {
            AdminResult.Error(e.message ?: "Could not create category.")
        }
    }

    suspend fun updateGenre(id: Int, name: String): AdminResult<GenreData> {
        return try {
            val response = api.updateGenre(id, CreateGenreRequest(name))
            AdminResult.Success(response)
        } catch (e: HttpException) {
            AdminResult.Error(parseHttpError(e))
        } catch (e: Exception) {
            AdminResult.Error(e.message ?: "Could not update category.")
        }
    }

    suspend fun deleteGenre(id: Int): AdminResult<Boolean> {
        return try {
            val response = api.deleteGenre(id)
            if (response.isSuccessful) {
                AdminResult.Success(true)
            } else {
                AdminResult.Error("Could not delete category.")
            }
        } catch (e: HttpException) {
            AdminResult.Error(parseHttpError(e))
        } catch (e: Exception) {
            AdminResult.Error(e.message ?: "Could not delete category.")
        }
    }
}
