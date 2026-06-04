package com.example.echo_panda_mobile.presentation.views.user.discover

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.presentation.components.*
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.theme.LocalAppLanguage
import com.example.echo_panda_mobile.presentation.viewmodel.DiscoverViewModel
import com.example.echo_panda_mobile.presentation.viewmodel.GlobalPlayerViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    selectedNav: Int = 1,
    onNavSelect: (Int) -> Unit = {},
    onNavigateToArtist: (String) -> Unit = {},
    onNavigateToAlbum: (String) -> Unit = {},
    onNavigateToSong: (String, Long?) -> Unit = { _, _ -> },
    onViewAllArtists: () -> Unit = {},
    onViewAllSongs: (String) -> Unit = {},
    viewModel: DiscoverViewModel = viewModel(),
    globalPlayerViewModel: GlobalPlayerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val ptrState = rememberPullToRefreshState()

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) ?: ""
            if (spokenText.isNotEmpty()) {
                if (!state.isSearchActive) viewModel.toggleSearch()
                viewModel.onSearchQueryChange(spokenText)
            }
        }
    }

    val onVoiceSearch = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search...")
        }
        speechLauncher.launch(intent)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05070D))
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                DiscoverTopBar(
                    query = state.searchQuery,
                    isSearchActive = state.isSearchActive,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onToggleSearch = viewModel::toggleSearch,
                    onVoiceSearch = onVoiceSearch
                )
            },
            bottomBar = { EchoPandaBottomBar(selectedNav, onNavSelect) }
        ) { padding ->
            PullToRefreshBox(
                state = ptrState,
                isRefreshing = state.isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = ptrState,
                        isRefreshing = state.isLoading,
                        containerColor = Color(0xFF1A1A26),
                        color = EchoPandaColors.AccentBlue,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            ) {
                if (state.isLoading && state.genres.isEmpty() && state.moodPlaylists.isEmpty() && state.newReleases.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EchoPandaColors.AccentBlue)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(Modifier.height(16.dp))

                        if (state.isSearchActive && state.searchResults.isNotEmpty()) {
                            SectionHeader(
                                fullTitle = "Search Results: Albums",
                                highlightPart = "Albums",
                                highlightColor = EchoPandaColors.AccentBlue,
                                onViewAll = null
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                state.searchResults.forEach { album ->
                                    AlbumCard(
                                        album = album,
                                        onClick = { onNavigateToAlbum(album.id) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(32.dp))
                        }

                        // ── Music Genres ──────────────────────────────────────────────
                        if (state.genres.isNotEmpty()) {
                            SectionHeader(
                                fullTitle = "Music Genres",
                                highlightPart = "Genres",
                                highlightColor = EchoPandaColors.AccentBlue,
                                onViewAll = { onNavSelect(2) }
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                state.genres.forEach { genre ->
                                    LabeledArtCard(
                                        title = genre.name,
                                        subLabel = genre.subLabel,
                                        colors = genre.placeholderColors,
                                        imageUrl = genre.imageUrl,
                                        onClick = { onNavSelect(2) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(32.dp))
                        }

                        // ── Mood Playlist ─────────────────────────────────────────────
                        if (state.moodPlaylists.isNotEmpty()) {
                            SectionHeader(
                                fullTitle = "Mood Playlist",
                                highlightPart = "Playlist",
                                highlightColor = EchoPandaColors.AccentBlue,
                                onViewAll = { onNavSelect(3) }
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                state.moodPlaylists.forEach { playlist ->
                                    LabeledArtCard(
                                        title = playlist.name,
                                        subLabel = playlist.subLabel,
                                        colors = playlist.placeholderColors,
                                        imageUrl = playlist.imageUrl,
                                        onClick = { onNavSelect(3) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(32.dp))
                        }

                        // ── New Release Songs ─────────────────────────────────────────
                        if (state.newReleases.isNotEmpty()) {
                            SectionHeader(
                                fullTitle = "New Release Songs",
                                highlightPart = "Songs",
                                highlightColor = EchoPandaColors.AccentBlue,
                                onViewAll = { onViewAllSongs("New Releases") }
                            )
                            Spacer(Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                state.newReleases.take(4).forEachIndexed { index, track ->
                                    SongCardHorizontal(
                                        track = track,
                                        onClick = { 
                                            globalPlayerViewModel.setQueue(state.newReleases, index)
                                            onNavigateToSong(track.id, track.resumePositionMs) 
                                        },
                                        onFavoriteClick = { viewModel.toggleFavorite(track) }
                                    )
                                }
                            }
                        }

                        // ── Popular Artists ───────────────────────────────────────────
                        if (!state.isSearchActive && state.popularArtists.isNotEmpty()) {
                            SectionHeader(
                                fullTitle = "Popular Artists",
                                highlightPart = "Artists",
                                highlightColor = EchoPandaColors.AccentBlue,
                                onViewAll = onViewAllArtists
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                state.popularArtists.forEach { artist ->
                                    ArtistCircleCard(
                                        artist = artist,
                                        onClick = { onNavigateToArtist(artist.id) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(32.dp))
                        }

                        // ── Most Played Songs ──────────────────────────────────────────
                        if (!state.isSearchActive && state.mostPlayedSongs.isNotEmpty()) {
                            SectionHeader(
                                fullTitle = "Most Played Songs",
                                highlightPart = "Songs",
                                highlightColor = EchoPandaColors.AccentBlue,
                                onViewAll = { onViewAllSongs("Most Played") }
                            )
                            Spacer(Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                state.mostPlayedSongs.take(4).forEachIndexed { index, track ->
                                    SongCardHorizontal(
                                        track = track,
                                        onClick = { 
                                            globalPlayerViewModel.setQueue(state.mostPlayedSongs, index)
                                            onNavigateToSong(track.id, track.resumePositionMs) 
                                        },
                                        onFavoriteClick = { viewModel.toggleFavorite(track) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscoverTopBar(
    query: String,
    isSearchActive: Boolean,
    onQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onVoiceSearch: () -> Unit
) {
    if (isSearchActive) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleSearch) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                placeholder = { Text("Search songs, artists, albums...", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1A1F2E),
                    unfocusedContainerColor = Color(0xFF1A1F2E),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                trailingIcon = {
                    Row {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                        IconButton(onClick = onVoiceSearch) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = Color.Gray)
                        }
                    }
                }
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleSearch) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = EchoPandaColors.AccentBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Text(
                text = "Discover",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onVoiceSearch) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Voice Search",
                    tint = EchoPandaColors.AccentBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
