package com.example.echo_panda_mobile.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.echo_panda_mobile.data.model.Languages

// CompositionLocal to provide current language throughout the app
val LocalAppLanguage = staticCompositionLocalOf { "en" }
// CompositionLocal to provide current theme mode throughout the app
val LocalIsDarkTheme = staticCompositionLocalOf { true }

object EchoPandaColors {
    val BgDarkStart   = Color(0xFF0D1B2A)
    val BgDarkEnd     = Color(0xFF05070D) // Deepest black
    val BgCardDark    = Color(0xFF121A26)
    val AccentBlue    = Color(0xFF00D9FF)
    val AccentPink    = Color(0xFFFF2D55)
    
    val BgLightStart  = Color(0xFFF0F4F8)
    val BgLightEnd    = Color(0xFFE1E7EF)
    
    val TextPrimaryDark   = Color(0xFFFFFFFF)
    val TextMutedDark     = Color(0xFF8FA3B0)
    
    val ErrorRed      = Color(0xFFFF4D4D)
    val DividerDark   = Color(0xFF1E2736)
}

private val DarkColorScheme = darkColorScheme(
    primary          = EchoPandaColors.AccentBlue,
    onPrimary        = Color.White,
    background       = EchoPandaColors.BgDarkEnd,
    onBackground     = EchoPandaColors.TextPrimaryDark,
    surface          = EchoPandaColors.BgDarkEnd, // Set surface to match background
    onSurface        = EchoPandaColors.TextPrimaryDark,
    surfaceVariant   = EchoPandaColors.BgDarkEnd, // Prevent NavigationBar white/gray tint
    onSurfaceVariant = EchoPandaColors.TextMutedDark,
    error            = EchoPandaColors.ErrorRed,
    outline          = EchoPandaColors.DividerDark,
)

@Composable
fun EchoPandaTheme(
    darkTheme: Boolean = true, // Default to true for music app aesthetic
    language: String = "en",
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // Forced dark theme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    CompositionLocalProvider(
        LocalIsDarkTheme provides true,
        LocalAppLanguage provides language
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
