package com.example.echo_panda_mobile.data.model

/**
 * App settings stored in local persistence.
 */
data class AppSettings(
    val isDarkMode: Boolean = true,
    val language: String = Languages.ENGLISH,
    val notificationsEnabled: Boolean = true,
    val privateAccount: Boolean = false,
    val explicitContentEnabled: Boolean = true
)

/**
 * Languages supported by the app.
 */
object Languages {
    const val ENGLISH = "en"
    const val KHMER = "km"
}
