package com.example.echo_panda_mobile.presentation.views.user.artist

import androidx.compose.foundation.background
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
import com.example.echo_panda_mobile.presentation.components.SquareArtCard
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistAlbumManagementScreen(
    onBack: () -> Unit,
    onNavigateToCreate: () -> Unit
) {
    // Mock albums
    val albums = remember {
        listOf(
            ArtistAlbum("1", "Neon Dreams", "12 Songs", "Published"),
            ArtistAlbum("2", "Midnight Sessions", "8 Songs", "Draft"),
            ArtistAlbum("3", "Lost in Echo", "5 Songs", "Published")
        )
    }

    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            TopAppBar(
                title = { Text("My Albums", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCreate) {
                        Icon(Icons.Default.Add, null, tint = EchoPandaColors.AccentBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(albums) { album ->
                AlbumManageRow(album)
            }
        }
    }
}

data class ArtistAlbum(
    val id: String,
    val title: String,
    val count: String,
    val status: String
)

@Composable
private fun AlbumManageRow(album: ArtistAlbum) {
    Surface(
        color = Color(0xFF121A26),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Album, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(album.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(album.count, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (album.status == "Published") Color(0xFF4CAF50).copy(alpha = 0.1f)
                                else Color(0xFFFFC107).copy(alpha = 0.1f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            album.status,
                            color = if (album.status == "Published") Color(0xFF4CAF50) else Color(0xFFFFC107),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Edit, null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Delete, null, tint = EchoPandaColors.ErrorRed, modifier = Modifier.size(20.dp))
            }
        }
    }
}
