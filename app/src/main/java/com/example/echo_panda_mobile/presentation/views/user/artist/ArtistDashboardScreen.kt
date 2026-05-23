package com.example.echo_panda_mobile.presentation.views.user.artist

import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo_panda_mobile.data.model.ArtistDashboardData
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistDashboardViewModel

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
    onNavigateToSettings: () -> Unit,
    currentUser: User? = null,
    viewModel: ArtistDashboardViewModel = remember { ArtistDashboardViewModel() }
) {
    // Load dashboard data on compose
    LaunchedEffect(currentUser) {
        currentUser?.let { viewModel.loadDashboard(it) }
    }

    // Observe UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

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
        when (uiState) {
            ArtistDashboardViewModel.DashboardUiState.Loading -> {
                // Show loading skeleton
                DashboardLoadingState()
            }

            is ArtistDashboardViewModel.DashboardUiState.Success -> {
                val dashboardData =
                    (uiState as ArtistDashboardViewModel.DashboardUiState.Success).data
                DashboardContent(
                    dashboardData = dashboardData,
                    isRefreshing = isRefreshing,
                    onRefresh = { currentUser?.let { viewModel.refreshDashboard(it) } },
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToSettings = onNavigateToSettings
                )
            }

            is ArtistDashboardViewModel.DashboardUiState.Error -> {
                // Show error state with retry
                val errorMessage =
                    (uiState as ArtistDashboardViewModel.DashboardUiState.Error).message
                DashboardErrorState(
                    errorMessage = errorMessage,
                    onRetry = { currentUser?.let { viewModel.retry(it) } }
                )
            }
        }

        // Bottom Navigation Bar (always visible)
        ArtistBottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = {},
            onProfileClick = onNavigateToProfile,
            onSettingsClick = onNavigateToSettings
        )
    }
}

@Composable
private fun DashboardContent(
    dashboardData: ArtistDashboardData,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scrollState = rememberScrollState()
    val welcomeName = dashboardData.user.name.takeIf { it.isNotBlank() } ?: "Artist"

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
        RevenueCard(
            revenue = dashboardData.stats.monthlyRevenue,
            growthPercentage = dashboardData.stats.revenueGrowth
        )

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
                value = dashboardData.stats.streams,
                subtitle = "+${dashboardData.stats.streamsGrowth.toInt()}% this month",
                icon = Icons.Filled.TrendingUp,
                color = StatColor
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Listeners",
                value = dashboardData.stats.listeners,
                subtitle = "+${dashboardData.stats.listenersGrowth.toInt()}% this month",
                icon = Icons.Filled.Person,
                color = Color(0xFF4ECDC4)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Songs",
                value = dashboardData.stats.publishedSongs.toString(),
                subtitle = "Published",
                icon = Icons.Filled.MusicNote,
                color = Color(0xFFA78BFA)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Top Track Section
        TopTrackSection(
            trackTitle = dashboardData.topTrack.title,
            streams = dashboardData.topTrack.streams,
            ranking = dashboardData.topTrack.ranking
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Activity
        RecentActivitySection(activities = dashboardData.recentActivities.map { it.text })

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        ActionButtonsSection(
            onProfileClick = onNavigateToProfile,
            onSettingsClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(100.dp)) // Added space for bottom nav
    }
}

@Composable
private fun DashboardLoadingState() {
    val scrollState = rememberScrollState()
    val shimmerAlpha = remember { androidx.compose.animation.core.Animatable(0.3f) }

    LaunchedEffect(Unit) {
        shimmerAlpha.animateTo(
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header Skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(28.dp)
                        .background(CardBg.copy(alpha = shimmerAlpha.value), RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(16.dp)
                        .background(CardBg.copy(alpha = shimmerAlpha.value), RoundedCornerShape(8.dp))
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(CardBg.copy(alpha = shimmerAlpha.value), RoundedCornerShape(12.dp))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Revenue Card Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(160.dp)
                .background(CardBg.copy(alpha = shimmerAlpha.value), RoundedCornerShape(20.dp))
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Grid Skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .background(CardBg.copy(alpha = shimmerAlpha.value), RoundedCornerShape(16.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Top Track Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(160.dp)
                .background(CardBg.copy(alpha = shimmerAlpha.value), RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Activities Skeleton
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
                    .background(CardBg.copy(alpha = shimmerAlpha.value), RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun DashboardErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.TrendingUp,
                contentDescription = "Error",
                tint = StatColor.copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Oops! Something went wrong",
                color = TextLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage,
                color = TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatColor)
            ) {
                Text(text = "Retry", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        }
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
private fun RevenueCard(
    revenue: Double = 12450.0,
    growthPercentage: Double = 18.0
) {
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
                    text = "$${String.format("%.0f", revenue)}",
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
                        text = "+${growthPercentage.toInt()}% from last month",
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
private fun TopTrackSection(
    trackTitle: String = "Summer Nights",
    streams: String = "456.2K",
    ranking: String = "3rd most streamed"
) {
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
                        text = trackTitle,
                        color = TextLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$streams streams",
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
                            text = ranking,
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
private fun RecentActivitySection(activities: List<String> = emptyList()) {
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

        if (activities.isEmpty()) {
            Text(
                text = "No recent activities",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        } else {
            activities.forEach { activity ->
                ActivityItem(activity)
                Spacer(modifier = Modifier.height(10.dp))
            }
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