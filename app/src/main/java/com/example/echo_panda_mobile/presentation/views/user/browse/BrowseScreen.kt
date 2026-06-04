package com.example.echo_panda_mobile.presentation.views.user.browse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.data.model.Track
import com.example.echo_panda_mobile.presentation.components.*
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewmodel.GlobalPlayerViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.BrowseContent
import com.example.echo_panda_mobile.presentation.viewsmodel.BrowseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    onBack: () -> Unit,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToPlayer: (String, Long?) -> Unit,
    viewModel: BrowseViewModel = viewModel(),
    globalPlayerViewModel: GlobalPlayerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF05070D))
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EchoPandaColors.AccentBlue)
                }
            }
            state.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.errorMessage!!, color = EchoPandaColors.ErrorRed)
                }
            }
            state.content == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nothing to show", color = Color.White.copy(alpha = 0.5f))
                }
            }
            else -> BrowseContentList(
                content = state.content!!,
                modifier = Modifier.padding(padding),
                onNavigateToArtist = onNavigateToArtist,
                onNavigateToAlbum = onNavigateToAlbum,
                onNavigateToPlayer = onNavigateToPlayer,
                onToggleFavorite = viewModel::toggleFavorite,
                globalPlayerViewModel = globalPlayerViewModel
            )
        }
    }
}

@Composable
private fun BrowseContentList(
    content: BrowseContent,
    modifier: Modifier = Modifier,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToPlayer: (String, Long?) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    globalPlayerViewModel: GlobalPlayerViewModel
) {
    when (content) {
        is BrowseContent.Artists -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(content.items, key = { it.id }) { artist ->
                    ArtistCircleCard(
                        artist = artist,
                        onClick = { onNavigateToArtist(artist.id) }
                    )
                }
            }
        }
        is BrowseContent.Albums -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(content.items, key = { it.id }) { album ->
                    AlbumListRow(album = album, onClick = { onNavigateToAlbum(album.id) })
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.1f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
        is BrowseContent.Tracks -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(content.items, key = { it.id }) { track ->
                    SongCardHorizontal(
                        track = track,
                        onClick = {
                            globalPlayerViewModel.playQueue(content.items, track.id)
                            onNavigateToPlayer(track.id, track.resumePositionMs)
                        },
                        onFavoriteClick = { onToggleFavorite(track) }
                    )
                }
            }
        }
        is BrowseContent.Genres -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(content.items, key = { it.id }) { genre ->
                    LabeledArtCard(
                        title = genre.name,
                        subLabel = genre.subLabel,
                        colors = genre.placeholderColors,
                        imageUrl = genre.imageUrl
                    )
                }
            }
        }
        is BrowseContent.Moods -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(content.items, key = { it.id }) { mood ->
                    LabeledArtCard(
                        title = mood.name,
                        subLabel = mood.subLabel,
                        colors = mood.placeholderColors,
                        imageUrl = mood.imageUrl
                    )
                }
            }
        }
        is BrowseContent.ContinueListening -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(content.items, key = { it.id }) { playlist ->
                    RecentPlaylistCard(
                        playlist = playlist,
                        onClick = {
                            val track = Track(
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
    }
}
