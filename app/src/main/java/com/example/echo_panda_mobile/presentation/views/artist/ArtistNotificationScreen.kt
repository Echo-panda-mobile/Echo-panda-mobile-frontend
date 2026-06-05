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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.data.remote.NotificationDto
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistNotificationViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistNotificationViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistNotificationScreen(
    onBack: () -> Unit,
    viewModel: ArtistNotificationViewModel = viewModel(factory = ArtistNotificationViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadNotifications(isRefresh = true) },
            modifier = Modifier.padding(padding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is ArtistNotificationViewModel.NotificationUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = EchoPandaColors.AccentBlue)
                    }
                    is ArtistNotificationViewModel.NotificationUiState.Success -> {
                        if (state.notifications.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No notifications", color = Color.White.copy(alpha = 0.5f))
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.notifications) { notification ->
                                    NotificationRow(notification)
                                }
                            }
                        }
                    }
                    is ArtistNotificationViewModel.NotificationUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Error: ${state.message}", color = Color.Red)
                            Button(onClick = { viewModel.loadNotifications() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: NotificationDto) {
    val icon = when (notification.type) {
        "follower" -> Icons.Default.PersonAdd
        "milestone" -> Icons.AutoMirrored.Filled.TrendingUp
        "comment" -> Icons.AutoMirrored.Filled.Comment
        "trending" -> Icons.Default.Bolt
        else -> Icons.Default.Notifications
    }

    val timeStr = remember(notification.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        sdf.format(Date(notification.timestamp))
    }

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
                Icon(icon, null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(notification.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(timeStr, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(notification.message, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            }
        }
    }
}
