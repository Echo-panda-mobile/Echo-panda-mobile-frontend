package com.example.echo_panda_mobile.presentation.views.artist

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistSecurityScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    
    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Account Security", color = Color.White, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SecurityItem(title = "Change Password", icon = Icons.Default.Lock)
            SecurityItem(title = "Two-Factor Authentication", icon = Icons.Default.Security, hasToggle = true)
            SecurityItem(title = "Biometric Login", icon = Icons.Default.Fingerprint, hasToggle = true)
            SecurityItem(title = "Manage Devices", icon = Icons.Default.Devices)
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C0B0B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Deactivate Account", color = Color(0xFFFFB4B4), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SecurityItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, hasToggle: Boolean = false) {
    var checked by remember { mutableStateOf(false) }
    
    Surface(
        color = Color(0xFF121A26),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(EchoPandaColors.AccentBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(title, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
            
            if (hasToggle) {
                Switch(
                    checked = checked, 
                    onCheckedChange = { checked = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = EchoPandaColors.AccentBlue)
                )
            } else {
                Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}
