package com.example.echo_panda_mobile.presentation.views.user.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
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
import com.example.echo_panda_mobile.presentation.components.ArtPlaceholder
import com.example.echo_panda_mobile.presentation.components.EchoPandaBottomBar
import com.example.echo_panda_mobile.presentation.components.SongRow
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewmodel.GlobalPlayerViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.PlaylistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    playlistTitle: String = "Playlist",
    selectedNav: Int = 3,
    onNavSelect: (Int) -> Unit = {},
    onBack: () -> Unit = {},
    onNavigateToPlayer: (String, Long?) -> Unit = { _: String, _: Long? -> },
    viewModel: PlaylistViewModel = viewModel(),
    globalPlayerViewModel: GlobalPlayerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val playerState by globalPlayerViewModel.playerState.collectAsState()
    val displayTitle = state.playlist?.title ?: playlistTitle
    val themeColor = state.playlist?.placeholderColors?.firstOrNull() ?: EchoPandaColors.AccentBlue

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05070D))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(themeColor.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = { EchoPandaBottomBar(selectedNav, onNavSelect) }
        ) { padding ->
            when {
                state.isLoading -> {
                    Box(
                        Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = EchoPandaColors.AccentBlue)
                    }
                }
                state.error != null && state.tracks.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.error ?: "Could not load playlist",
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding() + 16.dp
                        )
                    ) {
                        item {
                            PlaylistHeader(
                                title = displayTitle,
                                songCount = state.tracks.size,
                                imageUrl = state.playlist?.imageUrl,
                                colors = state.playlist?.placeholderColors
                                    ?: listOf(themeColor, Color(0xFF1A1A26))
                            )
                        }

                        if (state.tracks.isEmpty()) {
                            item {
                                Text(
                                    text = "No songs in this playlist yet",
                                    color = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                        } else {
                            itemsIndexed(state.tracks) { index, track ->
                                val isPlaying =
                                    playerState.currentTrack?.id == track.id && playerState.isPlaying
                                SongRow(
                                    index = index,
                                    track = track,
                                    isPlaying = isPlaying,
                                    onClick = {
                                        globalPlayerViewModel.playQueue(state.tracks, track.id)
                                        onNavigateToPlayer(track.id, track.resumePositionMs)
                                    },
                                    onAddToFavorites = { viewModel.toggleFavorite(track) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistHeader(
    title: String,
    songCount: Int,
    imageUrl: String?,
    colors: List<Color>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            ArtPlaceholder(
                colors = colors,
                imageUrl = imageUrl,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.width(20.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "$songCount songs",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }
    }
}
