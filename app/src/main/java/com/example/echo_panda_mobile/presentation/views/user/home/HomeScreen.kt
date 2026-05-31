package com.example.echo_panda_mobile.presentation.views.user.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.echo_panda_mobile.presentation.components.*
import com.example.echo_panda_mobile.presentation.viewmodel.HomeViewModel
import com.example.echo_panda_mobile.presentation.viewmodel.GlobalPlayerViewModel
import com.example.echo_panda_mobile.presentation.navigation.BrowseSection
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors

@Composable
fun HomeScreen(
    selectedNav: Int = 0,
    onNavSelect: (Int) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAlbum: (String) -> Unit = {},
    onNavigateToPlayer: (String, Long?) -> Unit = { _, _ -> },
    onNavigateToArtist: (String) -> Unit = {},
    onNavigateToBrowse: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
    globalPlayerViewModel: GlobalPlayerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshUserHeader()
                viewModel.refreshContinueListening()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                    userPhotoUrl = state.userPhotoUrl,
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
                    if (state.errorMessage != null) {
                        Text(
                            text = state.errorMessage!!,
                            color = EchoPandaColors.ErrorRed,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp
                        )
                    }

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
                                                val track = com.example.echo_panda_mobile.data.model.Track(
                                                    id = playlist.id,
                                                    title = playlist.title,
                                                    artist = playlist.labelOverlay ?: "Various Artists",
                                                    imageUrl = playlist.imageUrl,
                                                    placeholderColors = playlist.placeholderColors
                                                )
                                                globalPlayerViewModel.playTrack(track)
                                                onNavigateToPlayer(track.id, playlist.resumePositionMs)
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
                        onViewAll = { onNavigateToBrowse(BrowseSection.POPULAR_ARTISTS) }
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
                        onViewAll = { onNavigateToBrowse(BrowseSection.TOP_ALBUMS) }
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
                            onListenNow = { onNavigateToArtist(featured.artist.id) },
                            onClick = { onNavigateToArtist(featured.artist.id) }
                        )
                        Spacer(Modifier.height(32.dp))
                    }

                    // ── Random Artists ─────────────────────────────────────────────
                    if (state.randomArtists.isNotEmpty()) {
                        SectionHeader(
                            fullTitle = "Random Artists",
                            highlightPart = "Artists",
                            highlightColor = EchoPandaColors.AccentBlue,
                            onViewAll = { onNavigateToBrowse(BrowseSection.POPULAR_ARTISTS) }
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            state.randomArtists.forEach { artist ->
                                ArtistCircleCard(
                                    artist = artist,
                                    onClick = { onNavigateToArtist(artist.id) }
                                )
                            }
                        }
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
                                    imageUrl = playlist.imageUrl,
                                    size = 160.dp,
                                    cornerRadius = 16.dp,
                                    onClick = { 
                                        val track = com.example.echo_panda_mobile.data.model.Track(
                                            id = playlist.id,
                                            title = playlist.title,
                                            artist = playlist.labelOverlay ?: "Various Artists",
                                            imageUrl = playlist.imageUrl,
                                            placeholderColors = playlist.placeholderColors
                                        )
                                        globalPlayerViewModel.playTrack(track)
                                        onNavigateToPlayer(track.id, playlist.resumePositionMs)
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
private fun HomeTopBar(userName: String, userPhotoUrl: String?, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onProfileClick)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F2537)),
                contentAlignment = Alignment.Center
            ) {
                if (!userPhotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(userPhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile photo",
                        tint = EchoPandaColors.AccentBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Welcome back!",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        IconButton(onClick = { /* TODO */ }) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
        }
    }
}
