package com.example.echo_panda_mobile.presentation.views.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgDark = Color(0xFF05070D)
private val CardBg = Color(0xFF161C24)
private val AccentPurple = Color(0xFFFF00FF)
private val TextMuted = Color.White.copy(alpha = 0.5f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(
    userId: String,
    role: String,
    onBack: () -> Unit
) {
    // Mock data for detail view
    val user = AdminUserRecord(
        id = userId,
        name = "Pory Morokot",
        email = "morokotpory@gmail.com",
        joinedDate = "May 24, 2026",
        status = "ACTIVE",
        role = role
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Details", color = Color.White, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AccentPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.firstOrNull()?.toString() ?: "",
                    color = AccentPurple,
                    fontWeight = FontWeight.Black,
                    fontSize = 40.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(user.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(user.email, color = TextMuted, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Info Cards
            DetailItem("User ID", user.id, Icons.Default.Fingerprint)
            DetailItem(
                label = "Status",
                value = user.status,
                icon = if (user.status == "ACTIVE") Icons.Default.CheckCircle else Icons.Default.Block,
                color = if (user.status == "ACTIVE") Color(0xFF00C853) else Color(0xFFFF5252)
            )
            DetailItem("Joined Date", user.joinedDate, Icons.Default.CalendarToday)
            DetailItem("Account Type", user.role, Icons.Default.Stars)

            // --- Information shown ONLY for ARTISTS ---
            if (user.role == "Artist") {
                val artistRole = "Group" // Example: "Single" or "Group"
                val artistGender = "Male" // Example: "Male" or "Female"
                
                DetailItem("Artist Role", artistRole, Icons.Default.Groups)
                
                val genderValue = if (artistRole == "Group") "They" else artistGender
                DetailItem("Gender", genderValue, Icons.Default.Face)

                DetailItem("Total Songs", "24 Tracks", Icons.Default.MusicNote)
                DetailItem("Monthly Listeners", "12,400", Icons.Default.GraphicEq)
                DetailItem("Verification", "Verified Artist", Icons.Default.Verified)
            }

            // --- Information shown ONLY for ADMINS ---
            if (user.role == "Admin") {
                DetailItem("Permission Level", "Super Admin", Icons.Default.Security)
                DetailItem("Last Access", "10 mins ago", Icons.Default.History)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Button(
                onClick = { /* Handle Ban/Unban */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (user.status == "ACTIVE") Color(0xFFFF5252) else Color(0xFF00C853)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (user.status == "ACTIVE") "Ban User" else "Unban User", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, icon: ImageVector, color: Color = Color.White) {
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
