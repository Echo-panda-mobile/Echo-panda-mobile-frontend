package com.example.echo_panda_mobile.presentation.views.user.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.presentation.components.ArtPlaceholder
import com.example.echo_panda_mobile.presentation.components.EchoPandaBottomBar
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewsmodel.LibraryItem
import com.example.echo_panda_mobile.presentation.viewsmodel.LibraryViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.LibraryState

@Composable
fun LibraryScreen(
    selectedNav: Int = 3,
    onNavSelect: (Int) -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    viewModel: LibraryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF05070D),
        topBar = {
            LibraryTopBar()
        },
        bottomBar = { EchoPandaBottomBar(selectedNav, onNavSelect) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                FilterChipsRow(
                    selectedFilter = state.selectedFilter,
                    onFilterSelect = { viewModel.setFilter(it) }
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                LibraryActionButton(
                    icon = Icons.Default.Add,
                    text = "Add New Playlist",
                    gradient = Brush.verticalGradient(listOf(Color(0xFF00D9FF), Color(0xFF00A3C2))),
                    onClick = { /* TODO */ }
                )
                Spacer(Modifier.height(16.dp))
                LibraryActionButton(
                    icon = Icons.Default.FavoriteBorder,
                    text = "Your Liked Songs",
                    gradient = Brush.verticalGradient(listOf(Color(0xFF00D9FF), Color(0xFF00A3C2))),
                    onClick = onNavigateToFavorites
                )
                Spacer(Modifier.height(32.dp))
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SwapVert,
                        contentDescription = null,
                        tint = EchoPandaColors.AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    val sectionTitle = if (state.selectedFilter == "All" || state.selectedFilter == "Recently") "Recently played" else state.selectedFilter
                    Text(
                        text = sectionTitle,
                        color = EchoPandaColors.AccentBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            items(state.filteredItems) { item ->
                LibraryListItem(item = item)
                Spacer(Modifier.height(16.dp))
            }
            
            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun LibraryTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = EchoPandaColors.AccentBlue,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = EchoPandaColors.AccentBlue
            )
        }
        IconButton(onClick = { /* TODO */ }) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
        }
    }
}

@Composable
fun FilterChipsRow(
    selectedFilter: String,
    onFilterSelect: (String) -> Unit
) {
    val filters = listOf("Recently", "Playlists", "Artists", "Albums")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) EchoPandaColors.AccentBlue else Color.White.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable { onFilterSelect(filter) },
                color = if (isSelected) EchoPandaColors.AccentBlue.copy(alpha = 0.2f) else Color.Transparent
            ) {
                Text(
                    text = filter,
                    color = if (isSelected) EchoPandaColors.AccentBlue else Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun LibraryActionButton(
    icon: ImageVector,
    text: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.width(20.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LibraryListItem(
    item: LibraryItem,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (item) {
            is LibraryItem.ArtistItem -> {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                ) {
                    ArtPlaceholder(colors = item.artist.placeholderColors, modifier = Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = item.artist.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            is LibraryItem.PlaylistItem -> {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    ArtPlaceholder(colors = item.playlist.placeholderColors, modifier = Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = item.playlist.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val subtitle = item.subtitle ?: "${item.trackCount ?: 0} songs"
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }
            is LibraryItem.AlbumItem -> {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    ArtPlaceholder(colors = item.album.placeholderColors, modifier = Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = item.album.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.album.artist,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
