package com.example.echo_panda_mobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.theme.LocalAppLanguage
import com.example.echo_panda_mobile.presentation.navigation.Routes

// ─── Navigation Components ──────────────────

enum class BottomNavTab(val key: String, val icon: ImageVector, val index: Int) {
    HOME("home",     Icons.Default.Home,         0),
    DISCOVER("discover", Icons.Default.Explore,  1),
    ALBUMS("albums", Icons.Default.Album,         2),
    LIBRARY("library", Icons.Default.LibraryMusic, 3),
}

enum class AdminBottomNavTab(val label: String, val icon: ImageVector, val index: Int, val color: Color) {
    HOME("Home",       Icons.Default.Home,       0, Color(0xFF00E5FF)),
    USERS("Users",     Icons.Default.Person,     1, Color(0xFF00E5FF)),
    MUSIC("Music",     Icons.Default.MusicNote,  2, Color(0xFF00E5FF)),
    LIBRARY("Library", Icons.AutoMirrored.Filled.Label,      3, Color(0xFF00E5FF)),
}

enum class ArtistNavTab(val route: String, val icon: ImageVector, val label: String) {
    DASHBOARD(Routes.ARTIST_DASHBOARD, Icons.Default.Dashboard, "Dashboard"),
    MUSIC(Routes.ARTIST_MY_MUSIC, Icons.Default.MusicNote, "Music"),
    UPLOAD(Routes.ARTIST_UPLOAD, Icons.Default.CloudUpload, "Upload"),
    ANALYTICS(Routes.ARTIST_ANALYTICS, Icons.Default.BarChart, "Analytics"),
    PROFILE(Routes.ARTIST_PROFILE, Icons.Default.Person, "Profile"),
}

