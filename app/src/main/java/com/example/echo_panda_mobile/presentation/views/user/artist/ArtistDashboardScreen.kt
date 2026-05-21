package com.example.echo_panda_mobile.presentation.views.user.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.echo_panda_mobile.data.repository.AuthRepository
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color Palette for Artist Dashboard
val DarkBg = Color(0xFF0A0E16)
val CardBg = Color(0xFF1A1F2E)
val AccentGradient1 = Color(0xFFFF6B35)
val AccentGradient2 = Color(0xFFF7931E)
val StatColor = Color(0xFF1CC7D0)
val TextLight = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF9DA3AA)

@Composable
fun ArtistDashboardScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scrollState = rememberScrollState()
    var currentUser by remember { mutableStateOf<com.example.echo_panda_mobile.data.model.User?>(null) }
    val welcomeName = currentUser?.name?.takeIf { it.isNotBlank() } ?: "Artist"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DarkBg, Color(0xFF0F1419)),
                    startY = 0f,
                    endY = 1200f
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
            // Header
            HeaderSection(
                welcomeName = welcomeName,
                onSettingsClick = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Revenue Card (Primary Stat)
            RevenueCard()

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Grid (3 columns)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Streams",
                    value = "2.5M",
                    subtitle = "+12% this month",
                    icon = Icons.Filled.TrendingUp,
                    color = StatColor
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Listeners",
                    value = "185K",
                    subtitle = "+8% this month",
                    icon = Icons.Filled.Person,
                    color = Color(0xFF4ECDC4)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Songs",
                    value = "24",
                    subtitle = "Published",
                    icon = Icons.Filled.MusicNote,
                    color = Color(0xFFA78BFA)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Top Track Section
            TopTrackSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Activity
            RecentActivitySection()

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            ActionButtonsSection(
                onProfileClick = onNavigateToProfile,
                onSettingsClick = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(100.dp)) // Added space for bottom nav
        }

        // Bottom Navigation Bar
        ArtistBottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = {},
            onProfileClick = onNavigateToProfile,
            onSettingsClick = onNavigateToSettings
        )
    }
}

@Composable
private fun HeaderSection(
    welcomeName: String,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome back, $welcomeName!",
                color = TextLight,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Artist Dashboard",
                color = TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(44.dp)
                .background(CardBg, RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = StatColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun RevenueCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(160.dp),
        color = CardBg,
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(AccentGradient1, AccentGradient2),
                        start = androidx.compose.ui.geometry.Offset.Zero,
                        end = androidx.compose.ui.geometry.Offset(1000f, 500f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Monthly Revenue",
                    color = TextLight.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$12,450",
                    color = TextLight,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropUp,
                        contentDescription = "Growth",
                        tint = TextLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "+18% from last month",
                        color = TextLight.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = StatColor
) {
    Surface(
        modifier = modifier.height(140.dp),
        color = CardBg,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = TextLight,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = color.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TopTrackSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Top Track",
            color = TextLight,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            color = CardBg,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = StatColor.copy(alpha = 0.3f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = "Track",
                            tint = StatColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "Summer Nights",
                        color = TextLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "456.2K streams",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropUp,
                            contentDescription = "Trending",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "3rd most streamed",
                            color = Color(0xFF4CAF50),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentActivitySection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Recent Activity",
            color = TextLight,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        val activities = listOf(
            "102K new listeners this week",
            "8.5K saves on 'Summer Nights'",
            "Your playlist trending in 5 countries",
            "New fan reached 1K followers"
        )

        activities.forEach { activity ->
            ActivityItem(activity)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ActivityItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.TrendingUp,
            contentDescription = "Activity",
            tint = StatColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            color = TextLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun ActionButtonsSection(
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onProfileClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StatColor)
        ) {
            Text(text = "View Profile", color = DarkBg, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onSettingsClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, StatColor)
        ) {
            Text(text = "Settings", color = StatColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ArtistBottomNavigationBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        color = CardBg,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationItem(
                icon = Icons.Filled.Home,
                label = "Home",
                isSelected = true,
                onClick = onHomeClick
            )
            NavigationItem(
                icon = Icons.Filled.MusicNote,
                label = "Tracks",
                isSelected = false,
                onClick = {}
            )
            NavigationItem(
                icon = Icons.Filled.Person,
                label = "Profile",
                isSelected = false,
                onClick = onProfileClick
            )
            NavigationItem(
                icon = Icons.Filled.Settings,
                label = "Settings",
                isSelected = false,
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) StatColor else TextMuted,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = if (isSelected) StatColor else TextMuted,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}