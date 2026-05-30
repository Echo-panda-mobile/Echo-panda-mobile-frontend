package com.example.echo_panda_mobile.presentation.views.artist

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.echo_panda_mobile.presentation.components.ArtistBottomBar
import com.example.echo_panda_mobile.presentation.navigation.Routes
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistProfileViewModel

@Composable
fun ArtistProfileScreen(
    onNavigate: (String) -> Unit,
    viewModel: ArtistProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.updateProfileImage(it.toString())
        }
    }

    Scaffold(
        containerColor = Color(0xFF05070D),
        bottomBar = { ArtistBottomBar(Routes.ARTIST_PROFILE, onNavigate) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EchoPandaColors.AccentBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                // Banner & Profile Picture
                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.8f)
                            .background(Brush.verticalGradient(listOf(EchoPandaColors.AccentBlue.copy(alpha = 0.3f), Color.Transparent)))
                    )
                    
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier.size(120.dp).clip(CircleShape),
                                color = Color(0xFF121A26)
                            ) {
                                if (uiState.user?.photoUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(uiState.user?.photoUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        null,
                                        modifier = Modifier.padding(30.dp),
                                        tint = Color.White
                                    )
                                }
                            }
                            IconButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(EchoPandaColors.AccentBlue)
                            ) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Show email if name is missing
                    val displayName = uiState.user?.name?.takeIf { it.isNotBlank() } ?: uiState.user?.email ?: "Artist Name"
                    
                    Text(
                        text = displayName,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Verified Artist",
                        color = EchoPandaColors.AccentBlue,
                        fontSize = 14.sp
                    )
                    
                    if (uiState.user?.name?.isNotBlank() == true) {
                        Text(
                            text = uiState.user?.email ?: "",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Bio
                    Text(
                        text = uiState.bio,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    // Settings options
                    ProfileOption(icon = Icons.Default.Edit, title = "Edit Bio & Socials")
                    ProfileOption(icon = Icons.Default.Share, title = "Share Profile")
                    ProfileOption(icon = Icons.Default.Settings, title = "Account Settings")
                    ProfileOption(icon = Icons.AutoMirrored.Filled.HelpOutline, title = "Support Center")
                    
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileOption(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f))
    }
}
