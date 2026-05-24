package com.example.echo_panda_mobile.presentation.components

import androidx.compose.foundation.background
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

// ─── Bottom Navigation Bar ──────────────────

enum class BottomNavTab(val key: String, val icon: ImageVector, val index: Int) {
    HOME("home",     Icons.Default.Home,         0),
    DISCOVER("discover", Icons.Default.Explore,  1),
    ALBUMS("albums", Icons.Default.Album,         2),
    LIBRARY("library", Icons.Default.LibraryMusic, 3),
}

enum class AdminBottomNavTab(val label: String, val icon: ImageVector, val index: Int, val color: Color) {
    HOME("Home",       Icons.Default.Home,       0, Color(0xFF00E5FF)),
    USERS("Users",     Icons.Default.Person,     1, Color(0xFFFF00FF)),
    MUSIC("Music",     Icons.Default.MusicNote,  2, Color(0xFF00E5FF)),
    LIBRARY("Library", Icons.AutoMirrored.Filled.Label,      3, Color(0xFF00E5FF)),
}

@Composable
fun EchoPandaBottomBar(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val language = LocalAppLanguage.current
    
    // Explicitly using a solid dark color to avoid any white/gray background issues
    NavigationBar(
        containerColor = Color(0xFF05070D),
        tonalElevation = 0.dp
    ) {
        BottomNavTab.entries.forEach { tab ->
            val isSelected = selectedIndex == tab.index
            val accentColor = when (tab) {
                BottomNavTab.DISCOVER -> EchoPandaColors.AccentPink
                else                  -> MaterialTheme.colorScheme.primary
            }
            val label = AppStrings.getString(tab.key, language)
            
            NavigationBarItem(
                selected = isSelected,
                onClick  = { onSelect(tab.index) },
                icon = { Icon(tab.icon, contentDescription = label, modifier = Modifier.size(24.dp)) },
                label = { 
                    Text(
                        text = label, 
                        fontSize = 10.sp, 
                        lineHeight = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = accentColor,
                    selectedTextColor   = accentColor,
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.White.copy(alpha = 0.5f),
                    indicatorColor      = Color.Transparent
                )
            )
        }
    }
}

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
    highlightColor: Color = MaterialTheme.colorScheme.primary,
    onViewAll: (() -> Unit)? = null
) {
    val language = LocalAppLanguage.current
    
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = annotatedString,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 24.sp,
            modifier = Modifier.weight(1f)
        )
        if (onViewAll != null) {
            Row(modifier = Modifier.clickable { onViewAll() }, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = AppStrings.getString("view_all", language), 
                    color = highlightColor, 
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = highlightColor, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ArtPlaceholder(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    overlayText: String? = null,
    overlayTextColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .background(Brush.linearGradient(
                if (colors.size >= 2) colors else listOf(colors.first(), colors.first())
            )),
        contentAlignment = Alignment.BottomStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))))
        )
        if (overlayText != null) {
            Text(
                text = overlayText,
                color = overlayTextColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 15.sp,
                modifier = Modifier.padding(8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun ArtistCircleCard(
    artist: Artist, 
    modifier: Modifier = Modifier, 
    size: Dp = 76.dp,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(size)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        ) {
            ArtPlaceholder(colors = artist.placeholderColors, modifier = Modifier.fillMaxSize())
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(size * 0.47f).align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = artist.name,
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SquareArtCard(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    overlayText: String? = null,
    size: Dp = 110.dp,
    cornerRadius: Dp = 12.dp,
    onClick: () -> Unit = {}
) {
    val sizeModifier = if (size != Dp.Unspecified) {
        Modifier.size(size)
    } else {
        Modifier.fillMaxWidth().aspectRatio(1f)
    }

    Box(
        modifier = modifier
            .then(sizeModifier)
            .clip(RoundedCornerShape(cornerRadius))
            .clickable { onClick() }
    ) {
        ArtPlaceholder(colors = colors, overlayText = overlayText, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun AlbumCard(
    album: Album, 
    modifier: Modifier = Modifier,
    width: Dp = 110.dp,
    onClick: () -> Unit = {}
) {
    val finalModifier = if (width != Dp.Unspecified) modifier.width(width) else modifier
    Column(
        modifier = finalModifier
            .clickable { onClick() }
    ) {
        SquareArtCard(
            colors = album.placeholderColors, 
            overlayText = album.title,
            modifier = Modifier.fillMaxWidth(),
            size = width
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = album.title,  
            color = Color.White, 
            fontSize = 13.sp, 
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold, 
            maxLines = 1, 
            overflow = TextOverflow.Ellipsis, 
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = album.artist, 
            color = Color.White.copy(alpha = 0.6f),   
            fontSize = 11.sp, 
            lineHeight = 15.sp,
            maxLines = 1, 
            overflow = TextOverflow.Ellipsis, 
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AlbumListRow(
    album: Album,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SquareArtCard(
            colors = album.placeholderColors,
            size = 64.dp,
            cornerRadius = 8.dp
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = album.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = album.artist,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.Default.MoreVert,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SongRow(
    index: Int,
    track: Track,
    isPlaying: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Index or Play Icon
        Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.CenterStart) {
            if (isPlaying) {
                Icon(Icons.Default.Pause, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text(
                    text = (index + 1).toString(),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Small Artwork
        SquareArtCard(
            colors = track.placeholderColors,
            size = 48.dp,
            cornerRadius = 4.dp
        )

        Spacer(Modifier.width(16.dp))

        // Title and Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Duration
        val minutes = (track.durationMs / 1000) / 60
        val seconds = (track.durationMs / 1000) % 60
        Text(
            text = String.format("%d:%02d", minutes, seconds),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // More Icon
        Icon(
            Icons.Default.MoreVert,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121212).copy(alpha = 0.95f))
            .clickable { onClick() }
    ) {
        // Progress Bar
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.2f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SquareArtCard(
                colors = track.placeholderColors,
                size = 40.dp,
                cornerRadius = 4.dp
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            
            IconButton(onClick = onTogglePlay) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(onClick = onNext) {
                Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            
            IconButton(onClick = { /* More options */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun LabeledArtCard(
    title: String,
    subLabel: String,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    size: Dp = 110.dp,
    onClick: () -> Unit = {}
) {
    val finalModifier = if (size != Dp.Unspecified) modifier.width(size) else modifier
    Column(
        modifier = finalModifier
            .clickable { onClick() }
    ) {
        SquareArtCard(
            colors = colors, 
            overlayText = title, 
            modifier = Modifier.fillMaxWidth(),
            size = size
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subLabel, 
            color = Color.White, 
            fontSize = 12.sp, 
            lineHeight = 16.sp,
            maxLines = 1, 
            overflow = TextOverflow.Ellipsis, 
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun RecentPlaylistCard(playlist: Playlist, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Row(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(58.dp)) {
            ArtPlaceholder(
                colors = playlist.placeholderColors,
                overlayText = playlist.labelOverlay,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = playlist.title,
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
    }
}
