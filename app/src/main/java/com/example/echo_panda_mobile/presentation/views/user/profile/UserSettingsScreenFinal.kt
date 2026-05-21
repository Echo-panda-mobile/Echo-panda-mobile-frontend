package com.example.echo_panda_mobile.presentation.views.user.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo_panda_mobile.data.model.AppSettings
import com.example.echo_panda_mobile.data.model.AppStrings
import com.example.echo_panda_mobile.data.model.Languages
import com.example.echo_panda_mobile.data.repository.SettingsRepository
import kotlinx.coroutines.launch

private object SettingsColors {
    val DarkBgStart = Color(0xFF0D1B2A)
    val DarkBgEnd = Color(0xFF0A0F14)
    val DarkCardBg = Color(0xFF1A252F)
    val DarkAccent = Color(0xFF00D9FF)
    val DarkTextLight = Color(0xFFFFFFFF)
    val DarkTextMuted = Color(0xFF8FA3B0)

    val LightBgStart = Color(0xFFF5F5F5)
    val LightBgEnd = Color(0xFFEAEAEA)
    val LightCardBg = Color(0xFFFFFFFF)
    val LightAccent = Color(0xFF1DB954)
    val LightTextLight = Color(0xFF1F1F1F)
    val LightTextMuted = Color(0xFF666666)

    fun bgStart(isDark: Boolean) = if (isDark) DarkBgStart else LightBgStart
    fun bgEnd(isDark: Boolean) = if (isDark) DarkBgEnd else LightBgEnd
    fun cardBg(isDark: Boolean) = if (isDark) DarkCardBg else LightCardBg
    fun accent(isDark: Boolean) = if (isDark) DarkAccent else LightAccent
    fun textLight(isDark: Boolean) = if (isDark) DarkTextLight else LightTextLight
    fun textMuted(isDark: Boolean) = if (isDark) DarkTextMuted else LightTextMuted
}

