package com.example.echo_panda_mobile.presentation.views.admin

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.echo_panda_mobile.data.model.Track
import com.example.echo_panda_mobile.presentation.viewsmodel.AdminAlbumDetailViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.AdminAlbumDetailViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val BgDark = Color(0xFF05070D)
private val CardBg = Color(0xFF161C24)
private val AccentPurple = Color(0xFFFF00FF)
private val TextMuted = Color.White.copy(alpha = 0.5f)
private val ActiveGreen = Color(0xFF00C853)
private val InactiveRed = Color(0xFFFF5252)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAlbumDetailScreen(
    albumId: String,
    onBack: () -> Unit,
    onNavigateToSongDetail: (String) -> Unit = {}
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AdminAlbumDetailViewModel = viewModel(
        factory = AdminAlbumDetailViewModelFactory(application, albumId)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Album Details", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark
    ) { paddingValues ->
        var isRefreshing by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    viewModel.loadAlbum()
                    delay(1000)
                    isRefreshing = false
                }
            },
            modifier = Modifier.padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.errorMessage.orEmpty(), color = Color(0xFFFF6B6B), fontSize = 14.sp)
                    }
                }

                uiState.album != null -> {
                    val album = uiState.album!!
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!album.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = album.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Album, contentDescription = null, tint = TextMuted, modifier = Modifier.size(80.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(album.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(album.artist, color = AccentPurple, fontSize = 18.sp, fontWeight = FontWeight.Medium)

                        Spacer(modifier = Modifier.height(32.dp))

                        DetailItem("Artist", album.artist, Icons.Default.Group, modifier = Modifier.fillMaxWidth())
                        DetailItem("Album ID", album.id, Icons.Default.Visibility, modifier = Modifier.fillMaxWidth())
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailItem("Tracks", album.tracks.size.toString(), Icons.Default.MusicNote, modifier = Modifier.weight(1f))
                            DetailItem("Duration", album.totalDuration, Icons.Default.AccessTime, modifier = Modifier.weight(1f))
                        }

                        DetailToggleRow(
                            label = if (album.isActive) "Active" else "Banned",
                            description = if (album.isActive) {
                                "Album is visible on the platform"
                            } else {
                                "Album is hidden from listeners (is_active = false)"
                            },
                            checked = album.isActive,
                            enabled = !uiState.isUpdatingStatus,
                            onCheckedChange = { viewModel.setAlbumActive(it) }
                        )

                        uiState.actionMessage?.let { message ->
                            Text(message, color = AccentPurple, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Track List", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CardBg,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (album.tracks.isEmpty()) {
                                    Text("No track data returned by the API.", color = TextMuted, fontSize = 13.sp)
                                } else {
                                    album.tracks.forEachIndexed { index, track ->
                                        AlbumTrackRow(
                                            index = index + 1, 
                                            track = track,
                                            onClick = { onNavigateToSongDetail(track.id) }
                                        )
                                        if (index < album.tracks.lastIndex) {
                                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumTrackRow(index: Int, track: Track, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text("$index.", color = TextMuted, fontSize = 14.sp, modifier = Modifier.width(28.dp))
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (!track.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = track.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(track.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(track.artist, color = TextMuted, fontSize = 12.sp)
            }
        }
        
        Text(
            text = formatDuration(track.durationMs),
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
private fun DetailToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = CardBg,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(description, color = TextMuted, fontSize = 12.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ActiveGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = InactiveRed,
                    uncheckedBorderColor = Color.Transparent
                ),
                modifier = Modifier.scale(0.85f)
            )
        }
    }
}

@Composable
private fun DetailItem(
    label: String, 
    value: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Surface(
        modifier = modifier.padding(vertical = 8.dp),
        color = CardBg,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, color = TextMuted, fontSize = 12.sp)
                Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            }
        }
    }
}
