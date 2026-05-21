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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.model.User // Added import for User model
import com.google.firebase.auth.FirebaseAuth

private object UserProfileTheme {
    val BgStart = Color(0xFF090909)
    val BgEnd = Color(0xFF121212)
    val CardBg = Color(0xFF1E1E1E)
    val AccentColor = Color(0xFF00D9FF)
    val SecondaryAccent = Color(0xFF7000FF)
    val TextMuted = Color(0xFFAAAAAA)
    val ErrorRed = Color(0xFFFF4B4B)
}

@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onEditProfile: () -> Unit, // Navigate to edit screen
    onLogoutSuccess: () -> Unit // Navigate to login screen
) {
    val scrollState = rememberScrollState()
    val authRepo = remember { AuthRepository() }

    // --- FIX: Manage currentUser state correctly ---
    var currentUser by remember { mutableStateOf<User?>(null) }

    // Use LaunchedEffect to call the suspend function when the screen loads
    LaunchedEffect(Unit) {
        try {
            currentUser = authRepo.getCurrentUser()
        } catch (e: Exception) {
            // Handle error silently, user will see default values
        }
    }
    // --- END FIX ---

    // State for Logout Dialog
    var showLogoutDialog by remember { mutableStateOf(false) }

    // --- LOGOUT CONFIRMATION DIALOG ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = UserProfileTheme.CardBg,
            title = { Text("Log Out", color = Color.White) },
            text = { Text("Are you sure you want to log out of Echo Panda?", color = UserProfileTheme.TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    FirebaseAuth.getInstance().signOut() // Actual Firebase Logout
                    onLogoutSuccess()
                }) {
                    Text("Log Out", color = UserProfileTheme.ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(UserProfileTheme.SecondaryAccent.copy(alpha = 0.15f), UserProfileTheme.BgStart),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                // Settings/Edit icon
                IconButton(onClick = onEditProfile) {
                    Icon(Icons.Default.Settings, "Settings", tint = Color.White)
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
                            .clickable { onEditProfile() }, // Clicking photo also goes to edit
                        color = UserProfileTheme.CardBg
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).padding(30.dp),
                            tint = UserProfileTheme.AccentColor
                        )
                    }
                    // Camera Badge - Triggers Edit Profile
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onEditProfile() },
                        color = UserProfileTheme.AccentColor,
                        tonalElevation = 4.dp
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            null,
                            modifier = Modifier.padding(10.dp),
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = currentUser?.name ?: "Loading...",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = currentUser?.email ?: "Please wait",
                    color = UserProfileTheme.TextMuted,
                    fontSize = 14.sp
                )

                // Stats Row
                Row(modifier = Modifier.padding(vertical = 24.dp)) {
                    ProfileStat("12", "Playlists")
                    VerticalDivider()
                    ProfileStat("148", "Liked Songs")
                    VerticalDivider()
                    ProfileStat("24", "Following")
                }
            }

            // --- CONTENT ---
            ProfileSectionTitle("Your Playlists")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listOf("Chill Vibes", "Road Trip", "Gym Mix", "Night Drives")) { playlist ->
                    PlaylistItem(playlist)
                }
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
                    containerColor = UserProfileTheme.ErrorRed.copy(alpha = 0.1f),
                    contentColor = UserProfileTheme.ErrorRed
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, UserProfileTheme.ErrorRed.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontWeight = FontWeight.Bold)
            }

            Text(
                text = "Echo Panda v1.0.42",
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 40.dp),
                textAlign = TextAlign.Center,
                color = UserProfileTheme.TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ProfileStat(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = UserProfileTheme.TextMuted, fontSize = 12.sp)
    }
}

@Composable
fun VerticalDivider() {
    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.1f)))
}

@Composable
fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
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
                .background(UserProfileTheme.CardBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, null, tint = UserProfileTheme.AccentColor.copy(0.4f), modifier = Modifier.size(40.dp))
        }
        Text(name, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp), maxLines = 1)
    }
}