package com.example.echo_panda_mobile.presentation.views.artist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo_panda_mobile.data.model.Track
import com.example.echo_panda_mobile.presentation.components.ArtistBottomBar
import com.example.echo_panda_mobile.presentation.components.SquareArtCard
import com.example.echo_panda_mobile.presentation.navigation.Routes
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors

@Composable
fun ArtistMusicScreen(
    onNavigate: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Mock tracks for now - will be replaced with real data
    val mockTracks = listOf(
        Track("1", "Neon Dreams", "You", durationMs = 210000),
        Track("2", "Midnight City", "You", durationMs = 185000),
        Track("3", "Lost in Echo", "You", durationMs = 240000)
    )

    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            Column(modifier = Modifier.statusBarsPadding().padding(16.dp)) {
                Text("Your Music", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search your tracks...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EchoPandaColors.AccentBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        bottomBar = { ArtistBottomBar(Routes.ARTIST_MY_MUSIC, onNavigate) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mockTracks) { track ->
                ArtistTrackRow(track)
            }
        }
    }
}

@Composable
private fun ArtistTrackRow(track: Track) {
    Surface(
        color = Color(0xFF121A26),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SquareArtCard(colors = track.placeholderColors, size = 56.dp, cornerRadius = 8.dp)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(track.title, color = Color.White, fontWeight = FontWeight.Bold)
                Text("2.5K streams • Published 2 days ago", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, null, tint = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}
