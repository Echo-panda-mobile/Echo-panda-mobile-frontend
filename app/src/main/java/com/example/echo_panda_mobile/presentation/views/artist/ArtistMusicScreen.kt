package com.example.echo_panda_mobile.presentation.views.artist

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
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.data.remote.AlbumDto
import com.example.echo_panda_mobile.data.remote.SongDto
import com.example.echo_panda_mobile.presentation.components.AlbumTracksBottomSheet
import com.example.echo_panda_mobile.presentation.components.ArtistBottomBar
import com.example.echo_panda_mobile.presentation.components.ErrorState
import com.example.echo_panda_mobile.presentation.components.SquareArtCard
import com.example.echo_panda_mobile.presentation.navigation.Routes
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistMusicViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistMusicViewModelFactory
import java.util.Locale

private enum class CatalogSortFilter {
    DEFAULT,
    MOST_PLAYED,
}

private enum class CatalogTypeFilter {
    SONG,
    ALBUM,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistMusicScreen(
    onNavigate: (String) -> Unit,
    viewModel: ArtistMusicViewModel = viewModel(factory = ArtistMusicViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val albumTracks by viewModel.albumTracks.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var sortFilter by remember { mutableStateOf(CatalogSortFilter.DEFAULT) }
    var typeFilter by remember { mutableStateOf(CatalogTypeFilter.SONG) }

    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(EchoPandaColors.AccentBlue.copy(alpha = 0.1f), Color.Transparent)
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "My Catalog",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(
                        onClick = { onNavigate(Routes.ARTIST_UPLOAD) },
                        modifier = Modifier.background(EchoPandaColors.AccentBlue, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.Black)
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            if (typeFilter == CatalogTypeFilter.SONG) "Search your songs..." else "Search your albums...",
                            color = Color.Gray,
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EchoPandaColors.AccentBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color(0xFF121A26),
                        unfocusedContainerColor = Color(0xFF121A26)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CatalogFilterChip(
                        label = "Latest",
                        selected = sortFilter == CatalogSortFilter.DEFAULT,
                        onClick = { sortFilter = CatalogSortFilter.DEFAULT },
                    )
                    CatalogFilterChip(
                        label = "Most Played",
                        selected = sortFilter == CatalogSortFilter.MOST_PLAYED,
                        onClick = { sortFilter = CatalogSortFilter.MOST_PLAYED },
                    )
                    Spacer(Modifier.weight(1f))
                    CatalogTypeDropdown(
                        selected = typeFilter,
                        onSelected = { typeFilter = it },
                    )
                }
            }
        },
        bottomBar = { ArtistBottomBar(Routes.ARTIST_MY_MUSIC, onNavigate) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadMyMusic(isRefresh = true) },
            modifier = Modifier.padding(padding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is ArtistMusicViewModel.MusicUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = EchoPandaColors.AccentBlue
                        )
                    }
                    is ArtistMusicViewModel.MusicUiState.Success -> {
                        if (typeFilter == CatalogTypeFilter.ALBUM) {
                            val trackCountByAlbumId = songCountByAlbumId(state.songs)
                            val filteredAlbums = state.albums
                                .filter { album ->
                                    searchQuery.isBlank() ||
                                        album.title.contains(searchQuery, ignoreCase = true)
                                }
                                .let { albums ->
                                    if (sortFilter == CatalogSortFilter.MOST_PLAYED) {
                                        albums.sortedByDescending { album ->
                                            trackCountByAlbumId[album.id] ?: album.songsCount ?: 0
                                        }
                                    } else {
                                        albums
                                    }
                                }

                            if (filteredAlbums.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    EmptyAlbumCatalog(onCreateAlbum = { onNavigate(Routes.ARTIST_CREATE_ALBUM) })
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    item {
                                        Text(
                                            "${filteredAlbums.size} Album${if (filteredAlbums.size == 1) "" else "s"} Found",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    items(filteredAlbums, key = { it.id }) { album ->
                                        ArtistAlbumRow(
                                            album = album,
                                            trackCount = trackCountByAlbumId[album.id]
                                                ?: album.songsCount
                                                ?: 0,
                                            highlightTrackCount = sortFilter == CatalogSortFilter.MOST_PLAYED,
                                            onClick = { viewModel.loadAlbumTracks(album) },
                                        )
                                    }
                                }
                            }
                        } else {
                            val filteredSongs = state.songs
                                .filter { song ->
                                    searchQuery.isBlank() ||
                                        song.title?.contains(searchQuery, ignoreCase = true) == true
                                }
                                .let { songs ->
                                    if (sortFilter == CatalogSortFilter.MOST_PLAYED) {
                                        songs.sortedByDescending { it.playCount ?: 0 }
                                    } else {
                                        songs
                                    }
                                }

                            if (filteredSongs.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    EmptyCatalog(onUpload = { onNavigate(Routes.ARTIST_UPLOAD) })
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    item {
                                        Text(
                                            "${filteredSongs.size} Songs Found",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    items(filteredSongs, key = { it.id ?: it.title.hashCode() }) { song ->
                                        ArtistTrackRow(
                                            song = song,
                                            showPlayCount = sortFilter == CatalogSortFilter.MOST_PLAYED,
                                            onDelete = { viewModel.deleteSong(song.id.toString()) },
                                            onEdit = {
                                                onNavigate(Routes.ARTIST_EDIT_SONG.replace("{trackId}", song.id.toString()))
                                            },
                                            onClick = {
                                                onNavigate(Routes.ARTIST_PLAYER.replace("{trackId}", song.id.toString()))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is ArtistMusicViewModel.MusicUiState.Error -> {
                        ErrorState(message = state.message) {
                            viewModel.loadMyMusic()
                        }
                    }
                }
            }
        }
    }

    AlbumTracksBottomSheet(
        visible = albumTracks.album != null,
        albumTitle = albumTracks.album?.title ?: "Album",
        isLoading = albumTracks.isLoading,
        error = albumTracks.error,
        songs = albumTracks.songs,
        onDismiss = { viewModel.dismissAlbumTracks() },
        onRetry = { albumTracks.album?.let { viewModel.loadAlbumTracks(it) } },
        onSongClick = { songId ->
            viewModel.dismissAlbumTracks()
            onNavigate(Routes.ARTIST_PLAYER.replace("{trackId}", songId))
        },
        onEditSong = { songId ->
            viewModel.dismissAlbumTracks()
            onNavigate(Routes.ARTIST_EDIT_SONG.replace("{trackId}", songId))
        },
    )
}

@Composable
private fun CatalogTypeDropdown(
    selected: CatalogTypeFilter,
    onSelected: (CatalogTypeFilter) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (selected) {
        CatalogTypeFilter.SONG -> "Songs"
        CatalogTypeFilter.ALBUM -> "Albums"
    }

    Box {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, fontSize = 12.sp)
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EchoPandaColors.AccentBlue.copy(alpha = 0.2f),
                selectedLabelColor = EchoPandaColors.AccentBlue,
                containerColor = Color(0xFF121A26),
                labelColor = Color.White.copy(alpha = 0.7f),
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = true,
                borderColor = Color.White.copy(alpha = 0.15f),
                selectedBorderColor = EchoPandaColors.AccentBlue,
            ),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF1E2736)),
        ) {
            DropdownMenuItem(
                text = { Text("Songs", color = Color.White) },
                leadingIcon = {
                    Icon(Icons.Default.MusicNote, null, tint = EchoPandaColors.AccentBlue)
                },
                onClick = {
                    onSelected(CatalogTypeFilter.SONG)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Albums", color = Color.White) },
                leadingIcon = {
                    Icon(Icons.Default.Album, null, tint = EchoPandaColors.AccentBlue)
                },
                onClick = {
                    onSelected(CatalogTypeFilter.ALBUM)
                    expanded = false
                },
            )
        }
    }
}

