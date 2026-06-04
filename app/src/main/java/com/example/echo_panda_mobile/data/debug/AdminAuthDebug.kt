package com.example.echo_panda_mobile.data.debug

import com.example.echo_panda_mobile.BuildConfig
import com.example.echo_panda_mobile.data.repository.TokenStorage
import okhttp3.Response

/**
 * Filter Logcat with tag: AdminAuthDebug
 *
 * Example:
 *   adb logcat -s AdminAuthDebug OkHttpClient
 */
object AdminAuthDebug {

    const val TAG = "AdminAuthDebug"

    enum class RouteKind(val label: String) {
        API_SANCTUM("API (expects Bearer Sanctum token)"),
        WEB_SESSION("WEB (expects browser session cookie)"),
        PUBLIC_AUTH("PUBLIC auth endpoint"),
        EXTERNAL("external host"),
        UNKNOWN("unknown route type")
    }

    enum class LoginPath(val label: String) {
        FIRESTORE_ADMIN_THEN_API_LOGIN("Firestore admins doc -> POST /api/login"),
        FIRESTORE_ARTIST_THEN_API_LOGIN("Firestore artists doc -> POST /api/login"),
        FIREBASE_THEN_API_SESSION("Firebase Auth -> POST /api/firebase/session"),
        GOOGLE_FIREBASE_SESSION("Google -> Firebase -> POST /api/firebase/session"),
        SESSION_RESTORE("Firebase session restore -> POST /api/firebase/session"),
        UNKNOWN("unknown login path")
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

    fun logDivider(section: String) {
        android.util.Log.i(TAG, "[$section] ${"=".repeat(60)}")
    }

    fun logSession(section: String, tokenStorage: TokenStorage) {
        log(section, "API base URL: ${BuildConfig.API_BASE_URL}")
        log(section, "Stored role: ${tokenStorage.getRole() ?: "<missing>"}")
        log(section, "Stored email: ${tokenStorage.getEmail() ?: "<missing>"}")
        log(section, "Stored userId: ${tokenStorage.getUserId()}")
        log(section, "Stored token: ${tokenPreview(tokenStorage.getToken())}")
    }

    fun logLoginStart(email: String) {
        logDivider("LOGIN")
        log("LOGIN", "Starting login for: $email")
    }

    fun logLoginPath(path: LoginPath, detail: String? = null) {
        log("LOGIN", "Path selected: ${path.label}")
        detail?.let { log("LOGIN", "Detail: $it") }
    }

    fun logLoginBackendResult(role: String, userId: Int, token: String, redirectTo: String?) {
        log("LOGIN", "Backend accepted login")
        log("LOGIN", "  role=$role userId=$userId redirectTo=${redirectTo ?: "<none>"}")
        log("LOGIN", "  token=${tokenPreview(token)}")
        logDivider("LOGIN")
    }

    fun logLoginError(message: String) {
        log("LOGIN", "FAILED: $message")
        logDivider("LOGIN")
    }

    fun logOutgoingRequest(
        url: String,
        method: String,
        token: String?,
        hasXsrfHeader: Boolean,
        cookieNames: List<String>
    ) {
        val kind = routeKind(url)
        log("HTTP-OUT", "$method $url")
        log("HTTP-OUT", "  routeKind=${kind.label}")
        log("HTTP-OUT", "  Authorization=${if (token.isNullOrBlank()) "<not sent>" else tokenPreview(token)}")
        log("HTTP-OUT", "  X-XSRF-TOKEN=${if (hasXsrfHeader) "present" else "missing"}")
        log("HTTP-OUT", "  cookies=${if (cookieNames.isEmpty()) "<none>" else cookieNames.joinToString()}")
        if (kind == RouteKind.WEB_SESSION && !token.isNullOrBlank()) {
            log(
                "HTTP-OUT",
                "  NOTE: WEB route + Bearer token often returns 401 (session auth required, not Sanctum)."
            )
        }
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
                            "Likely wrong auth mode (Bearer sent to web/session route)."
                        RouteKind.API_SANCTUM ->
                            "Likely invalid/expired Sanctum token or user not linked in backend."
                        else -> "Check login path and stored token."
                    }
            )
        }
        if (response.code == 403) {
            log("HTTP-IN", "  DIAGNOSIS hint: authenticated but role may not include admin.")
        }
    }

    fun logAdminOperation(operation: String, tokenStorage: TokenStorage) {
        logDivider("ADMIN-$operation")
        logSession("ADMIN-$operation", tokenStorage)
    }

    fun logAdminResult(operation: String, success: Boolean, message: String? = null) {
        log("ADMIN-$operation", if (success) "SUCCESS" else "FAILED${message?.let { ": $it" } ?: ""}")
        logDivider("ADMIN-$operation")
    }
}
