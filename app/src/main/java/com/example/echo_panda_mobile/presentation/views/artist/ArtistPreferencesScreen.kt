package com.example.echo_panda_mobile.presentation.views.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Visibility
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
fun ArtistPreferencesScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    
    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Preferences", color = Color.White, fontWeight = FontWeight.Black) },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("App Experience", color = EchoPandaColors.AccentBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            PreferenceToggle(title = "Push Notifications", icon = Icons.Default.Notifications, initialValue = true)
            PreferenceToggle(title = "Email Analytics Reports", icon = Icons.Default.Visibility, initialValue = false)
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            
            Text("Content", color = EchoPandaColors.AccentBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            PreferenceItem(title = "App Language", value = "English", icon = Icons.Default.Language)
        }
    }
}

@Composable
private fun PreferenceToggle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, initialValue: Boolean) {
    var checked by remember { mutableStateOf(initialValue) }
    
    Surface(
        color = Color(0xFF121A26),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.6f))
            Spacer(Modifier.width(16.dp))
            Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = checked, 
                onCheckedChange = { checked = it },
                colors = SwitchDefaults.colors(checkedThumbColor = EchoPandaColors.AccentBlue)
            )
        }
    }
}

@Composable
private fun PreferenceItem(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color = Color(0xFF121A26),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.6f))
            Spacer(Modifier.width(16.dp))
            Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Text(value, color = EchoPandaColors.AccentBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
