@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.echo_panda_mobile.presentation.views.user.profile

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.UserProfileViewModel
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.google.firebase.auth.FirebaseAuth

@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onEditProfile: () -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: UserProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // State for Logout Dialog
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Log Out", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Are you sure you want to log out of Echo Panda?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    FirebaseAuth.getInstance().signOut()
                    onLogoutSuccess()
                }) {
                    Text("Log Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    ),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .statusBarsPadding()
            ) {
                // --- TOP NAVIGATION ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }

                // --- PROFILE HEADER ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .clickable { onEditProfile() },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp).padding(30.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { onEditProfile() },
                            color = MaterialTheme.colorScheme.primary,
                            tonalElevation = 4.dp
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                null,
                                modifier = Modifier.padding(10.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.user?.name ?: "User",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = uiState.user?.email ?: "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    // Dynamic Stats Row
                    Row(modifier = Modifier.padding(vertical = 24.dp)) {
                        ProfileStat(uiState.playlists.size.toString(), "Playlists")
                        VerticalDivider()
                        ProfileStat(uiState.likedSongsCount.toString(), "Liked Songs")
                        VerticalDivider()
                        ProfileStat(uiState.followingCount.toString(), "Following")
                    }
                }

                // --- DYNAMIC CONTENT ---
                if (uiState.playlists.isNotEmpty()) {
                    ProfileSectionTitle("Your Playlists")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.playlists) { playlistName ->
                            PlaylistItem(playlistName)
                        }
                    }
                } else {
                    ProfileSectionTitle("No Playlists Yet")
                    Text(
                        "Start creating your own music world!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- LOGOUT BUTTON ---
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Echo Panda v1.0.42",
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 40.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ProfileStat(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(number, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
fun VerticalDivider() {
    Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)))
}

@Composable
fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
    )
}

@Composable
fun PlaylistItem(name: String) {
    Column(modifier = Modifier.width(120.dp)) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), modifier = Modifier.size(40.dp))
        }
        Text(name, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp), maxLines = 1)
    }
}
