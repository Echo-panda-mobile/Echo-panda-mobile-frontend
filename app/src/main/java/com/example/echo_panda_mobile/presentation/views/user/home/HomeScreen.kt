@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.echo_panda_mobile.presentation.views.user.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.model.User // Ensure this import is correct
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color Palette
val DarkBgStart = Color(0xFF0D2530)
val DarkBgEnd = Color(0xFF0F0F11)
val CardSurfaceColor = Color(0xFF1B1E22)
val AccentBlue = Color(0xFF0091FF)
val AccentPink = Color(0xFFE91E63)
val TextSecondaryColor = Color(0xFF8F93A2)

// Data models for the UI
data class GridItem(val title: String, val imageUrl: String?)
data class AlbumItem(val title: String, val artist: String)

@Composable
fun HomeScreen(
    onNavigateToUserProfile: () -> Unit,
    onNavigateToArtistProfile: () -> Unit
) {
    val authRepo = remember { AuthRepository() }

    // FIX: Initialize state as null first
    var currentUser by remember { mutableStateOf<User?>(null) }

    // FIX: Call the suspend functions inside LaunchedEffect
    LaunchedEffect(Unit) {
        try {
            // Fetch the user data asynchronously
            currentUser = authRepo.getCurrentUser()

            // Optionally fetch profile if needed
            authRepo.getCurrentUserProfile()?.let {
                currentUser = it
            }
        } catch (e: Exception) {
            // Log or handle error silently - show default user
            currentUser = User(
                id = 0,
                name = "Echo Panda",
                email = "hello@echo.com",
                role = "user",
                token = ""
            )
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkBgStart, DarkBgEnd),
                        startY = 0f,
                        endY = 1200f
                    )
                )
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Top Header Profile Row
            HeaderSection(
                userName = currentUser?.name ?: "Echo Panda",
                userEmail = currentUser?.email ?: "hello@echo.com",
                onProfileClick = onNavigateToUserProfile
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Continue Listening Section
            SectionHeader(title = "Continue Listening", showViewAll = false)
            Spacer(modifier = Modifier.height(12.dp))
            ContinueListeningGrid()

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Popular Artists
            SectionHeader(title = "Popular Artists", highlightWord = "Artists", onClickViewAll = {})
            Spacer(modifier = Modifier.height(12.dp))
            PopularArtistsRow()

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Top Albums
            SectionHeader(title = "Top Albums", highlightWord = "Albums", onClickViewAll = {})
            Spacer(modifier = Modifier.height(12.dp))
            TopAlbumsRow()

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Promotional Billboard Card
            PromoBillboardCard()

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Recent listening
            SectionHeader(title = "Based on your recent listening", showViewAll = false)
            Spacer(modifier = Modifier.height(12.dp))
            RecentListeningRow()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HeaderSection(
    userName: String,
    userEmail: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable { onProfileClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.align(Alignment.Center),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Welcome back, ${userName.ifBlank { "Echo Panda" }}!",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userEmail.ifEmpty { "hello@echo.com" },
                    color = TextSecondaryColor,
                    fontSize = 12.sp
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Stats", tint = Color.White, modifier = Modifier.size(22.dp))
            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alerts", tint = Color.White, modifier = Modifier.size(22.dp))
            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    highlightWord: String? = null,
    showViewAll: Boolean = true,
    onClickViewAll: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (highlightWord != null && title.contains(highlightWord)) {
            val parts = title.split(highlightWord)
            Text(
                text = buildAnnotatedString {
                    append(parts.getOrNull(0) ?: "")
                    withStyle(style = SpanStyle(color = AccentBlue)) {
                        append(highlightWord)
                    }
                    append(parts.getOrNull(1) ?: "")
                },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (showViewAll) {
            Text(
                text = "View All >",
                color = AccentBlue,
                fontSize = 12.sp,
                modifier = Modifier.clickable { onClickViewAll() }
            )
        }
    }
}

@Composable
fun ContinueListeningGrid() {
    val items = listOf(
        GridItem("Coffee & Jazz", null),
        GridItem("RELEASED", null),
        GridItem("Anything Goes", null),
        GridItem("Anime OSTs", null),
        GridItem("Harry's House", null),
        GridItem("Lo-Fi Beats", null)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in items.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GridTile(item = items[i], modifier = Modifier.weight(1f))
                if (i + 1 < items.size) {
                    GridTile(item = items[i + 1], modifier = Modifier.weight(1f))
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun GridTile(item: GridItem, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(56.dp)
            .background(CardSurfaceColor, RoundedCornerShape(6.dp))
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(56.dp)
                .background(Color(0xFF2C3139), RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PopularArtistsRow() {
    val artists = listOf("Eminem", "Lana Del Rey", "Adele", "Harry Styles")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(artists) { artist ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(76.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF252930))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = artist,
                    color = TextSecondaryColor,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TopAlbumsRow() {
    val albums = listOf(
        AlbumItem("Adele 21", "Adele"),
        AlbumItem("Scorpion", "Drake"),
        AlbumItem("Born To Die", "Lana Del Rey")
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(albums) { album ->
            Column(modifier = Modifier.width(110.dp)) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color(0xFF252930), RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = album.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = album.artist, color = TextSecondaryColor, fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}

@Composable
fun PromoBillboardCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF28231D), Color(0xFF1C1A17))),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(0.6f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Billie Eilish", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You can have easy access to every song by clicking down below.",
                    color = TextSecondaryColor,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Listen Now", fontSize = 10.sp, color = Color.White)
                }
                OutlinedButton(
                    onClick = {},
                    border = BorderStroke(1.dp, Color.Gray),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Follow", fontSize = 10.sp, color = Color.White)
                }
            }
        }
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterEnd)
                .background(Color.DarkGray, RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun RecentListeningRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(140.dp)
                .background(Color(0xFF252930), RoundedCornerShape(8.dp))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(140.dp)
                .background(Color(0xFF252930), RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar(
        containerColor = Color(0xFF090A0C),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentPink,
                selectedTextColor = AccentPink,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Search, contentDescription = "Discover") },
            label = { Text("Discover") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Playlist") },
            label = { Text("Playlist") }
        )
    }
}