@Composable
fun UserSettingsScreen(
    onBack: () -> Unit,
    isDarkMode: Boolean = true,
    language: String = Languages.ENGLISH
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val settingsRepo = remember(context) { SettingsRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    val persistedSettings by settingsRepo.settingsFlow.collectAsState(
        initial = AppSettings(isDarkMode = isDarkMode, language = language)
    )

    var uiSettings by remember { mutableStateOf(persistedSettings) }

    LaunchedEffect(persistedSettings) {
        uiSettings = persistedSettings
    }

    val currentDarkMode = uiSettings.isDarkMode
    val currentLanguage = uiSettings.language

    val bgStart by animateColorAsState(
        targetValue = SettingsColors.bgStart(currentDarkMode),
        animationSpec = tween(250),
        label = "settingsBgStart"
    )
    val bgEnd by animateColorAsState(
        targetValue = SettingsColors.bgEnd(currentDarkMode),
        animationSpec = tween(250),
        label = "settingsBgEnd"
    )
    val cardBg by animateColorAsState(
        targetValue = SettingsColors.cardBg(currentDarkMode),
        animationSpec = tween(250),
        label = "settingsCardBg"
    )
    val accentColor by animateColorAsState(
        targetValue = SettingsColors.accent(currentDarkMode),
        animationSpec = tween(250),
        label = "settingsAccent"
    )
    val textLight by animateColorAsState(
        targetValue = SettingsColors.textLight(currentDarkMode),
        animationSpec = tween(250),
        label = "settingsTextLight"
    )
    val textMuted by animateColorAsState(
        targetValue = SettingsColors.textMuted(currentDarkMode),
        animationSpec = tween(250),
        label = "settingsTextMuted"
    )

    fun saveSettings(updated: AppSettings) {
        uiSettings = updated
        coroutineScope.launch {
            settingsRepo.saveSettings(updated)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(bgStart, bgEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            SettingsHeader(
                title = AppStrings.getString("settings", currentLanguage),
                onBack = onBack,
                textLight = textLight,
                accentColor = accentColor,
                cardBg = cardBg
            )

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSectionHeader(
                title = AppStrings.getString("display", currentLanguage),
                color = accentColor
            )

            SettingsItemSwitch(
                icon = Icons.Filled.DarkMode,
                title = AppStrings.getString("dark_mode", currentLanguage),
                subtitle = if (currentDarkMode) {
                    AppStrings.getString("dark_mode_desc", currentLanguage)
                } else {
                    AppStrings.getString("light_mode_desc", currentLanguage)
                },
                isChecked = currentDarkMode,
                onToggle = { newValue ->
                    saveSettings(uiSettings.copy(isDarkMode = newValue))
                },
                cardBg = cardBg,
                accentColor = accentColor,
                textLight = textLight,
                textMuted = textMuted,
                checkedTrackColor = accentColor,
                uncheckedTrackColor = if (currentDarkMode) Color.DarkGray else Color.LightGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSectionHeader(
                title = AppStrings.getString("language", currentLanguage),
                color = accentColor
            )

            SettingsLanguageItem(
                title = AppStrings.getString("english", Languages.ENGLISH),
                isSelected = currentLanguage == Languages.ENGLISH,
                onSelect = { saveSettings(uiSettings.copy(language = Languages.ENGLISH)) },
                cardBg = cardBg,
                accentColor = accentColor,
                textLight = textLight,
                textMuted = textMuted
            )

            SettingsLanguageItem(
                title = AppStrings.getString("khmer", Languages.KHMER),
                isSelected = currentLanguage == Languages.KHMER,
                onSelect = { saveSettings(uiSettings.copy(language = Languages.KHMER)) },
                cardBg = cardBg,
                accentColor = accentColor,
                textLight = textLight,
                textMuted = textMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSectionHeader(
                title = AppStrings.getString("account", currentLanguage),
                color = accentColor
            )

            SettingsItemSwitch(
                icon = Icons.Filled.Lock,
                title = AppStrings.getString("private_account", currentLanguage),
                subtitle = AppStrings.getString("private_account_desc", currentLanguage),
                isChecked = uiSettings.privateAccount,
                onToggle = { newValue ->
                    saveSettings(uiSettings.copy(privateAccount = newValue))
                },
                cardBg = cardBg,
                accentColor = accentColor,
                textLight = textLight,
                textMuted = textMuted,
                checkedTrackColor = accentColor,
                uncheckedTrackColor = if (currentDarkMode) Color.DarkGray else Color.LightGray
            )

            SettingsItemSwitch(
                icon = Icons.Filled.Notifications,
                title = AppStrings.getString("notifications", currentLanguage),
                subtitle = AppStrings.getString("notifications_desc", currentLanguage),
                isChecked = uiSettings.notificationsEnabled,
                onToggle = { newValue ->
                    saveSettings(uiSettings.copy(notificationsEnabled = newValue))
                },
                cardBg = cardBg,
                accentColor = accentColor,
                textLight = textLight,
                textMuted = textMuted,
                checkedTrackColor = accentColor,
                uncheckedTrackColor = if (currentDarkMode) Color.DarkGray else Color.LightGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSectionHeader(
                title = AppStrings.getString("audio", currentLanguage),
                color = accentColor
            )

            SettingsItemArrow(
                icon = Icons.Filled.VolumeUp,
                title = AppStrings.getString("audio_quality", currentLanguage),
                subtitle = AppStrings.getString("audio_quality_desc", currentLanguage),
                cardBg = cardBg,
                accentColor = accentColor,
                textLight = textLight,
                textMuted = textMuted
            )

            SettingsItemSwitch(
                icon = Icons.Filled.Explicit,
                title = AppStrings.getString("explicit_content", currentLanguage),
                subtitle = AppStrings.getString("explicit_content_desc", currentLanguage),
                isChecked = uiSettings.explicitContentEnabled,
                onToggle = { newValue ->
                    saveSettings(uiSettings.copy(explicitContentEnabled = newValue))
                },
                cardBg = cardBg,
                accentColor = accentColor,
                textLight = textLight,
                textMuted = textMuted,
                checkedTrackColor = accentColor,
                uncheckedTrackColor = if (currentDarkMode) Color.DarkGray else Color.LightGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSectionHeader(
                title = AppStrings.getString("privacy_security", currentLanguage),
                color = accentColor
            )

            SettingsItemArrow(
                icon = Icons.Filled.Fingerprint,
                title = AppStrings.getString("two_factor_auth", currentLanguage),
                subtitle = AppStrings.getString("two_factor_auth_desc", currentLanguage),
                cardBg = cardBg,
                accentColor = accentColor,
                textLight = textLight,
                textMuted = textMuted
            )

            SettingsItemArrow(
                icon = Icons.Filled.Security,
                title = AppStrings.getString("privacy_policy", currentLanguage),
                subtitle = AppStrings.getString("privacy_policy_desc", currentLanguage),
                cardBg = cardBg,
                accentColor = accentColor,
                textLight = textLight,
                textMuted = textMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSectionHeader(
                title = AppStrings.getString("about", currentLanguage),
                color = accentColor
            )

            SettingsItemArrow(
                icon = Icons.Filled.Info,
                title = AppStrings.getString("version", currentLanguage),
                subtitle = AppStrings.getString("version_desc", currentLanguage),
                cardBg = cardBg,
                accentColor = accentColor,
                textLight = textLight,
                textMuted = textMuted
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsHeader(
    title: String,
    onBack: () -> Unit,
    textLight: Color,
    accentColor: Color,
    cardBg: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .width(44.dp)
                .height(44.dp)
                .background(cardBg, RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = title,
                tint = accentColor
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = textLight,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    color: Color
) {
    Text(
        text = title,
        color = color,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsItemSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    cardBg: Color,
    accentColor: Color,
    textLight: Color,
    textMuted: Color,
    checkedTrackColor: Color,
    uncheckedTrackColor: Color
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.width(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = textLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = textMuted,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = checkedTrackColor,
                    uncheckedThumbColor = textMuted,
                    uncheckedTrackColor = uncheckedTrackColor
                )
            )
        }
    }
}

@Composable
private fun SettingsLanguageItem(
    title: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    cardBg: Color,
    accentColor: Color,
    textLight: Color,
    textMuted: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onSelect),
        color = if (isSelected) accentColor.copy(alpha = 0.1f) else cardBg,
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) {
            BorderStroke(2.dp, accentColor)
        } else {
            BorderStroke(1.dp, accentColor.copy(alpha = 0.15f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = if (isSelected) accentColor else textLight,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = accentColor,
                    modifier = Modifier.width(20.dp)
                )
            } else {
                Text(
                    text = "Select",
                    color = textMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsItemArrow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    cardBg: Color,
    accentColor: Color,
    textLight: Color,
    textMuted: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        color = cardBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.width(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = textLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = textMuted,
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Navigate",
                tint = textMuted,
                modifier = Modifier.width(20.dp)
            )
        }
    }
}
