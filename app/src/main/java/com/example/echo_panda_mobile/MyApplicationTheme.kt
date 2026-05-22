package com.example.echo_panda_mobile

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── EchoPanda Colour Tokens ──────────────────────────────────────────────────
val BgDark        = Color(0xFF0A0A0F)   // main background
val BgCard        = Color(0xFF13131A)   // card / surface
val BgInput       = Color(0xFF1E1E2A)   // text field background
val AccentGreen   = Color(0xFF1DB954)   // primary action / brand green
val AccentGreenDim = Color(0xFF17A349)  // darker green for gradients
val TextPrimary   = Color(0xFFFFFFFF)
val TextMuted     = Color(0xFF8A8A9A)
val ErrorRed      = Color(0xFFFF4D4D)
val DividerColor  = Color(0xFF2A2A3A)

// ─── Material3 Dark Colour Scheme ─────────────────────────────────────────────
private val EchoPandaColorScheme = darkColorScheme(
    primary          = AccentGreen,
    onPrimary        = Color.White,
    primaryContainer = AccentGreenDim,

    background       = BgDark,
    onBackground     = TextPrimary,

    surface          = BgCard,
    onSurface        = TextPrimary,
    surfaceVariant   = BgInput,
    onSurfaceVariant = TextMuted,

    error            = ErrorRed,
    onError          = Color.White,

    outline          = DividerColor,
)

// ─── App Theme ────────────────────────────────────────────────────────────────
@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Make status bar transparent so bg shows through
            window.statusBarColor = BgDark.toArgb()
            WindowCompat
                .getInsetsController(window, view)
                .isAppearanceLightStatusBars = false  // white icons on dark bg
        }
    }

    MaterialTheme(
        colorScheme = EchoPandaColorScheme,
        content     = content
    )
}