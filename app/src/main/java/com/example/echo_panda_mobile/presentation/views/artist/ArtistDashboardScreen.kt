package com.example.echo_panda_mobile.presentation.views.artist

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.presentation.navigation.Routes
import com.example.echo_panda_mobile.presentation.components.*
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistDashboardViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistDashboardViewModelFactory
import androidx.compose.ui.platform.LocalContext

@Composable
fun ArtistDashboardScreen(
    currentUser: User?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: ArtistDashboardViewModel = viewModel(factory = ArtistDashboardViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasUnread by viewModel.hasUnreadNotifications.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.loadDashboard(currentUser)
        }
    }

    Scaffold(
        containerColor = Color(0xFF05070D),
        bottomBar = { ArtistBottomBar(Routes.ARTIST_DASHBOARD, onNavigate) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is ArtistDashboardViewModel.DashboardUiState.Loading -> {
                    ArtistDashboardSkeleton()
                }
                is ArtistDashboardViewModel.DashboardUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        ArtistDashboardHeader(
                            user = state.data.user,
                            onLogout = onLogout,
                            onNavigate = onNavigate,
                            hasUnreadNotifications = hasUnread
                        )
                        
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Spacer(Modifier.height(24.dp))

                            if (state.data.topListenedSongs.isNotEmpty()) {
                                TopListenedSongsSection(
                                    songs = state.data.topListenedSongs,
                                    onSongClick = { songId ->
                                        onNavigate(Routes.ARTIST_PLAYER.replace("{trackId}", songId))
                                    }
                                )
                                Spacer(Modifier.height(32.dp))
                            }
                            

                            
                            Spacer(Modifier.height(32.dp))
                            
                            RecentActivitySection(activities = state.data.recentActivities)
                            
                            Spacer(Modifier.height(32.dp))
                            
                            QuickActionsSection(onNavigate = onNavigate)
                            
                            Spacer(Modifier.height(40.dp))
                        }
                    }
                }
                is ArtistDashboardViewModel.DashboardUiState.Error -> {
                    ErrorState(message = state.message) { 
                        viewModel.loadDashboard(currentUser)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistDashboardHeader(
    user: User,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    hasUnreadNotifications: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(EchoPandaColors.AccentBlue.copy(alpha = 0.2f), Color.Transparent)
                )
            )
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { onNavigate(Routes.ARTIST_PROFILE) },
                    contentAlignment = Alignment.Center
                ) {
                    val photoUrl = user.getDisplayPhotoUrl()
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = androidx.compose.ui.graphics.painter.ColorPainter(Color.DarkGray)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(EchoPandaColors.AccentBlue, Color(0xFFA78BFA)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.take(1).uppercase(),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = "Good morning, Artist!",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
            
            Row {
                IconButton(
                    onClick = { onNavigate(Routes.ARTIST_NOTIFICATIONS) },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Box {
                        Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        if (hasUnreadNotifications) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EchoPandaColors.ErrorRed)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.background(EchoPandaColors.ErrorRed.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = EchoPandaColors.ErrorRed, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}


@Composable
private fun TopListenedSongsSection(
    songs: List<TopListenedSong>,
    onSongClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Top Listened Songs",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(songs, key = { it.id }) { song ->
                TopListenedSongCard(
                    song = song,
                    onClick = { onSongClick(song.id) }
                )
            }
        }
    }
}

@Composable
private fun TopListenedSongCard(
    song: TopListenedSong,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
    ) {
        SquareArtCard(
            colors = listOf(Color(0xFF2C2C3A), Color(0xFF1A1A26)),
            imageUrl = song.imageUrl,
            size = 120.dp,
            cornerRadius = 16.dp,
            onClick = onClick
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = song.title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = formatPlayCount(song.playCount),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

private fun formatPlayCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${String.format("%.1f", count / 1_000_000f)}M plays"
        count >= 1_000 -> "${String.format("%.1f", count / 1_000f)}K plays"
        count > 0 -> "$count plays"
        else -> "0 plays"
    }
}


@Composable
private fun StatCard(
    title: String,
    value: String,
    growth: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(110.dp),
        color = Color(0xFF121A26),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                }
                Text(growth, color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(title, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}


@Composable
private fun RecentActivitySection(activities: List<ActivityItem>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Recent Activity", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        activities.forEach { activity ->
            ActivityRow(activity)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ActivityRow(activity: ActivityItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121A26), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(EchoPandaColors.AccentBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Bolt, null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(activity.text, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun QuickActionsSection(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Quick Actions", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickActionButton(
                    title = "Upload Song",
                    icon = Icons.Default.CloudUpload,
                    color = EchoPandaColors.AccentBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Routes.ARTIST_UPLOAD) }
                )
                QuickActionButton(
                    title = "Manage Albums",
                    icon = Icons.Default.Album,
                    color = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Routes.ARTIST_ALBUMS) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickActionButton(
                    title = "Support",
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f),
                    onClick = { /* Navigate to support */ }
                )
                QuickActionButton(
                    title = "Comments",
                    icon = Icons.AutoMirrored.Filled.Comment,
                    color = Color(0xFFFFC107),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Routes.ARTIST_COMMENTS) }
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        color = Color(0xFF121A26),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ArtistDashboardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 20.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 14.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(width = 180.dp, height = 24.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
        )

        Spacer(Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(3) {
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 160.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Stats Grid Skeleton
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Section Title Skeleton
        Box(
            modifier = Modifier
                .size(width = 150.dp, height = 24.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
        )
        
        Spacer(Modifier.height(16.dp))

        // Card Skeleton
        Box(
            modifier = Modifier
                .size(width = 160.dp, height = 200.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
        )
    }
}
