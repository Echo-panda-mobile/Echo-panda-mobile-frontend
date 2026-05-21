package com.example.echo_panda_mobile.presentation.views.user.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// We wrap colors in an object to solve the "Overload resolution ambiguity"
private object ArtistSettingsColors {
    val DarkBg = Color(0xFF0A0E16)
    val CardBg = Color(0xFF1A1F2E)
    val StatColor = Color(0xFF1CC7D0)
    val TextLight = Color(0xFFFFFFFF)
    val TextMuted = Color(0xFF9DA3AA)
}

@Composable
fun ArtistSettingsScreen(
    onBack: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArtistSettingsColors.DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(ArtistSettingsColors.CardBg, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ArtistSettingsColors.StatColor
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Settings",
                    color = ArtistSettingsColors.TextLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Artist Account Section
            SettingsSectionHeaderArtist("Artist Account")
            SettingsItemSwitchArtist(
                icon = Icons.Filled.VerifiedUser,
                title = "Public Profile",
                subtitle = "Make your profile visible",
                isChecked = true,
                onToggle = {}
            )
            SettingsItemSwitchArtist(
                icon = Icons.Filled.Notifications,
                title = "Artist Notifications",
                subtitle = "Receive important updates",
                isChecked = true,
                onToggle = {}
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Revenue Settings
            SettingsSectionHeaderArtist("Revenue")
            SettingsItemArrowArtist(
                icon = Icons.Filled.Payments,
                title = "Payment Method",
                subtitle = "Manage payout details"
            )
            SettingsItemArrowArtist(
                icon = Icons.Filled.Receipt,
                title = "Tax Information",
                subtitle = "Update tax details"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Distribution
            SettingsSectionHeaderArtist("Distribution")
            SettingsItemArrowArtist(
                icon = Icons.Filled.CloudUpload,
                title = "Upload Settings",
                subtitle = "Configure upload preferences"
            )
            SettingsItemArrowArtist(
                icon = Icons.Filled.Storage,
                title = "Catalog Management",
                subtitle = "Manage your music catalog"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Collaboration
            SettingsSectionHeaderArtist("Collaboration")
            SettingsItemArrowArtist(
                icon = Icons.Filled.People,
                title = "Featured Artists",
                subtitle = "Manage collaborators"
            )
            SettingsItemArrowArtist(
                icon = Icons.Filled.Group,
                title = "Team Members",
                subtitle = "Manage access"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Analytics
            SettingsSectionHeaderArtist("Analytics")
            SettingsItemSwitchArtist(
                icon = Icons.Filled.Analytics,
                title = "Detailed Analytics",
                subtitle = "Track comprehensive data",
                isChecked = true,
                onToggle = {}
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy & Security
            SettingsSectionHeaderArtist("Privacy & Security")
            SettingsItemArrowArtist(
                icon = Icons.Filled.Lock,
                title = "Two-Factor Authentication",
                subtitle = "Secure your account"
            )
            SettingsItemArrowArtist(
                icon = Icons.Filled.Security,
                title = "Privacy Policy",
                subtitle = "View our policy"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Support
            SettingsSectionHeaderArtist("Support")
            SettingsItemArrowArtist(
                icon = Icons.Filled.Help,
                title = "Help & Support",
                subtitle = "Contact support team"
            )
            SettingsItemArrowArtist(
                icon = Icons.Filled.Info,
                title = "App Version",
                subtitle = "Version 1.0.0"
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeaderArtist(
    title: String
) {
    Text(
        text = title,
        color = ArtistSettingsColors.StatColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsItemSwitchArtist(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        color = ArtistSettingsColors.CardBg,
        shape = RoundedCornerShape(12.dp)
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
                tint = ArtistSettingsColors.StatColor,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = ArtistSettingsColors.TextLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = ArtistSettingsColors.TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ArtistSettingsColors.StatColor,
                    uncheckedThumbColor = ArtistSettingsColors.TextMuted,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}

@Composable
private fun SettingsItemArrowArtist(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        color = ArtistSettingsColors.CardBg,
        shape = RoundedCornerShape(12.dp)
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
                tint = ArtistSettingsColors.StatColor,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = ArtistSettingsColors.TextLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = ArtistSettingsColors.TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Navigate",
                tint = ArtistSettingsColors.TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}