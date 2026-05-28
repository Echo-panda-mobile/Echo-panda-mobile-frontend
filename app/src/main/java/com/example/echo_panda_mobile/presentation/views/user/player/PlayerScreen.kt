package com.example.echo_panda_mobile.presentation.views.user.player

import java.util.Locale
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.presentation.components.SquareArtCard
import com.example.echo_panda_mobile.presentation.viewsmodel.PlayerViewModel
import com.example.echo_panda_mobile.presentation.components.ErrorState
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors

@Composable
fun PlayerScreen(
    trackId: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(trackId) {
        viewModel.loadTrack(trackId)
    }

    val track = state.track

    if (state.showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closePlaylistDialog() },
            containerColor = EchoPandaColors.BgCardDark,
            title = { Text("Add to Playlist", color = Color.White) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(state.userPlaylists) { playlist ->
                        Text(
                            text = playlist.title,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.addToPlaylist(playlist.id) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05070D))
    ) {
        if (state.isLoading) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator(color = EchoPandaColors.AccentBlue)
                Spacer(Modifier.height(8.dp))
                Text("Loading track $trackId...", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        } else if (state.errorMessage != null) {
            ErrorState(
                message = state.errorMessage!!,
                onRetry = { viewModel.loadTrack(trackId) }
            )
        } else if (track != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PLAYING FROM ALBUM:",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = track.album ?: "Unknown Album",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Large Artwork
                SquareArtCard(
                    colors = track.placeholderColors,
                    imageUrl = track.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    size = androidx.compose.ui.unit.Dp.Unspecified
                )

                Spacer(Modifier.height(48.dp))

                // Song Info & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = track.artist,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 18.sp
                        )
                    }
                    IconButton(onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Listen to ${track.title} by ${track.artist} on Echo Panda!")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Track"))
                    }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (track.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = EchoPandaColors.AccentBlue
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Progress Bar
                Slider(
                    value = state.progress,
                    onValueChange = { viewModel.updateProgress(it) },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = EchoPandaColors.AccentBlue,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTime(state.currentPositionMs), color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    val totalDuration = if (state.durationMs > 0) state.durationMs else track.durationMs
                    Text(text = formatTime(totalDuration), color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }

                Spacer(Modifier.height(16.dp))

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Shuffle */ }) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = Color.White.copy(alpha = 0.5f))
                    }
                    IconButton(onClick = { /* Previous */ }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(EchoPandaColors.AccentBlue)
                            .clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    IconButton(onClick = { /* Next */ }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    IconButton(onClick = { /* Visualizer */ }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Visualizer", tint = Color.White.copy(alpha = 0.5f))
                    }
                }

                Spacer(Modifier.height(32.dp))
                
                // Extra actions (Plus and Download)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.openPlaylistDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add to Playlist", tint = Color.White.copy(alpha = 0.5f))
                    }
                    IconButton(onClick = { viewModel.downloadTrack() }) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Download",
                            tint = if (track.isDownloaded) EchoPandaColors.AccentBlue else Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(Modifier.height(48.dp))

                // Lyrics Section
                Text(
                    text = "LYRICS",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    track.placeholderColors.firstOrNull()?.copy(alpha = 0.5f) ?: EchoPandaColors.AccentBlue.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Text(
                        text = track.lyrics ?: "No lyrics available for ${track.title}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 32.sp
                    )
                }
                
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
