package com.example.echo_panda_mobile.data.debug

import com.example.echo_panda_mobile.BuildConfig
import com.example.echo_panda_mobile.data.repository.TokenStorage
import okhttp3.Response

/** Filter Logcat with tag: AdminAuthDebug */
object AdminAuthDebug {

    const val TAG = "AdminAuthDebug"

    enum class RouteKind(val label: String) {
        API_SANCTUM("API (expects Bearer Sanctum token)"),
        WEB_SESSION("WEB (expects browser session cookie)"),
        PUBLIC_AUTH("PUBLIC auth endpoint"),
        EXTERNAL("external host"),
        UNKNOWN("unknown route type")
    }

    fun routeKind(url: String): RouteKind {
        val lower = url.lowercase()
        return when {
            !lower.contains("echopanda.me") -> RouteKind.EXTERNAL
            lower.contains("/api/login") ||
                lower.contains("/api/register") ||
                lower.contains("/api/firebase/session") -> RouteKind.PUBLIC_AUTH
            lower.contains("/api/mb/admin/") || lower.contains("/api/") -> RouteKind.API_SANCTUM
            lower.contains("/admin/") -> RouteKind.WEB_SESSION
            else -> RouteKind.UNKNOWN
        }
    }

    fun tokenPreview(token: String?): String {
        if (token.isNullOrBlank()) return "<missing>"
        return if (token.length <= 14) {
            "*** (${token.length} chars)"
        } else {
            "${token.take(8)}...${token.takeLast(4)} (${token.length} chars)"
        }
    }

    fun log(section: String, message: String) {
        android.util.Log.i(TAG, "[$section] $message")
    }

    fun logSession(section: String, tokenStorage: TokenStorage) {
        log(section, "API base URL: ${BuildConfig.API_BASE_URL}")
        log(section, "Stored role: ${tokenStorage.getRole() ?: "<missing>"}")
        log(section, "Stored email: ${tokenStorage.getEmail() ?: "<missing>"}")
        log(section, "Stored userId: ${tokenStorage.getUserId()}")
        log(section, "Stored token: ${tokenPreview(tokenStorage.getToken())}")
    }

    fun logIncomingResponse(url: String, response: Response, bodyPreview: String? = null) {
        val kind = routeKind(url)
        log("HTTP-IN", "${response.code} ${response.message} <- $url")
        log("HTTP-IN", "  routeKind=${kind.label}")
        bodyPreview?.takeIf { it.isNotBlank() }?.let {
            log("HTTP-IN", "  body=${it.take(300)}")
        }
        if (response.code == 401) {
            log(
                "HTTP-IN",
                "  DIAGNOSIS hint: 401 on ${kind.name} route. " +
                    when (kind) {
                        RouteKind.WEB_SESSION ->
                            "Wrong auth mode (Bearer sent to web/session route)."
                        RouteKind.API_SANCTUM ->
                            "Invalid/expired Sanctum token or backend not deployed."
                        else -> "Check login path and stored token."
                    }
            )
        }
    }
}
