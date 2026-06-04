package com.example.echo_panda_mobile.presentation.views.user.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistNotificationScreen(
    onBack: () -> Unit
) {
    val notifications = remember {
        listOf(
            ArtistNotification("1", "New Follower", "Alex Rivers started following you.", Icons.Default.PersonAdd, "2m ago"),
            ArtistNotification("2", "Song Milestone", "Your track 'Midnight City' reached 10K streams!", Icons.Default.TrendingUp, "1h ago"),
            ArtistNotification("3", "New Comment", "Someone commented on your album 'Neon Dreams'.", Icons.Default.Comment, "3h ago"),
            ArtistNotification("4", "Trending", "You are trending in the 'Synthwave' genre!", Icons.Default.Bolt, "5h ago"),
            ArtistNotification("5", "Payout Ready", "Your monthly revenue has been processed.", Icons.Default.Payments, "1d ago")
        )
    }

    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { notification ->
                NotificationRow(notification)
            }
        }
    }
}

data class ArtistNotification(
    val id: String,
    val title: String,
    val message: String,
    val icon: ImageVector,
    val time: String
)

@Composable
private fun NotificationRow(notification: ArtistNotification) {
    Surface(
        color = Color(0xFF121A26),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(EchoPandaColors.AccentBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(notification.icon, null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(notification.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(notification.time, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(notification.message, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            }
        }
    }
}