@Composable
fun EchoPandaBottomBar(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val language = LocalAppLanguage.current
    NavigationBar(containerColor = Color(0xFF05070D), tonalElevation = 0.dp) {
        BottomNavTab.entries.forEach { tab ->
            val isSelected = selectedIndex == tab.index
            val label = AppStrings.getString(tab.key, language)
            NavigationBarItem(
                selected = isSelected,
                onClick  = { onSelect(tab.index) },
                icon = { Icon(tab.icon, contentDescription = label, modifier = Modifier.size(24.dp)) },
                label = { Text(text = label, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = EchoPandaColors.AccentBlue,
                    selectedTextColor   = EchoPandaColors.AccentBlue,
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.White.copy(alpha = 0.5f),
                    indicatorColor      = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun ArtistBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = Color(0xFF05070D), tonalElevation = 0.dp) {
        ArtistNavTab.entries.forEach { tab ->
            val isSelected = currentRoute == tab.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(tab.route) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = EchoPandaColors.AccentBlue,
                    selectedTextColor = EchoPandaColors.AccentBlue,
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.White.copy(alpha = 0.5f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

// ─── Display Components ─────────────────────

@Composable
fun AdminBottomBar(selectedIndex: Int, onSelect: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF05070D),
        tonalElevation = 0.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        AdminBottomNavTab.entries.forEach { tab ->
            val isSelected = selectedIndex == tab.index
            val tabColor = tab.color

            NavigationBarItem(
                selected = isSelected,
                onClick  = { onSelect(tab.index) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = tabColor,
                    selectedTextColor   = tabColor,
                    unselectedIconColor = tabColor.copy(alpha = 0.6f), // Keep it colorful but slightly faded
                    unselectedTextColor = tabColor.copy(alpha = 0.6f),
                    indicatorColor      = Color.Transparent
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        color = Color(0xFF05070D),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp)),
                placeholder = {
                    Text(
                        "Search...",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF161C24),
                    unfocusedContainerColor = Color(0xFF161C24),
                    disabledContainerColor = Color(0xFF161C24),
                    cursorColor = Color(0xFF00E5FF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            // Profile Icon
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    fullTitle: String,
    highlightPart: String? = null,
    highlightColor: Color = EchoPandaColors.AccentBlue,
    onViewAll: (() -> Unit)? = null
) {
    val annotatedString = buildAnnotatedString {
        val startIndex = highlightPart?.let { fullTitle.indexOf(it) } ?: -1
        if (startIndex != -1 && highlightPart != null) {
            append(fullTitle.substring(0, startIndex))
            withStyle(style = SpanStyle(color = highlightColor, fontWeight = FontWeight.Bold)) {
                append(highlightPart)
            }
            append(fullTitle.substring(startIndex + highlightPart.length))
        } else {
            append(fullTitle)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = annotatedString, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (onViewAll != null) {
            Text(
                text = "View All >",
                color = highlightColor,
                fontSize = 12.sp,
                modifier = Modifier.clickable { onViewAll() }
            )
        }
    }
}

@Composable
fun ArtPlaceholder(colors: List<Color>, modifier: Modifier = Modifier, overlayText: String? = null) {
    Box(
        modifier = modifier.background(Brush.linearGradient(if (colors.size >= 2) colors else listOf(colors.first(), colors.first()))),
        contentAlignment = Alignment.BottomStart
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)))))
        if (overlayText != null) {
            Text(text = overlayText, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
        } else {
            Icon(Icons.Default.MusicNote, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(28.dp).align(Alignment.Center))
        }
    }
}

@Composable
fun SquareArtCard(colors: List<Color>, modifier: Modifier = Modifier, overlayText: String? = null, size: Dp = 110.dp, cornerRadius: Dp = 12.dp, onClick: () -> Unit = {}) {
    val sizeMod = if (size != Dp.Unspecified) Modifier.size(size) else Modifier.fillMaxWidth().aspectRatio(1f)
    Box(modifier = modifier.then(sizeMod).clip(RoundedCornerShape(cornerRadius)).clickable { onClick() }) {
        ArtPlaceholder(colors = colors, overlayText = overlayText, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun ArtistCircleCard(artist: Artist, modifier: Modifier = Modifier, size: Dp = 76.dp, onClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.width(size).clickable { onClick() }) {
        Box(modifier = Modifier.size(size).clip(CircleShape)) {
            ArtPlaceholder(colors = artist.placeholderColors, modifier = Modifier.fillMaxSize())
            Icon(Icons.Default.Person, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(size * 0.5f).align(Alignment.Center))
        }
        Spacer(Modifier.height(6.dp))
        Text(text = artist.name, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun AlbumCard(album: Album, modifier: Modifier = Modifier, width: Dp = 110.dp, onClick: () -> Unit = {}) {
    val finalMod = if (width != Dp.Unspecified) modifier.width(width) else modifier
    Column(modifier = finalMod.clickable { onClick() }) {
        SquareArtCard(colors = album.placeholderColors, modifier = Modifier.fillMaxWidth(), size = width)
        Spacer(Modifier.height(8.dp))
        Text(text = album.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = album.artist, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun AlbumListRow(album: Album, modifier: Modifier = Modifier, onClick: () -> Unit = {}, onAddToFavorites: (() -> Unit)? = null, onAddToPlaylist: (() -> Unit)? = null) {
    var showMenu by remember { mutableStateOf(false) }
    Row(modifier = modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        SquareArtCard(colors = album.placeholderColors, size = 64.dp, cornerRadius = 8.dp)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = album.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = album.artist, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        }
        Box {
            IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = Color.White.copy(alpha = 0.4f)) }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(EchoPandaColors.BgCardDark)) {
                if (onAddToFavorites != null) DropdownMenuItem(text = { Text("Add to Favorites", color = Color.White) }, onClick = { onAddToFavorites(); showMenu = false })
                if (onAddToPlaylist != null) DropdownMenuItem(text = { Text("Add to Playlist", color = Color.White) }, onClick = { onAddToPlaylist(); showMenu = false })
            }
        }
    }
}

@Composable
fun SongRow(index: Int, track: Track, isPlaying: Boolean = false, onClick: () -> Unit = {}, onAddToFavorites: (() -> Unit)? = null, onAddToPlaylist: (() -> Unit)? = null) {
    var showMenu by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp, horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(32.dp)) {
            if (isPlaying) Icon(Icons.Default.Pause, null, tint = Color.White, modifier = Modifier.size(16.dp))
            else Text(text = (index + 1).toString(), color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
        }
        SquareArtCard(colors = track.placeholderColors, size = 48.dp, cornerRadius = 4.dp)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = track.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(text = track.artist, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        }
        val mins = (track.durationMs / 1000) / 60
        val secs = (track.durationMs / 1000) % 60
        Text(text = String.format("%d:%02d", mins, secs), color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp))
        Box {
            IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = Color.White.copy(alpha = 0.5f)) }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(EchoPandaColors.BgCardDark)) {
                if (onAddToFavorites != null) DropdownMenuItem(text = { Text("Add to Favorites", color = Color.White) }, onClick = { onAddToFavorites(); showMenu = false })
                if (onAddToPlaylist != null) DropdownMenuItem(text = { Text("Add to Playlist", color = Color.White) }, onClick = { onAddToPlaylist(); showMenu = false })
            }
        }
    }
}

@Composable
fun MiniPlayer(track: Track, isPlaying: Boolean, progress: Float, onTogglePlay: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF121212).copy(alpha = 0.95f)).clickable { onClick() }) {
        LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(2.dp), color = Color.White, trackColor = Color.White.copy(alpha = 0.2f))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            SquareArtCard(colors = track.placeholderColors, size = 40.dp, cornerRadius = 4.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = track.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = track.artist, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
            IconButton(onClick = onPrevious) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White) }
            IconButton(onClick = onTogglePlay) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
            IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, null, tint = Color.White) }
        }
    }
}

