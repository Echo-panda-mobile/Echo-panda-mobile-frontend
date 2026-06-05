package com.example.echo_panda_mobile.presentation.views.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.data.remote.SongDto
import com.example.echo_panda_mobile.presentation.components.ArtistBottomBar
import com.example.echo_panda_mobile.presentation.components.ErrorState
import com.example.echo_panda_mobile.presentation.components.SquareArtCard
import com.example.echo_panda_mobile.presentation.navigation.Routes
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistMusicViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistMusicViewModelFactory

@Composable
fun ArtistMusicScreen(
    onNavigate: (String) -> Unit,
    viewModel: ArtistMusicViewModel = viewModel(factory = ArtistMusicViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

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
                    placeholder = { Text("Search your songs...", color = Color.Gray) },
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
            }
        },
        bottomBar = { ArtistBottomBar(Routes.ARTIST_MY_MUSIC, onNavigate) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is ArtistMusicViewModel.MusicUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = EchoPandaColors.AccentBlue
                    )
                }
                is ArtistMusicViewModel.MusicUiState.Success -> {
                    val filteredSongs = if (searchQuery.isBlank()) state.songs 
                                        else state.songs.filter { it.title?.contains(searchQuery, ignoreCase = true) == true }
                    
                    if (filteredSongs.isEmpty()) {
                        EmptyCatalog(onUpload = { onNavigate(Routes.ARTIST_UPLOAD) })
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
                            items(filteredSongs) { song ->
                                ArtistTrackRow(
                                    song = song,
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
                is ArtistMusicViewModel.MusicUiState.Error -> {
                    ErrorState(message = state.message) {
                        viewModel.loadMyMusic()
                    }
                }
            }
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

@Composable
private fun ArtistTrackRow(
    song: SongDto,
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
