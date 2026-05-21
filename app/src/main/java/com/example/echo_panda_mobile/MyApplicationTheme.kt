package com.example.echo_panda_mobile

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ─── Common Brand Colors ──────────────────────────────────────────────────────
val LightBgPrimary = Color(0xFFFAFAFA)
val LightBgCard = Color(0xFFFFFFFF)
val LightBgInput = Color(0xFFF5F5F5)
val LightTextPrimary = Color(0xFF1F1F1F)
val LightTextMuted = Color(0xFF666666)
val LightDivider = Color(0xFFE0E0E0)

// ─── Dark Theme Colors ────────────────────────────────────────────────────────
val DarkBgPrimary = Color(0xFF0A0A0F)
val DarkBgCard = Color(0xFF13131A)
val DarkBgInput = Color(0xFF1E1E2A)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextMuted = Color(0xFF8A8A9A)
val DarkDivider = Color(0xFF2A2A3A)

// ─── Brand Colors ─────────────────────────────────────────────────────────────
val AccentGreen = Color(0xFF1DB954)
val AccentBlue = Color(0xFF00D9FF)
val AccentOrange = Color(0xFFFF8A3D)
val ErrorRed = Color(0xFFFF4D4D)

// ─── Material3 Dark Colour Scheme ─────────────────────────────────────────────
private val EchoPandaDarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Color.White,
    secondary = AccentBlue,
    onSecondary = Color.White,
    background = Color(0xFF05070D),
    onBackground = Color(0xFFF5F7FA),
    surface = Color(0xFF101B2A),
    onSurface = Color(0xFFEFF3F8),
    surfaceVariant = Color(0xFF17253C),
    onSurfaceVariant = Color(0xFFB8C1D6),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF2E405C),
)

// ─── Material3 Light Colour Scheme ────────────────────────────────────────────
private val EchoPandaLightColorScheme = lightColorScheme(
    primary = AccentGreen,
    onPrimary = Color.White,
    secondary = AccentBlue,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF6B7280),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFFCBD5E1),
)

// ─── Typography and Shapes ───────────────────────────────────────────────────
private val EchoPandaTypography = Typography(
    titleLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.8.sp
    )
)

private val EchoPandaShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

// ─── App Theme ────────────────────────────────────────────────────────────────
@Composable
fun MyApplicationTheme(
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val colorScheme = if (isDarkMode) EchoPandaDarkColorScheme else EchoPandaLightColorScheme
    val statusBarColor = if (isDarkMode) DarkBgPrimary else LightBgPrimary
    val isLightStatusBars = !isDarkMode

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = statusBarColor.toArgb()
            WindowCompat
                .getInsetsController(window, view)
                .isAppearanceLightStatusBars = isLightStatusBars
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EchoPandaTypography,
        shapes = EchoPandaShapes,
        content = content
    )
}
