package com.example.echo_panda_mobile.presentation.views.user.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.presentation.components.*
import com.example.echo_panda_mobile.presentation.viewmodel.HomeViewModel
import com.example.echo_panda_mobile.presentation.viewmodel.GlobalPlayerViewModel
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors

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
            .background(Color(0xFF05070D))
    ) {
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
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EchoPandaColors.AccentBlue)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(16.dp))

                    // ── Continue Listening ──────────────────────────────────────
                    if (state.recentPlaylists.isNotEmpty()) {
                        Text(
                            text = "Continue Listening",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(16.dp))
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            state.recentPlaylists.take(6).chunked(2).forEach { rowItems ->
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        Spacer(Modifier.height(32.dp))
                    }

                    // ── Popular Artists ───────────────────────────────────────────
                    SectionHeader(
                        fullTitle = "Popular Artists",
                        highlightPart = "Artists",
                        highlightColor = EchoPandaColors.AccentBlue,
                        onViewAll = { /* TODO */ }
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        state.popularArtists.forEach { artist ->
                            ArtistCircleCard(
                                artist = artist,
                                onClick = { onNavigateToArtist(artist.id) }
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // ── Top Albums ────────────────────────────────────────────────
                    SectionHeader(
                        fullTitle = "Top Albums",
                        highlightPart = "Albums",
                        highlightColor = EchoPandaColors.AccentBlue,
                        onViewAll = { /* TODO */ }
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        state.topAlbums.forEach { album ->
                            AlbumCard(
                                album = album,
                                onClick = { onNavigateToAlbum(album.id) }
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // ── Featured Artist ──────────────────────────────────────────
                    state.featuredArtist?.let { featured ->
                        FeaturedArtistCard(
                            featured = featured,
                            onListenNow = { onNavigateToArtist(featured.artist.id) }
                        )
                        Spacer(Modifier.height(32.dp))
                    }

                    // ── Based on your recent listening ────────────────────────────
                    if (state.recentListening.isNotEmpty()) {
                        Text(
                            text = "Based on your recent listening",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            state.recentListening.forEach { playlist ->
                                SquareArtCard(
                                    colors = playlist.placeholderColors,
                                    size = 160.dp,
                                    cornerRadius = 16.dp,
                                    onClick = { 
                                        // Mock play
                                        val mockTrack = com.example.echo_panda_mobile.data.model.Track(
                                            id = "p1", title = playlist.title, artist = "Various Artists",
                                            placeholderColors = playlist.placeholderColors
                                        )
                                        globalPlayerViewModel.playTrack(mockTrack)
                                        onNavigateToPlayer(mockTrack.id)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(100.dp))
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person, 
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "Welcome back !",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = userName,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
        
        IconButton(onClick = { /* TODO */ }) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
        }
    }
}