@Composable
private fun CatalogFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = EchoPandaColors.AccentBlue.copy(alpha = 0.2f),
            selectedLabelColor = EchoPandaColors.AccentBlue,
            containerColor = Color(0xFF121A26),
            labelColor = Color.White.copy(alpha = 0.7f),
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color.White.copy(alpha = 0.15f),
            selectedBorderColor = EchoPandaColors.AccentBlue,
        ),
    )
}

@Composable
private fun EmptyAlbumCatalog(onCreateAlbum: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Album,
            null,
            modifier = Modifier.size(80.dp),
            tint = Color.White.copy(alpha = 0.1f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "No albums found",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Create an album to organize your music catalog.",
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onCreateAlbum,
            colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, null, tint = Color.Black)
            Spacer(Modifier.width(8.dp))
            Text("Create your first album", color = Color.Black)
        }
    }
}

@Composable
private fun EmptyCatalog(onUpload: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.MusicOff,
            null,
            modifier = Modifier.size(80.dp),
            tint = Color.White.copy(alpha = 0.1f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "No music found",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Start sharing your creativity with the world.",
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onUpload,
            colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CloudUpload, null, tint = Color.Black)
            Spacer(Modifier.width(8.dp))
            Text("Upload your first song", color = Color.Black)
        }
    }
}