@Composable
fun LabeledArtCard(title: String, subLabel: String, colors: List<Color>, modifier: Modifier = Modifier, size: Dp = 110.dp, onClick: () -> Unit = {}) {
    val finalMod = if (size != Dp.Unspecified) modifier.width(size) else modifier
    Column(modifier = finalMod.clickable { onClick() }) {
        SquareArtCard(colors = colors, modifier = Modifier.fillMaxWidth(), size = size)
        Spacer(Modifier.height(10.dp))
        Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = subLabel, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Default.ErrorOutline, null, tint = EchoPandaColors.ErrorRed, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text(message, color = Color.White, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue)) { Text("Retry", color = Color.Black) }
        }
    }
}

@Composable
fun RecentPlaylistCard(playlist: Playlist, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Row(modifier = modifier.height(56.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF121A26)).clickable { onClick() }, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(56.dp).background(Color(0xFF1E2736))) {
            ArtPlaceholder(colors = playlist.placeholderColors, overlayText = playlist.labelOverlay, modifier = Modifier.fillMaxSize())
        }
        Text(text = playlist.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 12.dp))
    }
}

@Composable
fun FeaturedArtistCard(featured: FeaturedArtist, onListenNow: () -> Unit = {}, onFollow: () -> Unit = {}) {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), color = Color(0xFF1A1512), shape = RoundedCornerShape(20.dp)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(text = featured.artist.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(text = featured.description, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, lineHeight = 18.sp, maxLines = 3)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onListenNow, colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue), shape = RoundedCornerShape(20.dp), modifier = Modifier.height(36.dp)) { Text("Listen Now", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    OutlinedButton(onClick = onFollow, border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)), shape = RoundedCornerShape(20.dp), modifier = Modifier.height(36.dp)) { Text("Follow", color = Color.White, fontSize = 12.sp) }
                }
            }
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(0.8f).aspectRatio(1f).clip(RoundedCornerShape(12.dp))) { ArtPlaceholder(colors = featured.artist.placeholderColors, modifier = Modifier.fillMaxSize()) }
        }
    }
}
