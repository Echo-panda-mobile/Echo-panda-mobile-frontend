package com.example.echo_panda_mobile.presentation.views.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistCommentsScreen(
    onBack: () -> Unit
) {
    val comments = remember {
        listOf(
            CommentItem("1", "User123", "Love this track! Can't wait for more.", "Midnight City", "2h ago"),
            CommentItem("2", "MelodyFan", "The production is top notch.", "Neon Dreams", "5h ago"),
            CommentItem("3", "BeatMaster", "Is this part of a new album?", "Lost in Echo", "1d ago")
        )
    }

    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            TopAppBar(
                title = { Text("Manage Comments", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
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
            items(comments) { comment ->
                CommentRow(comment)
            }
        }
    }
}

data class CommentItem(
    val id: String,
    val username: String,
    val content: String,
    val trackName: String,
    val time: String
)

@Composable
private fun CommentRow(comment: CommentItem) {
    Surface(
        color = Color(0xFF121A26),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(comment.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("on ${comment.trackName} • ${comment.time}", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(comment.content, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = {}, colors = ButtonDefaults.textButtonColors(contentColor = EchoPandaColors.AccentBlue)) {
                    Text("Reply", fontSize = 12.sp)
                }
                TextButton(onClick = {}, colors = ButtonDefaults.textButtonColors(contentColor = EchoPandaColors.ErrorRed)) {
                    Text("Delete", fontSize = 12.sp)
                }
            }
        }
    }
}
