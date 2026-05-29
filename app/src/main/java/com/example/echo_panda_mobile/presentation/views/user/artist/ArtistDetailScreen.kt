package com.example.echo_panda_mobile.presentation.views.user.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.presentation.components.*
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun ArtistDetailScreen(
    artistId: String,
    onBack: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToPlayer: (String, Long?) -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    viewModel: ArtistDetailViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                // ─── Header ────────────────────────────────────────────────────
                ArtistHeader(
                    artist = artist,
                    headerColor = headerColor,
                    onBack = onBack
                )

                // ─── Actions & Popular ─────────────────────────────────────────
                PopularSection(
                    state = state,
                    onToggleFollow = { viewModel.toggleFollow() },
                    onTrackClick = onNavigateToPlayer,
                    onAddToFavorites = { track ->
                        viewModel.toggleFavorite(track)
                    },
                    onDashboardClick = onNavigateToDashboard
                )

                // ─── Albums Section ────────────────────────────────────────────
                Spacer(modifier = Modifier.height(32.dp))
                ArtistAlbumsSection(
                    albums = state.albums,
                    onAlbumClick = onNavigateToAlbum
                )

                // ─── Singles Section ───────────────────────────────────────────
                if (state.singles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    ArtistSinglesSection(
                        singles = state.singles,
                        onTrackClick = onNavigateToPlayer
                    )
                }

                Spacer(modifier = Modifier.height(120.dp))
            }

            // ─── Sticky Top Bar ──────────────────────────────────────────────
            ArtistStickyHeaderActions(
                artistName = artist.name,
                scrollOffset = scrollState.value,
                onBack = onBack
            )
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp)
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
            .height(340.dp)
    ) {
        // Background with image or gradient
        if (!artist.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = artist.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(headerColor, Color(0xFF05070D)),
                            startY = 0f,
                            endY = 1200f
                        )
                    )
            )
        }

        // Overlay Gradient for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF05070D).copy(alpha = 0.8f)),
                        startY = 400f
                    )
                )
        )

        // Artist Name and listeners
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Text(
                text = artist.name,
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.5).sp,
                lineHeight = 52.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${artist.monthlyListeners} monthly listeners",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PopularSection(
    state: com.example.echo_panda_mobile.presentation.viewsmodel.ArtistDetailUiState,
    onToggleFollow: () -> Unit,
    onTrackClick: (String, Long?) -> Unit,
    onAddToFavorites: (Track) -> Unit,
    onDashboardClick: () -> Unit
) {
    val isArtistRole = state.currentUser?.role?.lowercase() == "artist"
    val isOwnProfile = isArtistRole && state.currentUser?.name != null && state.currentUser.name == state.artist?.name

    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        // Control Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOwnProfile) {
                Button(
                    onClick = onDashboardClick,
                    colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Dashboard, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Dashboard", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    modifier = Modifier
                        .clickable { onToggleFollow() }
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    color = if (state.isFollowing) Color.White.copy(alpha = 0.1f) else Color.Transparent
                ) {
                    Text(
                        text = if (state.isFollowing) "Following" else "Follow",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            IconButton(onClick = { /* More Options */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(onClick = { /* Shuffle */ }) {
                Icon(Icons.Default.Shuffle, contentDescription = null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(24.dp))
            }
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(EchoPandaColors.AccentBlue)
                    .clickable { /* Play Top Tracks */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Popular",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        state.popularTracks.forEachIndexed { index, track ->
            PopularTrackItem(
                index = index + 1,
                track = track,
                onClick = { onTrackClick(track.id, track.resumePositionMs) },
                onFavorite = { onAddToFavorites(track) }
            )
        }
    }
}

@Composable
private fun PopularTrackItem(
    index: Int,
    track: Track,
    onClick: () -> Unit,
    onFavorite: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index.toString(),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 14.sp,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center
        )
        
        SquareArtCard(
            colors = track.placeholderColors,
            imageUrl = track.imageUrl,
            size = 52.dp,
            cornerRadius = 4.dp
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "273,225,108", // Mock stream count
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
        
        IconButton(onClick = onFavorite) {
            Icon(
                imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (track.isFavorite) Color.Red else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(EchoPandaColors.BgCardDark)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            if (track.isFavorite) "Remove from Favorites" else "Add to Favorites",
                            color = Color.White
                        )
                    },
                    onClick = { onFavorite(); showMenu = false },
                    leadingIcon = {
                        Icon(
                            if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            null,
                            tint = if (track.isFavorite) Color.Red else EchoPandaColors.AccentBlue
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to Playlist", color = Color.White) },
                    onClick = {
                        showPlaylistPicker = true
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null, tint = EchoPandaColors.AccentBlue) }
                )
            }
        }
    }

    if (showPlaylistPicker) {
        PlaylistPickerDialog(track = track, onDismiss = { showPlaylistPicker = false })
    }
}

@Composable
private fun ArtistAlbumsSection(
    albums: List<Album>,
    onAlbumClick: (String) -> Unit
) {
    if (albums.isEmpty()) return
    
    Column {
        SectionHeader(
            fullTitle = "Albums",
            onViewAll = { }
        )
        Spacer(modifier = Modifier.height(16.dp))
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
    onTrackClick: (String, Long?) -> Unit
) {
    Column {
        SectionHeader(
            fullTitle = "Singles & EPs",
            onViewAll = { }
        )
        Spacer(modifier = Modifier.height(16.dp))
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
                    imageUrl = track.imageUrl,
                    size = 140.dp,
                    onClick = { onTrackClick(track.id, track.resumePositionMs) }
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
    val alpha = (scrollOffset / 300f).coerceIn(0f, 1f)
    
    Surface(
        color = Color(0xFF05070D).copy(alpha = alpha),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
