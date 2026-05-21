@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.echo_panda_mobile.presentation.views.user.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.model.User // Ensure this matches your project's User model
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// We wrap colors in an object to solve the "Overload resolution ambiguity"
private object ArtistProfileColors {
    val DarkBg = Color(0xFF0A0E16)
    val CardBg = Color(0xFF1A1F2E)
    val StatColor = Color(0xFF1CC7D0)
    val TextLight = Color(0xFFFFFFFF)
    val TextMuted = Color(0xFF9DA3AA)
}

@Composable
fun ArtistProfileScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val authRepository = remember { AuthRepository() }

    // 1. Correct state management for the user
    var currentUser by remember { mutableStateOf<com.example.echo_panda_mobile.data.model.User?>(null) }

    // 2. Fetch the user in a side effect
    LaunchedEffect(Unit) {
        currentUser = authRepository.getCurrentUser()
    }

    // 3. Define UI states
    var artistName by remember { mutableStateOf("") }
    var artistEmail by remember { mutableStateOf("") }
    var artistBio by remember { mutableStateOf("Edit your artist name, email, and profile image.") }

    // 4. Update UI states when currentUser is loaded
    LaunchedEffect(currentUser) {
        currentUser?.let {
            artistName = it.name ?: "Alex Producer"
            artistEmail = it.email ?: "artist@example.com"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArtistProfileColors.DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(ArtistProfileColors.CardBg, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ArtistProfileColors.StatColor
                    )
                }
                Text(
                    text = "Artist Profile",
                    color = ArtistProfileColors.TextLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .background(ArtistProfileColors.CardBg, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = ArtistProfileColors.StatColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- ARTIST AVATAR ---
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape),
                color = ArtistProfileColors.StatColor.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Artist",
                        tint = ArtistProfileColors.StatColor,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (artistName.isEmpty()) "Loading..." else artistName,
                color = ArtistProfileColors.TextLight,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = if (artistEmail.isEmpty()) "Please wait..." else artistEmail,
                color = ArtistProfileColors.TextMuted,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- EDIT FIELDS ---
            OutlinedTextField(
                value = artistName,
                onValueChange = { artistName = it },
                label = { Text("Artist Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ArtistProfileColors.CardBg,
                    unfocusedContainerColor = ArtistProfileColors.CardBg,
                    focusedTextColor = ArtistProfileColors.TextLight,
                    unfocusedTextColor = ArtistProfileColors.TextLight,
                    focusedBorderColor = ArtistProfileColors.StatColor,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = ArtistProfileColors.StatColor
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = artistEmail,
                onValueChange = { artistEmail = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ArtistProfileColors.CardBg,
                    unfocusedContainerColor = ArtistProfileColors.CardBg,
                    focusedTextColor = ArtistProfileColors.TextLight,
                    unfocusedTextColor = ArtistProfileColors.TextLight,
                    focusedBorderColor = ArtistProfileColors.StatColor,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = ArtistProfileColors.StatColor
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { artistBio = "Profile updated locally." },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ArtistProfileColors.StatColor)
            ) {
                Text(text = "Save Profile", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- STATS ROW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArtistStatBox(title = "Followers", value = "125K", modifier = Modifier.weight(1f))
                ArtistStatBox(title = "Tracks", value = "24", modifier = Modifier.weight(1f))
                ArtistStatBox(title = "Playlists", value = "8", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- TOOLS SECTION ---
            Text(
                text = "Artist Tools",
                color = ArtistProfileColors.TextLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ArtistActionItem(
                icon = Icons.Filled.CloudUpload,
                title = "Upload New Track",
                subtitle = "Add music to your catalog",
                onClick = {}
            )

            ArtistActionItem(
                icon = Icons.Filled.BarChart,
                title = "Analytics",
                subtitle = "View detailed statistics",
                onClick = {}
            )

            ArtistActionItem(
                icon = Icons.Filled.Group,
                title = "Manage Team",
                subtitle = "Collaborators & producers",
                onClick = {}
            )

            ArtistActionItem(
                icon = Icons.Filled.Payments,
                title = "Earnings",
                subtitle = "View your revenue",
                onClick = {}
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- LOGOUT ---
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4B4B))
            ) {
                Text(text = "Log Out", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ArtistStatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(100.dp),
        color = ArtistProfileColors.CardBg,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                color = ArtistProfileColors.StatColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = ArtistProfileColors.TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ArtistActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        color = ArtistProfileColors.CardBg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = ArtistProfileColors.StatColor,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = ArtistProfileColors.TextLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = ArtistProfileColors.TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = ArtistProfileColors.TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}