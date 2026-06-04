package com.example.echo_panda_mobile.presentation.views.user.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.echo_panda_mobile.presentation.components.EchoPandaBottomBar
import com.example.echo_panda_mobile.presentation.components.SongRow
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewmodel.GlobalPlayerViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    selectedNav: Int = 3,
    onNavSelect: (Int) -> Unit = {},
    onBack: () -> Unit,
    onNavigateToPlayer: (String, Long?) -> Unit,
    viewModel: FavoritesViewModel = viewModel(),
    globalPlayerViewModel: GlobalPlayerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val playerState by globalPlayerViewModel.playerState.collectAsState()
    val themeColor = EchoPandaColors.AccentBlue

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05070D))
    ) {
        // Dynamic Background Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            themeColor.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                FavoritesTopBar(
                    onBack = onBack,
                    searchQuery = state.searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChange
                )
            },
            bottomBar = {
                EchoPandaBottomBar(selectedIndex = selectedNav, onSelect = onNavSelect)
            }
        ) { padding ->
            if (state.isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.errorMessage != null && state.favoriteTracks.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.errorMessage ?: "Could not load liked songs",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            } else if (state.favoriteTracks.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No liked songs yet",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding() + 16.dp
                    )
                ) {
                    item {
                        FavoritesHeaderSection(
                            songCount = state.favoriteTracks.size,
                            themeColor = themeColor,
                            onPlayAll = {
                                state.favoriteTracks.firstOrNull()?.let { track ->
                                    globalPlayerViewModel.playQueue(state.favoriteTracks, track.id)
                                    onNavigateToPlayer(track.id, track.resumePositionMs)
                                }
                            }
                        )
                    }

                    item {
                        SongListHeader()
                    }

                    itemsIndexed(state.favoriteTracks) { index, track ->
                        val isPlaying = playerState.currentTrack?.id == track.id && playerState.isPlaying
                        SongRow(
                            index = index,
                            track = track,
                            isPlaying = isPlaying,
                            onClick = {
                                globalPlayerViewModel.playQueue(state.favoriteTracks, track.id)
                                onNavigateToPlayer(track.id, track.resumePositionMs)
                            },
                            onAddToFavorites = {
                                viewModel.toggleFavorite(track)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesTopBar(
    onBack: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Liked Songs",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("Search liked songs") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
            },
            singleLine = true
        )
    }
}

@Composable
private fun FavoritesHeaderSection(
    songCount: Int,
    themeColor: Color,
    onPlayAll: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(themeColor.copy(alpha = 0.8f), themeColor.copy(alpha = 0.4f))
                )
            )
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF00D9FF), Color(0xFF00A3C2)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Liked Songs",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Your Collection",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "$songCount songs",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    IconButton(
                        onClick = onPlayAll,
                        enabled = songCount > 0,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SongListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = "Time",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Text(
            text = "More",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
    }
}
