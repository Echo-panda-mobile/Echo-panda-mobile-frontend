package com.example.echo_panda_mobile.presentation.views.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.echo_panda_mobile.data.model.Album
import com.example.echo_panda_mobile.data.model.Track
import com.example.echo_panda_mobile.presentation.components.AdminBottomBar
import com.example.echo_panda_mobile.presentation.components.AdminTopBar
import com.example.echo_panda_mobile.presentation.viewsmodel.AdminMusicViewModel

private val BgDark = Color(0xFF05070D)
private val CardBg = Color(0xFF161C24)
private val AccentPurple = Color(0xFFFF00FF)
private val TextMuted = Color.White.copy(alpha = 0.5f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMusicScreen(
    selectedNav: Int,
    onNavSelect: (Int) -> Unit,
    onNavigateToSongDetail: (String) -> Unit,
    onNavigateToAlbumDetail: (String) -> Unit,
    onProfileClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: AdminMusicViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Song") }
    val filters = listOf("Song", "Album")

    Scaffold(
        topBar = {
            AdminTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onProfileClick = onProfileClick
            )
        },
        bottomBar = {
            AdminBottomBar(selectedIndex = selectedNav, onSelect = onNavSelect)
        },
        containerColor = BgDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            selectedContainerColor = AccentPurple.copy(alpha = 0.2f),
                            labelColor = TextMuted,
                            selectedLabelColor = AccentPurple
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color.White.copy(alpha = 0.1f),
                            selectedBorderColor = AccentPurple,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.dp,
                            enabled = true,
                            selected = selectedFilter == filter
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentPurple)
                }
            } else {
                uiState.errorMessage?.let { message ->
                    Text(message, color = Color(0xFFFF6B6B), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                when (selectedFilter) {
                    "Song" -> SongManagementSection(
                        songs = uiState.songs.filter {
                            listOf(it.title, it.artist, it.album.orEmpty()).joinToString(" ").contains(searchQuery, ignoreCase = true)
                        },
                        onNavigateToDetail = onNavigateToSongDetail,
                        onApprove = { id -> viewModel.approveSong(id) { if (it == null) viewModel.refresh() } },
                        onHide = { id -> viewModel.hideSong(id) { if (it == null) viewModel.refresh() } },
                        onReport = { id -> viewModel.reportSong(id) { if (it == null) viewModel.refresh() } }
                    )

                    else -> AlbumManagementSection(
                        albums = uiState.albums.filter {
                            listOf(it.title, it.artist).joinToString(" ").contains(searchQuery, ignoreCase = true)
                        },
                        onNavigateToDetail = onNavigateToAlbumDetail,
                        onApprove = { id -> viewModel.approveAlbum(id) { if (it == null) viewModel.refresh() } },
                        onHide = { id -> viewModel.hideAlbum(id) { if (it == null) viewModel.refresh() } },
                        onReport = { id -> viewModel.reportAlbum(id) { if (it == null) viewModel.refresh() } }
                    )
                }
            }
        }
    }
}

@Composable
private fun SongManagementSection(
    songs: List<Track>,
    onNavigateToDetail: (String) -> Unit,
    onApprove: (String) -> Unit,
    onHide: (String) -> Unit,
    onReport: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                append("Songs ")
                withStyle(style = SpanStyle(color = AccentPurple)) { append("Management") }
            },
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp)),
            placeholder = { Text("Search by title or artist...", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.03f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    MusicHeaderText("SONG & ARTIST", Modifier.weight(3f))
                    MusicHeaderText("ALBUM", Modifier.weight(2f))
                    MusicHeaderText("ACTIONS", Modifier.weight(0.8f), textAlign = TextAlign.End)
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
                    items(songs, key = { it.id }) { song ->
                        SongRow(song, onNavigateToDetail, onApprove, onHide, onReport)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.03f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumManagementSection(
    albums: List<Album>,
    onNavigateToDetail: (String) -> Unit,
    onApprove: (String) -> Unit,
    onHide: (String) -> Unit,
    onReport: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                append("Albums ")
                withStyle(style = SpanStyle(color = AccentPurple)) { append("Management") }
            },
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp)),
            placeholder = { Text("Search by album or artist...", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.03f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    MusicHeaderText("ALBUM", Modifier.weight(3f))
                    MusicHeaderText("ARTIST", Modifier.weight(2f))
                    MusicHeaderText("ACTIONS", Modifier.weight(0.8f), textAlign = TextAlign.End)
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
                    items(albums, key = { it.id }) { album ->
                        AlbumRow(album, onNavigateToDetail, onApprove, onHide, onReport)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.03f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SongRow(
    song: Track,
    onNavigateToDetail: (String) -> Unit,
    onApprove: (String) -> Unit,
    onHide: (String) -> Unit,
    onReport: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(3f)) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (!song.imageUrl.isNullOrBlank()) {
                    AsyncImage(model = song.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = TextMuted, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(song.artist, color = TextMuted, fontSize = 11.sp)
        }

        Row(modifier = Modifier.weight(2f), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Album, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(song.album ?: "Unknown", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        }

        Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.CenterEnd) {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = TextMuted, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(CardBg)) {
                DropdownMenuItem(text = { Text("View Detail", color = Color.White, fontSize = 14.sp) }, onClick = { showMenu = false; onNavigateToDetail(song.id) })
                DropdownMenuItem(text = { Text("Approve", color = Color.White, fontSize = 14.sp) }, onClick = { showMenu = false; onApprove(song.id) })
                DropdownMenuItem(text = { Text("Hide", color = Color.White, fontSize = 14.sp) }, onClick = { showMenu = false; onHide(song.id) })
                DropdownMenuItem(text = { Text("Report", color = Color.White, fontSize = 14.sp) }, onClick = { showMenu = false; onReport(song.id) })
            }
        }
    }
}

@Composable
private fun AlbumRow(
    album: Album,
    onNavigateToDetail: (String) -> Unit,
    onApprove: (String) -> Unit,
    onHide: (String) -> Unit,
    onReport: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(3f)) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (!album.imageUrl.isNullOrBlank()) {
                    AsyncImage(model = album.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Album, contentDescription = null, tint = TextMuted, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(album.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Row(modifier = Modifier.weight(2f), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Album, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(album.artist, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        }

        Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.CenterEnd) {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = TextMuted, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(CardBg)) {
                DropdownMenuItem(text = { Text("View Detail", color = Color.White, fontSize = 14.sp) }, onClick = { showMenu = false; onNavigateToDetail(album.id) })
                DropdownMenuItem(text = { Text("Approve", color = Color.White, fontSize = 14.sp) }, onClick = { showMenu = false; onApprove(album.id) })
                DropdownMenuItem(text = { Text("Hide", color = Color.White, fontSize = 14.sp) }, onClick = { showMenu = false; onHide(album.id) })
                DropdownMenuItem(text = { Text("Report", color = Color.White, fontSize = 14.sp) }, onClick = { showMenu = false; onReport(album.id) })
            }
        }
    }
}

@Composable
private fun MusicHeaderText(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(text = text, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = modifier, textAlign = textAlign)
}