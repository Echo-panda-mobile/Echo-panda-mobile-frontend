package com.example.echo_panda_mobile.presentation.views.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import coil.compose.AsyncImage
import com.example.echo_panda_mobile.data.remote.AlbumDto
import com.example.echo_panda_mobile.presentation.components.AlbumTracksBottomSheet
import com.example.echo_panda_mobile.presentation.components.ErrorState
import com.example.echo_panda_mobile.presentation.navigation.Routes
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistAlbumViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistAlbumViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistAlbumManagementScreen(
    onBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigate: (String) -> Unit = {},
    viewModel: ArtistAlbumViewModel = viewModel(factory = ArtistAlbumViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val albumTracks by viewModel.albumTracks.collectAsState()

    // Refresh when returning to this screen
    LaunchedEffect(Unit) {
        viewModel.loadMyAlbums()
    }

    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "My Albums", 
                        color = Color.White, 
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToCreate,
                        modifier = Modifier.padding(end = 8.dp).background(EchoPandaColors.AccentBlue, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadMyAlbums(isRefresh = true) },
            modifier = Modifier.padding(padding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background subtle gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(EchoPandaColors.AccentBlue.copy(alpha = 0.05f), Color.Transparent)
                            )
                        )
                )
                
                when (val state = uiState) {
                    is ArtistAlbumViewModel.AlbumUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = EchoPandaColors.AccentBlue
                        )
                    }
                    is ArtistAlbumViewModel.AlbumUiState.Success -> {
                        if (state.albums.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                EmptyAlbumState(onNavigateToCreate)
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.albums, key = { it.id }) { album ->
                                    AlbumManageRow(
                                        album = album,
                                        onManageTracks = { viewModel.loadAlbumTracks(album) },
                                        onDelete = { viewModel.deleteAlbum(album.id.toString()) }
                                    )
                                }
                            }
                        }
                    }
                    is ArtistAlbumViewModel.AlbumUiState.Error -> {
                        ErrorState(message = state.message) {
                            viewModel.loadMyAlbums()
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
private fun EmptyAlbumState(onNavigateToCreate: () -> Unit) {
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
            "No albums yet",
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
            onClick = onNavigateToCreate,
            colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Create Your First Album", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AlbumManageRow(
    album: AlbumDto,
    onManageTracks: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF121A26),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = album.getDisplayCoverUrl()
                
                LaunchedEffect(imageUrl) {
                    android.util.Log.d("AlbumManageRow", "Album ID: ${album.id}")
                    android.util.Log.d("AlbumManageRow", "Album Name: ${album.title}")
                    android.util.Log.d("AlbumManageRow", "cover_url: ${album.coverUrl}")
                    android.util.Log.d("AlbumManageRow", "cover_image: ${album.coverImage}")
                    android.util.Log.d("AlbumManageRow", "Final image URL: $imageUrl")
                }

                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = androidx.compose.ui.graphics.painter.ColorPainter(Color.DarkGray),
                        onLoading = { android.util.Log.d("AlbumManageRow", "Loading image: $imageUrl") },
                        onSuccess = { android.util.Log.d("AlbumManageRow", "Successfully loaded image: $imageUrl") },
                        onError = { 
                            android.util.Log.e("AlbumManageRow", "Failed to load image: $imageUrl")
                            android.util.Log.e("AlbumManageRow", "Error: ${it.result.throwable.message}")
                        }
                    )
                } else {
                    Icon(Icons.Default.Album, null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(32.dp))
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    album.title, 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Released ${album.releaseDate ?: "N/A"}", 
                    color = Color.White.copy(alpha = 0.5f), 
                    fontSize = 12.sp
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when(album.releaseStatus?.lowercase()) {
                        "published" -> Color(0xFF4ADE80)
                        "draft" -> Color(0xFFFBBC05)
                        "pending_review" -> Color(0xFF60A5FA)
                        "rejected" -> Color(0xFFF87171)
                        else -> Color(0xFF4ADE80)
                    }
                    Badge(
                        containerColor = statusColor.copy(alpha = 0.1f),
                        contentColor = statusColor
                    ) {
                        Text(
                            album.releaseStatus?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "Active",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    val trackLabel = album.songsCount?.let { "$it Track${if (it == 1) "" else "s"}" } ?: "Tracks"
                    Text(trackLabel, color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
                }
            }
            
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.White.copy(alpha = 0.5f))
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF1E2736))
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Album", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = EchoPandaColors.AccentBlue) },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Manage Tracks", color = Color.White) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, null, tint = Color(0xFFA78BFA)) },
                        onClick = {
                            showMenu = false
                            onManageTracks()
                        }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    DropdownMenuItem(
                        text = { Text("Delete Album", color = EchoPandaColors.ErrorRed) },
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
