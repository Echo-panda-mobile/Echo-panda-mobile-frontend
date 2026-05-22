package com.example.echo_panda_mobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
fun ArtistCircleCard(artist: Artist, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
        ) {
            ArtPlaceholder(colors = artist.placeholderColors, modifier = Modifier.fillMaxSize())
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(36.dp).align(Alignment.Center)
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
    overlayText: String? = null,
    size: androidx.compose.ui.unit.Dp = 110.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 12.dp,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .clickable { onClick() }
    ) {
        ArtPlaceholder(colors = colors, overlayText = overlayText, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun AlbumCard(album: Album, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() }
    ) {
        SquareArtCard(colors = album.placeholderColors, overlayText = album.title)
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
fun LabeledArtCard(
    title: String,
    subLabel: String,
    colors: List<Color>,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() }
    ) {
        SquareArtCard(colors = colors, overlayText = title)
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
