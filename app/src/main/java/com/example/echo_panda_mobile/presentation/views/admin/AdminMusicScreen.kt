package com.example.echo_panda_mobile.presentation.views.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.echo_panda_mobile.presentation.components.AdminBottomBar
import com.example.echo_panda_mobile.presentation.components.AdminTopBar

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
    onBack: () -> Unit
) {
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

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedFilter) {
                "Song" -> SongManagementSection(onNavigateToSongDetail)
                "Album" -> AlbumManagementSection(onNavigateToAlbumDetail)
            }
        }
    }
}

@Composable
fun SongManagementSection(onNavigateToDetail: (String) -> Unit) {
    var listSearchQuery by remember { mutableStateOf("") }

    val mockSongs = listOf(
        AdminSongRecord("Birds of Feather", "Billie Eilish", "Happier Than Ever", "3:50", "1/10/2026", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_p_Q5F5W4zX2N8E4_6Xz7R6U4z_5y_8z9w&s"),
        AdminSongRecord("Like Jennie", "Jennie Kim", "Rubby", "3:02", "1/10/2026", ""),
        AdminSongRecord("Happier Than Ever", "Billie Eilish", "Happier Than Ever", "5:15", "1/10/2026", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_p_Q5F5W4zX2N8E4_6Xz7R6U4z_5y_8z9w&s"),
        AdminSongRecord("You Belong With Me", "Taylor Swift", "Speak Now", "5:45", "1/10/2026", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_p_Q5F5W4zX2N8E4_6Xz7R6U4z_5y_8z9w&s"),
        AdminSongRecord("Enchanted", "Taylor Swift", "Speak Now", "5:45", "1/10/2026", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_p_Q5F5W4zX2N8E4_6Xz7R6U4z_5y_8z9w&s"),
        AdminSongRecord("Happier", "Olivia Rodrigo", "SOUR", "2:55", "1/9/2026", ""),
        AdminSongRecord("Ambient", "laufey", "lofi", "3:20", "1/9/2026", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_p_Q5F5W4zX2N8E4_6Xz7R6U4z_5y_8z9w&s"),
        AdminSongRecord("dim the light", "Ahjay Stelino", "ChillOut", "3:20", "1/9/2026", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_p_Q5F5W4zX2N8E4_6Xz7R6U4z_5y_8z9w&s")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Songs ")
                    withStyle(style = SpanStyle(color = AccentPurple)) {
                        append("Management")
                    }
                },
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = listSearchQuery,
            onValueChange = { listSearchQuery = it },
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
                    items(mockSongs) { song ->
                        SongRow(song, onNavigateToDetail)
                        if (song != mockSongs.last()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.03f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumManagementSection(onNavigateToDetail: (String) -> Unit) {
    var listSearchQuery by remember { mutableStateOf("") }

    val mockAlbums = listOf(
        AdminAlbumRecord("Happier Than Ever", "Billie Eilish", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_p_Q5F5W4zX2N8E4_6Xz7R6U4z_5y_8z9w&s"),
        AdminAlbumRecord("Rubby", "Jennie Kim", ""),
        AdminAlbumRecord("Speak Now", "Taylor Swift", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_p_Q5F5W4zX2N8E4_6Xz7R6U4z_5y_8z9w&s"),
        AdminAlbumRecord("SOUR", "Olivia Rodrigo", ""),
        AdminAlbumRecord("lofi", "laufey", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_p_Q5F5W4zX2N8E4_6Xz7R6U4z_5y_8z9w&s"),
        AdminAlbumRecord("ChillOut", "Ahjay Stelino", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_p_Q5F5W4zX2N8E4_6Xz7R6U4z_5y_8z9w&s")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Albums ")
                    withStyle(style = SpanStyle(color = AccentPurple)) {
                        append("Management")
                    }
                },
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = listSearchQuery,
            onValueChange = { listSearchQuery = it },
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
                    items(mockAlbums) { album ->
                        AlbumRow(album, onNavigateToDetail)
                        if (album != mockAlbums.last()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.03f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SongRow(song: AdminSongRecord, onNavigateToDetail: (String) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(3f)) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                if (song.imageUrl.isNotEmpty()) {
                    AsyncImage(model = song.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = TextMuted, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = AccentPurple.copy(alpha = 0.6f), modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(song.artist, color = TextMuted, fontSize = 11.sp)
            }
        }
        Row(modifier = Modifier.weight(2f), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Album, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(song.album, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        }
        Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.CenterEnd) {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = TextMuted, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(CardBg)) {
                DropdownMenuItem(text = { Text("View Detail", color = Color.White, fontSize = 14.sp) }, onClick = { showMenu = false; onNavigateToDetail(song.title) })
                DropdownMenuItem(text = { Text("Ban this song?", color = Color(0xFFFF5252), fontSize = 14.sp) }, onClick = { showMenu = false })
            }
        }
    }
}

@Composable
fun AlbumRow(album: AdminAlbumRecord, onNavigateToDetail: (String) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        // ALBUM column
        Column(modifier = Modifier.weight(3f)) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                if (album.imageUrl.isNotEmpty()) {
                    AsyncImage(model = album.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Album, contentDescription = null, tint = TextMuted, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(album.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        // ARTIST column
        Row(modifier = Modifier.weight(2f), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(album.artist, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        }

        // ACTIONS column
        Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.CenterEnd) {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = TextMuted, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(CardBg)) {
                DropdownMenuItem(text = { Text("View Detail", color = Color.White, fontSize = 14.sp) }, onClick = { showMenu = false; onNavigateToDetail(album.title) })
                DropdownMenuItem(text = { Text("Ban this album?", color = Color(0xFFFF5252), fontSize = 14.sp) }, onClick = { showMenu = false })
            }
        }
    }
}

@Composable
private fun MusicHeaderText(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(text = text, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = modifier, textAlign = textAlign)
}

data class AdminSongRecord(val title: String, val artist: String, val album: String, val duration: String, val createdDate: String, val imageUrl: String)
data class AdminAlbumRecord(val title: String, val artist: String, val imageUrl: String)
