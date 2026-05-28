package com.example.echo_panda_mobile.presentation.views.user.album

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.data.model.AppStrings
import com.example.echo_panda_mobile.presentation.components.*
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.theme.LocalAppLanguage
import com.example.echo_panda_mobile.presentation.theme.LocalIsDarkTheme
import com.example.echo_panda_mobile.presentation.viewsmodel.AlbumViewModel
import kotlinx.coroutines.launch

@Composable
fun AlbumScreen(
    selectedNav: Int = 2,
    onNavSelect: (Int) -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    onSearch: (String) -> Unit = {},
    onVoiceSearch: () -> Unit = {},
    viewModel: AlbumViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isDark = LocalIsDarkTheme.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                AlbumTopBar(
                    query = state.searchQuery,
                    isSearchActive = state.isSearchActive,
                    onToggleSearch = { viewModel.toggleSearch() },
                    onChange = viewModel::onSearchQueryChange,
                    onSearch = { onSearch(state.searchQuery) },
                    onVoiceSearch = onVoiceSearch
                )
            },
            bottomBar = { EchoPandaBottomBar(selectedNav, onNavSelect) }
        ) { padding ->
            if (state.isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EchoPandaColors.AccentBlue)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── Filter Chips ──────────────────────────────────────────
                    AlbumFilterChips(
                        selected = state.selectedCategory,
                        onSelect = { viewModel.setCategory(it) }
                    )

                    if (state.isSearchActive || state.selectedCategory != "All") {
                        // ── Filtered/Search Results ──────────────────────────
                        SectionHeader(
                            fullTitle = if (state.isSearchActive) "Search Results" else "${state.selectedCategory} Albums",
                            onViewAll = null
                        )
                        Spacer(Modifier.height(16.dp))
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            state.filteredAlbums.forEach { album ->
                                AlbumListRow(
                                    album = album,
                                    onClick = { onNavigateToDetail(album.id) },
                                    onAddToFavorites = {
                                        scope.launch { snackbarHostState.showSnackbar("Added ${album.title} to Favorites") }
                                    },
                                    onAddToPlaylist = {
                                        scope.launch { snackbarHostState.showSnackbar("Added ${album.title} to Playlist") }
                                    }
                                )
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                            }
                            if (state.filteredAlbums.isEmpty()) {
                                Text(
                                    "No albums found",
                                    color = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // ── Default View ─────────────────────────────────────
                        
                        // ── Featured Hero Section ──────────────────────────────────
                        if (state.topAlbums.isNotEmpty()) {
                            FeaturedAlbumHero(
                                album = state.topAlbums.first(),
                                onClick = { onNavigateToDetail(state.topAlbums.first().id) }
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        // ── Top Picks (Horizontal Scroll) ─────────────────────────────────
                        SectionHeader(
                            fullTitle = "Top Picks", 
                            onViewAll = { viewModel.setCategory("Trending") }
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Exclude the featured one and show the rest of the most played albums
                            state.topAlbums.drop(1).forEach { album ->
                                AlbumCard(
                                    album = album,
                                    onClick = { onNavigateToDetail(album.id) }
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // ── All Albums (List Style) ────────────────────────────────
                        SectionHeader(fullTitle = "All Albums", onViewAll = null)
                        Spacer(Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            state.popularAlbums.forEach { album ->
                                AlbumListRow(
                                    album = album,
                                    onClick = { onNavigateToDetail(album.id) },
                                    onAddToFavorites = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Added ${album.title} to Favorites")
                                        }
                                    },
                                    onAddToPlaylist = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Added ${album.title} to Playlist")
                                        }
                                    }
                                )
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                            }
                        }
                    }

                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun AlbumFilterChips(
    selected: String,
    onSelect: (String) -> Unit
) {
    val filters = listOf("All", "Trending", "Newest", "Pop", "Rock", "Hip-Hop")
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            val isSelected = selected == filter
            Surface(
                modifier = Modifier.clickable { onSelect(filter) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) EchoPandaColors.AccentBlue else Color.White.copy(alpha = 0.08f),
                border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text(
                    text = filter,
                    color = if (isSelected) Color.Black else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun FeaturedAlbumHero(
    album: com.example.echo_panda_mobile.data.model.Album,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(album.placeholderColors))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)))
        )
        
        Row(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SquareArtCard(colors = album.placeholderColors, size = 100.dp, cornerRadius = 8.dp)
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    text = "FEATURED ALBUM",
                    color = EchoPandaColors.AccentBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = album.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = album.artist,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Listen Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AlbumTopBar(
    query: String, 
    isSearchActive: Boolean,
    onToggleSearch: () -> Unit,
    onChange: (String) -> Unit,
    onSearch: () -> Unit = {},
    onVoiceSearch: () -> Unit = {}
) {
    val language = LocalAppLanguage.current
    
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
                onValueChange = onChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search albums...") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true
            )
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = EchoPandaColors.AccentBlue)
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleSearch) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = EchoPandaColors.AccentBlue,
                    modifier = Modifier.size(26.dp)
                )
            }
            
            Text(
                AppStrings.getString("albums", language),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Voice Search",
                    tint = EchoPandaColors.AccentBlue,
                    modifier = Modifier.size(26.dp).clickable { onVoiceSearch() }
                )
                Spacer(Modifier.width(16.dp))
                
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = EchoPandaColors.AccentBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(EchoPandaColors.BgCardDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Name", color = Color.White) },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Artist", color = Color.White) },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings", color = Color.White) },
                            onClick = { showMenu = false }
                        )
                    }
                }
            }
        }
    }
}
