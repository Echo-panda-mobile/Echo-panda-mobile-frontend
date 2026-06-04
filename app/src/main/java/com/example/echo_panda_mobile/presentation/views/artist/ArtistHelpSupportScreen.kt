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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistHelpSupportScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    
    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Help & Support", color = Color.White, fontWeight = FontWeight.Black) },
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
            Text("Contact Us", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            
            SupportCard(title = "Live Chat", subtitle = "Talk to our support team now", icon = Icons.Default.Chat)
            SupportCard(title = "Email Support", subtitle = "support@echopanda.me", icon = Icons.Default.Email)
            
            Spacer(Modifier.height(8.dp))
            Text("Resources", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            
            SupportCard(title = "Artist Guide", subtitle = "How to maximize your reach", icon = Icons.Default.MenuBook)
            SupportCard(title = "FAQ", subtitle = "Frequently asked questions", icon = Icons.Default.QuestionAnswer)
            SupportCard(title = "Terms of Service", subtitle = "Our legal agreement", icon = Icons.Default.Description)
        }
    }
}

@Composable
private fun SupportCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
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
                modifier = Modifier.size(44.dp).background(EchoPandaColors.AccentBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = EchoPandaColors.AccentBlue)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f))
        }
    }
}
