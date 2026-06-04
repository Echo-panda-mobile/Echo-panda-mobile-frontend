package com.example.echo_panda_mobile.presentation.views.user.discover

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.presentation.components.EchoPandaBottomBar
import com.example.echo_panda_mobile.presentation.components.SongCardHorizontal
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewmodel.DiscoverViewModel
import com.example.echo_panda_mobile.presentation.viewmodel.GlobalPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllSongsScreen(
    type: String = "Songs",
    selectedNav: Int = 1,
    onNavSelect: (Int) -> Unit = {},
    onBack: () -> Unit = {},
    onNavigateToPlayer: (String, Long?) -> Unit = { _, _ -> },
    viewModel: DiscoverViewModel = viewModel(),
    globalPlayerViewModel: GlobalPlayerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    
    val tracks = when (type) {
        "New Releases" -> state.newReleases
        "Most Played" -> state.mostPlayedSongs
        else -> state.newReleases + state.mostPlayedSongs
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF05070D),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        type,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = { EchoPandaBottomBar(selectedNav, onNavSelect) }
    ) { padding ->
        if (state.isLoading && tracks.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EchoPandaColors.AccentBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(tracks) { index, track ->
                    SongCardHorizontal(
                        track = track,
                        onClick = {
                            globalPlayerViewModel.setQueue(tracks, index)
                            onNavigateToPlayer(track.id, track.resumePositionMs)
                        },
                        onFavoriteClick = { viewModel.toggleFavorite(track) }
                    )
                }
            }
        }
    }
}
