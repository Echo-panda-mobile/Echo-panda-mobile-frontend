package com.example.echo_panda_mobile.presentation.views.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val BgDark = Color(0xFF05070D)
private val CardBg = Color(0xFF161C24)
private val AccentPurple = Color(0xFFFF00FF)
private val TextMuted = Color.White.copy(alpha = 0.5f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSongDetailScreen(
    songId: String,
    onBack: () -> Unit
) {
    // Mock data for song detail
    // In a real app, you'd fetch this using songId
    val song = AdminSongRecord(
        title = "Birds of Feather",
        artist = "Billie Eilish",
        album = "Happier Than Ever",
        duration = "3:50",
        createdDate = "1/10/2026",
        imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_p_Q5F5W4zX2N8E4_6Xz7R6U4z_5y_8z9w&s"
    )
    val status = "ACTIVE" // Mock status

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Song Details", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            // Song Image
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (song.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = song.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(song.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(song.artist, color = AccentPurple, fontSize = 18.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(32.dp))

            // Info items
            DetailItem("Album", song.album, Icons.Default.Album)
            DetailItem("Created at", song.createdDate, Icons.Default.CalendarToday)
            DetailItem("Duration", song.duration, Icons.Default.AccessTime)
            DetailItem(
                label = "Status",
                value = status,
                icon = if (status == "ACTIVE") Icons.Default.CheckCircle else Icons.Default.Block,
                color = if (status == "ACTIVE") Color(0xFF00C853) else Color(0xFFFF5252)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Button(
                onClick = { /* Handle Ban/Unban */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (status == "ACTIVE") Color(0xFFFF5252) else Color(0xFF00C853)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (status == "ACTIVE") "Ban Song" else "Unban Song", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color = Color.White) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = CardBg,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, color = TextMuted, fontSize = 12.sp)
                Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
