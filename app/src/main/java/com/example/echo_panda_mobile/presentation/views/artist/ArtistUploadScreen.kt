package com.example.echo_panda_mobile.presentation.views.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo_panda_mobile.presentation.components.ArtistBottomBar
import com.example.echo_panda_mobile.presentation.navigation.Routes
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors

@Composable
fun ArtistUploadScreen(
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            Box(modifier = Modifier.statusBarsPadding().padding(16.dp)) {
                Text("Upload Music", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = { ArtistBottomBar(Routes.ARTIST_UPLOAD, onNavigate) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Drop zone for audio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF121A26))
                    .border(1.dp, EchoPandaColors.AccentBlue.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable { /* Pick Audio */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MusicVideo, null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Tap to select audio file", color = Color.White, fontWeight = FontWeight.Medium)
                    Text(".mp3, .wav or .m4a", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }

            // Cover Art Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF121A26))
                        .clickable { /* Pick Image */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.White.copy(alpha = 0.3f))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Cover Artwork", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("1:1 ratio, min 500x500px", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }

            // Track details
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                UploadField(label = "Song Title", placeholder = "Enter song title")
                UploadField(label = "Genre", placeholder = "Select genre")
                UploadField(label = "Album (Optional)", placeholder = "Select or create album")
                UploadField(label = "Lyrics", placeholder = "Paste your lyrics here", singleLine = false)
            }

            Button(
                onClick = { /* Upload */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue)
            ) {
                Text("Publish Now", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun UploadField(label: String, placeholder: String, singleLine: Boolean = true) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EchoPandaColors.AccentBlue,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 4
        )
    }
}
