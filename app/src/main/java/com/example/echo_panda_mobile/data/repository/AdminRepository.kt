package com.example.echo_panda_mobile.data.repository

import com.example.echo_panda_mobile.data.remote.AdminApiService
import com.example.echo_panda_mobile.data.remote.CreateAdminArtistRequest
import com.example.echo_panda_mobile.data.remote.CreateAdminArtistResponse
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import retrofit2.HttpException

sealed class AdminResult<out T> {
    data class Success<T>(val data: T) : AdminResult<T>()
    data class Error(val message: String) : AdminResult<Nothing>()
}

class AdminRepository(private val tokenStorage: TokenStorage) {
    private val api: AdminApiService
        get() = RetrofitClient.getAdminService(tokenStorage)

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

    private fun parseHttpError(e: HttpException): String = when (e.code()) {
        404 -> "Artist creation is not available on the server yet. Deploy the latest API to production."
        403 -> "You do not have permission to create artists."
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
}
