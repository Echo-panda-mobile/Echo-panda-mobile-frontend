package com.example.echo_panda_mobile.presentation.views.user.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.presentation.components.*
import com.example.echo_panda_mobile.presentation.viewmodel.HomeViewModel
import com.example.echo_panda_mobile.presentation.viewmodel.GlobalPlayerViewModel

@Composable
fun HomeScreen(
    selectedNav: Int = 0,
    onNavSelect: (Int) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAlbum: (String) -> Unit = {},
    onNavigateToPlayer: (String) -> Unit = {},
    onNavigateToArtist: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
    globalPlayerViewModel: GlobalPlayerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05070D)) // Solid dark background
    ) {
        // Subtle top glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                HomeTopBar(
                    userName = state.userName,
                    onProfileClick = onNavigateToProfile
                )
            },
            bottomBar = { EchoPandaBottomBar(selectedNav, onNavSelect) }
        ) { padding ->
            if (state.isLoading && state.recentPlaylists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(16.dp))

                    // ── Recent Playlists ──────────────────────────────────────
                    if (state.recentPlaylists.isNotEmpty()) {
                        Text(
                            text = "Jump back in",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(16.dp))
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.recentPlaylists.take(6).chunked(2).forEach { rowItems ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowItems.forEach { playlist ->
                                        RecentPlaylistCard(
                                            playlist = playlist,
                                            modifier = Modifier.weight(1f),
                                            onClick = { 
                                                // Mock playing a track from this playlist
                                                val mockTrack = com.example.echo_panda_mobile.data.model.Track(
                                                    id = "p1", title = playlist.title, artist = "Various Artists",
                                                    placeholderColors = playlist.placeholderColors
                                                )
                                                globalPlayerViewModel.playTrack(mockTrack)
                                                onNavigateToPlayer(mockTrack.id)
                                            }
                                        )
                                    }
                                    if (rowItems.size < 2) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── Popular Artists ───────────────────────────────────────────
                    SectionHeader(fullTitle = "Popular Artists", onViewAll = {})
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        state.popularArtists.take(4).forEach { artist ->
                            ArtistCircleCard(
                                artist = artist,
                                onClick = { onNavigateToArtist(artist.id) }
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── Top Albums ────────────────────────────────────────────────
                    SectionHeader(fullTitle = "Top Albums", onViewAll = {})
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.topAlbums.take(3).forEach { album ->
                            AlbumCard(
                                album = album,
                                onClick = { onNavigateToAlbum(album.id) }
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(userName: String, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome,",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    Icons.Default.NotificationsNone, 
                    contentDescription = "Notifications",
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person, 
                    contentDescription = "Profile",
                    tint = Color.White
                )
            }
        }
    }
}
