package com.example.echo_panda_mobile.presentation.views.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo_panda_mobile.presentation.components.ArtistBottomBar
import com.example.echo_panda_mobile.presentation.navigation.Routes
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors

@Composable
fun ArtistAnalyticsScreen(
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            Box(modifier = Modifier.statusBarsPadding().padding(16.dp)) {
                Text("Insights & Analytics", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = { ArtistBottomBar(Routes.ARTIST_ANALYTICS, onNavigate) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Main Chart placeholder
            Surface(
                modifier = Modifier.fillMaxWidth().height(260.dp),
                color = Color(0xFF121A26),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Streams", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            Text("2.5M", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        }
                        Icon(Icons.AutoMirrored.Filled.ShowChart, null, tint = EchoPandaColors.AccentBlue)
                    }
                    
                    Spacer(Modifier.weight(1f))
                    
                    // Simple Mock Chart
                    Row(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        listOf(0.4f, 0.6f, 0.5f, 0.9f, 0.7f, 0.8f, 1f).forEach { height ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(height)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        Brush.verticalGradient(listOf(EchoPandaColors.AccentBlue, Color.Transparent))
                                    )
                            )
                        }
                    }
                }
            }

            // Stats row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AnalyticsSmallCard(title = "Followers Growth", value = "+850", modifier = Modifier.weight(1f))
                AnalyticsSmallCard(title = "Avg. Listen Time", value = "3:42", modifier = Modifier.weight(1f))
            }

            // Top Countries
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF121A26),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Top Countries", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    CountryRow("United States", "45%")
                    CountryRow("United Kingdom", "20%")
                    CountryRow("Germany", "15%")
                    CountryRow("Canada", "10%")
                }
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun AnalyticsSmallCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(100.dp),
        color = Color(0xFF121A26),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text(title, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CountryRow(name: String, percent: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Text(percent, color = EchoPandaColors.AccentBlue, fontWeight = FontWeight.Bold)
    }
}
