package com.example.echo_panda_mobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo_panda_mobile.data.remote.SongDto
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumTracksBottomSheet(
    visible: Boolean,
    albumTitle: String,
    isLoading: Boolean,
    error: String?,
    songs: List<SongDto>,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onSongClick: (String) -> Unit,
    onEditSong: (String) -> Unit,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF121A26),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                "Tracks in $albumTitle",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    isLoading -> "Loading tracks…"
                    error != null -> error
                    else -> "${songs.size} song${if (songs.size == 1) "" else "s"}"
                },
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = EchoPandaColors.AccentBlue)
                    }
                }
                error != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(error, color = EchoPandaColors.ErrorRed, fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = onRetry) {
                            Text("Retry", color = EchoPandaColors.AccentBlue)
                        }
                    }
                }
                songs.isEmpty() -> {
                    Text(
                        "No songs in this album yet.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
                else -> {
                    songs.forEach { song ->
                        val songId = song.id?.toString() ?: return@forEach
                        AlbumTrackRow(
                            song = song,
                            onClick = { onSongClick(songId) },
                            onEdit = { onEditSong(songId) },
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumTrackRow(
    song: SongDto,
    onClick: () -> Unit,
    onEdit: () -> Unit,
) {
    Surface(
        color = Color(0xFF1E2736),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SquareArtCard(
                colors = listOf(Color(0xFF2C2C3A), Color(0xFF1A1A26)),
                imageUrl = song.coverUrl,
                size = 52.dp,
                cornerRadius = 10.dp,
                onClick = onClick,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    song.title ?: "Unknown",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    buildString {
                        song.trackNumber?.let { append("Track $it • ") }
                        val duration = song.durationSeconds ?: 0
                        append("${duration / 60}:${String.format("%02d", duration % 60)}")
                    },
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(20.dp))
            }
        }
    }
}
