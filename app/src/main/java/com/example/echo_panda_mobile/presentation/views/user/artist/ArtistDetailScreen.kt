package com.example.echo_panda_mobile.presentation.views.user.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.presentation.components.*
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistDetailViewModel

@Composable
fun ArtistDetailScreen(
    artistId: String,
    onBack: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    viewModel: ArtistDetailViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(artistId) {
        viewModel.loadArtist(artistId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05070D))
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = EchoPandaColors.AccentBlue
            )
        } else if (state.artist != null) {
            val artist = state.artist!!
            val headerColor = artist.placeholderColors.firstOrNull() ?: Color(0xFF1A1A2A)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // ─── Header with Parallax-ish Effect ───────────────────────────
                ArtistHeader(
                    artist = artist,
                    headerColor = headerColor,
                    onBack = onBack
                )

                // ─── Popular Section ───────────────────────────────────────────
                Spacer(modifier = Modifier.height(16.dp))
                PopularSection(
                    tracks = state.popularTracks,
                    onTrackClick = onNavigateToPlayer
                )

                // ─── Albums Section ────────────────────────────────────────────
                Spacer(modifier = Modifier.height(24.dp))
                ArtistAlbumsSection(
                    albums = state.albums,
                    onAlbumClick = onNavigateToAlbum
                )

                // ─── Singles & EPs Section ─────────────────────────────────────
                Spacer(modifier = Modifier.height(24.dp))
                ArtistSinglesSection(
                    singles = state.singles,
                    onTrackClick = onNavigateToPlayer
                )

                Spacer(modifier = Modifier.height(120.dp))
            }

            // ─── Top Sticky Actions (Floating back button and Play button) ───
            ArtistStickyHeaderActions(
                artistName = artist.name,
                scrollOffset = scrollState.value,
                onBack = onBack
            )
        }
    }
}

@Composable
private fun ArtistHeader(
    artist: Artist,
    headerColor: Color,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Background Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(headerColor, Color(0xFF05070D)),
                        startY = 0f,
                        endY = 1000f
                    )
                )
        )

        // Artist Name and Listeners
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = artist.name,
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "55.1M monthly listeners", // Hardcoded per user image style
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PopularSection(
    tracks: List<Track>,
    onTrackClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { /* Follow */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Following", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            IconButton(onClick = { /* More */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(onClick = { /* Shuffle */ }) {
                Icon(Icons.Default.Shuffle, contentDescription = null, tint = EchoPandaColors.AccentBlue)
            }
            
            FloatingActionButton(
                onClick = { /* Play */ },
                containerColor = EchoPandaColors.AccentBlue,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play All", modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Popular",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        tracks.take(5).forEachIndexed { index, track ->
            PopularTrackRow(
                index = index + 1,
                track = track,
                onClick = { onTrackClick(track.id) }
            )
        }
    }
}

@Composable
private fun PopularTrackRow(
    index: Int,
    track: Track,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index.toString(),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            modifier = Modifier.width(24.dp)
        )
        
        SquareArtCard(
            colors = track.placeholderColors,
            size = 48.dp,
            cornerRadius = 4.dp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "273,225,108", // Mock stream count per user image
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
        
        IconButton(onClick = { /* More */ }) {
            Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun ArtistAlbumsSection(
    albums: List<Album>,
    onAlbumClick: (String) -> Unit
) {
    Column {
        SectionHeader(
            fullTitle = "Albums",
            onViewAll = { }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            albums.forEach { album ->
                AlbumCard(
                    album = album,
                    onClick = { onAlbumClick(album.id) }
                )
            }
        }
    }
}

@Composable
private fun ArtistSinglesSection(
    singles: List<Track>,
    onTrackClick: (String) -> Unit
) {
    Column {
        SectionHeader(
            fullTitle = "Singles & EPs",
            onViewAll = { }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            singles.forEach { track ->
                LabeledArtCard(
                    title = track.title,
                    subLabel = "Single",
                    colors = track.placeholderColors,
                    onClick = { onTrackClick(track.id) }
                )
            }
        }
    }
}

@Composable
private fun ArtistStickyHeaderActions(
    artistName: String,
    scrollOffset: Int,
    onBack: () -> Unit
) {
    // Basic sticky behavior based on scroll offset
    val alpha = (scrollOffset / 400f).coerceIn(0f, 1f)
    
    Surface(
        color = Color(0xFF05070D).copy(alpha = alpha),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            
            if (alpha > 0.8f) {
                Text(
                    text = artistName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