private fun songCountByAlbumId(songs: List<SongDto>): Map<Int, Int> {
    return songs
        .mapNotNull { song ->
            val albumId = song.albumId ?: song.album?.id
            albumId?.let { id -> id to song }
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, albumSongs) -> albumSongs.size }
}

@Composable
private fun ArtistAlbumRow(
    album: AlbumDto,
    trackCount: Int,
    highlightTrackCount: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        color = Color(0xFF121A26),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SquareArtCard(
                colors = listOf(Color(0xFF2C2C3A), Color(0xFF1A1A26)),
                imageUrl = album.getDisplayCoverUrl(),
                size = 64.dp,
                cornerRadius = 12.dp,
                onClick = onClick
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    album.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Released ${album.releaseDate ?: "N/A"}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    Text(" • ", color = Color.White.copy(alpha = 0.2f))
                    Text(
                        "$trackCount Track${if (trackCount == 1) "" else "s"}",
                        color = if (highlightTrackCount) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = if (highlightTrackCount) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
private fun ArtistTrackRow(
    song: SongDto,
    showPlayCount: Boolean = false,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Surface(
        color = Color(0xFF121A26),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Song Cover Thumbnail
            SquareArtCard(
                colors = listOf(Color(0xFF2C2C3A), Color(0xFF1A1A26)),
                imageUrl = song.coverUrl, // Already resolved by ArtistRepository
                size = 64.dp,
                cornerRadius = 12.dp,
                onClick = onClick
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    song.title ?: "Unknown",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${(song.durationSeconds ?: 0) / 60}:${String.format("%02d", (song.durationSeconds ?: 0) % 60)}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    Text(" • ", color = Color.White.copy(alpha = 0.2f))
                    Text(
                        song.artist?.name ?: "Unknown Artist",
                        color = EchoPandaColors.AccentBlue.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (showPlayCount) {
                        Text(" • ", color = Color.White.copy(alpha = 0.2f))
                        Text(
                            formatCatalogPlayCount(song.playCount ?: 0),
                            color = Color(0xFF4ADE80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
          
                
                Spacer(Modifier.width(8.dp))
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.White.copy(alpha = 0.4f))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF1E2736))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Details", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.Edit, null, tint = EchoPandaColors.AccentBlue) },
                            onClick = { 
                                showMenu = false
                                onEdit()
                            }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        DropdownMenuItem(
                            text = { Text("Delete Song", color = EchoPandaColors.ErrorRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = EchoPandaColors.ErrorRed) },
                            onClick = { 
                                onDelete()
                                showMenu = false 
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatCatalogPlayCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${String.format(Locale.getDefault(), "%.1f", count / 1_000_000f)}M plays"
        count >= 1_000 -> "${String.format(Locale.getDefault(), "%.1f", count / 1_000f)}K plays"
        count > 0 -> "$count plays"
        else -> "0 plays"
    }
}
