package com.example.echo_panda_mobile.presentation.views.user.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.presentation.components.*
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.theme.LocalAppLanguage
import com.example.echo_panda_mobile.presentation.theme.LocalIsDarkTheme
import com.example.echo_panda_mobile.presentation.viewmodel.DiscoverViewModel

@Composable
fun DiscoverScreen(
    selectedNav: Int = 1,
    onNavSelect: (Int) -> Unit = {},
    viewModel: DiscoverViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val language = LocalAppLanguage.current
    
    // Fixed: Use LocalIsDarkTheme instead of isSystemInDarkTheme to respect app settings
    val isDark = LocalIsDarkTheme.current
    val bgStart = if (isDark) EchoPandaColors.BgDarkStart else EchoPandaColors.BgLightStart
    val bgEnd = if (isDark) EchoPandaColors.BgDarkEnd else EchoPandaColors.BgLightEnd

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgStart, bgEnd)))
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                DiscoverTopBar(
                    query    = state.searchQuery,
                    onChange = viewModel::onSearchQueryChange
                )
            },
            bottomBar = { EchoPandaBottomBar(selectedNav, onNavSelect) }
        ) { padding ->
            if (state.isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(12.dp))

                // ── Music Genres ──────────────────────────────────────────────
                val musicGenresStr = AppStrings.getString("music_genres", language)
                SectionHeader(
                    fullTitle = musicGenresStr,
                    highlightPart = musicGenresStr.substringAfter(" "),
                    highlightColor = MaterialTheme.colorScheme.primary,
                    onViewAll = {}
                )
                Spacer(Modifier.height(12.dp))
                HorizontalLabeledRow(
                    items    = state.genres.map { Triple(it.name, it.subLabel, it.placeholderColors) }
                )

                Spacer(Modifier.height(24.dp))

                // ── Mood Playlist ─────────────────────────────────────────────
                val moodPlaylistStr = AppStrings.getString("mood_playlist", language)
                SectionHeader(
                    fullTitle = moodPlaylistStr,
                    highlightPart = moodPlaylistStr.substringAfter(" "),
                    highlightColor = MaterialTheme.colorScheme.primary,
                    onViewAll = {}
                )
                Spacer(Modifier.height(12.dp))
                HorizontalLabeledRow(
                    items = state.moodPlaylists.map { Triple(it.name, it.subLabel, it.placeholderColors) }
                )

                Spacer(Modifier.height(24.dp))

                // ── New Release Songs ─────────────────────────────────────────
                val newReleaseStr = AppStrings.getString("new_release_songs", language)
                SectionHeader(
                    fullTitle = newReleaseStr,
                    highlightPart = newReleaseStr.substringAfterLast(" "),
                    highlightColor = MaterialTheme.colorScheme.primary,
                    onViewAll = {}
                )
                Spacer(Modifier.height(12.dp))
                HorizontalLabeledRow(
                    items = state.newReleases.map { Triple(it.title, it.artist, it.placeholderColors) }
                )

                Spacer(Modifier.height(24.dp))

                // ── Popular Artists ───────────────────────────────────────────
                val popularArtistsStr = AppStrings.getString("popular_artists", language)
                SectionHeader(
                    fullTitle = popularArtistsStr,
                    highlightPart = popularArtistsStr.substringAfter(" "),
                    highlightColor = MaterialTheme.colorScheme.primary,
                    onViewAll = {}
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.popularArtists.forEach { artist ->
                        ArtistCircleCard(artist = artist)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ─── Top Bar with live search ─────────────────────────────────────────────────
@Composable
private fun DiscoverTopBar(query: String, onChange: (String) -> Unit) {
    val language = LocalAppLanguage.current
    Surface(
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(26.dp)
                    .align(Alignment.CenterStart)
                    .clickable { }
            )
            Text(
                AppStrings.getString("discover", language),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                Icons.Default.Menu,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(26.dp)
                    .align(Alignment.CenterEnd)
                    .clickable { }
            )
        }
    }
}

// ─── Generic horizontal scrollable labeled-card row ──────────────────────────
@Composable
private fun HorizontalLabeledRow(items: List<Triple<String, String, List<Color>>>) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { (title, sub, colors) ->
            LabeledArtCard(title = title, subLabel = sub, colors = colors)
        }
    }
}
