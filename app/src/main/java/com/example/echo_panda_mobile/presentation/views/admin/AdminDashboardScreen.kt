package com.example.echo_panda_mobile.presentation.views.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.AdminDashboardViewModel

private val BgDark = Color(0xFF05070D)
private val CardBg = Color(0xFF161C24)
private val AccentCyan = Color(0xFF00E5FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    selectedNav: Int,
    onNavSelect: (Int) -> Unit,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: AdminDashboardViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Subtle top glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AccentCyan.copy(alpha = 0.1f), Color.Transparent)
                    )
                )
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                com.example.echo_panda_mobile.presentation.components.AdminTopBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onProfileClick = onProfileClick
                )
            },
            bottomBar = {
                com.example.echo_panda_mobile.presentation.components.AdminBottomBar(
                    selectedIndex = selectedNav,
                    onSelect = onNavSelect
                )
            }
        ) { paddingValues ->
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.isLoading && uiState.stats == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentCyan)
                    }
                } else if (uiState.errorMessage != null && uiState.stats == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Error loading dashboard",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.errorMessage ?: "Unknown error",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Button(
                                onClick = { viewModel.refresh() },
                                modifier = Modifier.padding(top = 16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                            ) {
                                Text("Retry", color = BgDark)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Spacer(modifier = Modifier.height(32.dp))

                        // Stats Grid
                        Text(
                            text = "System Overview",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val stats = uiState.stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatCard(
                                label = "Total Users",
                                value = stats?.totalUsers?.toString() ?: "0",
                                icon = Icons.Default.People,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavSelect(1) }
                            )
                            StatCard(
                                label = "Active Artists",
                                value = stats?.activeArtists?.toString() ?: "0",
                                icon = Icons.Default.MusicNote,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavSelect(1) }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatCard(
                                label = "Total Admins",
                                value = stats?.totalAdmins?.toString() ?: "0",
                                icon = Icons.Default.Security,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavSelect(1) }
                            )
                            StatCard(
                                label = "Total Genres",
                                value = stats?.totalGenres?.toString() ?: "0",
                                icon = Icons.Default.Category,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavSelect(3) }
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Content Statistics
                        Text(
                            text = "Content Metadata",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatCard(
                                label = "Total Tags",
                                value = stats?.totalTags?.toString() ?: "0",
                                icon = Icons.Default.LocalOffer,
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF00BCD4),
                                onClick = { onNavSelect(3) }
                            )
                            StatCard(
                                label = "Total Songs",
                                value = stats?.totalSongs?.toString() ?: "0",
                                icon = Icons.Default.AudioFile,
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF9C27B0),
                                onClick = { onNavSelect(2) }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatCard(
                                label = "Total Albums",
                                value = stats?.totalAlbums?.toString() ?: "0",
                                icon = Icons.Default.Album,
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF4CAF50),
                                onClick = { onNavSelect(2) }
                            )
                            // Placeholder card for balance
                            StatCard(
                                label = "System Status",
                                value = "Active",
                                icon = Icons.Default.CheckCircle,
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF4CAF50),
                                onClick = { /* Already on dashboard */ }
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = AccentCyan,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        color = CardBg,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
