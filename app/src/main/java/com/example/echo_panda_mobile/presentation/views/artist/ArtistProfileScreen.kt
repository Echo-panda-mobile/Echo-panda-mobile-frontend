package com.example.echo_panda_mobile.presentation.views.artist

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistProfileViewModelFactory

@Composable
fun ArtistProfileScreen(
    onNavigate: (String) -> Unit,
    viewModel: ArtistProfileViewModel = viewModel(factory = ArtistProfileViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.updateProfileImage(context, it)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color(0xFF05070D),
        bottomBar = { ArtistBottomBar(Routes.ARTIST_PROFILE, onNavigate) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading && uiState.user == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = EchoPandaColors.AccentBlue
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                ) {
                    // Premium Header with Gradient
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        // Background Gradient
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.7f)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            EchoPandaColors.AccentBlue.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        
                        // Profile Info Overlay
                        Column(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Surface(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF121A26)),
                                    border = androidx.compose.foundation.BorderStroke(4.dp, Color(0xFF05070D))
                                ) {
                                    val photoUrl = uiState.artistImageUrl
                                    if (!photoUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(photoUrl)
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
                                            modifier = Modifier.padding(35.dp),
                                            tint = Color.White.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                                
                                FloatingActionButton(
                                    onClick = { photoPickerLauncher.launch("image/*") },
                                    modifier = Modifier.size(40.dp),
                                    containerColor = EchoPandaColors.AccentBlue,
                                    contentColor = Color.Black,
                                    shape = CircleShape
                                ) {
                                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(20.dp))
                                }
                            }
                            
                            Spacer(Modifier.height(16.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.user?.name ?: "Artist Name",
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Verified,
                                    null,
                                    tint = EchoPandaColors.AccentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Independent Artist",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        
                        Spacer(Modifier.height(32.dp))
                        
                        // Bio Section
                        Surface(
                            color = Color(0xFF121A26),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Biography",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = uiState.bio.trim().ifBlank { "no bio yet" },
                                    color = Color.White.copy(alpha = if (uiState.bio.isBlank()) 0.4f else 0.7f),
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(32.dp))
                        
                        // Artist Portfolio Section
                        Text(
                            "Artist Portfolio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )
                        
                        ProfileOption(
                            icon = Icons.Default.MusicNote, 
                            title = "My Catalog",
                            onClick = { onNavigate(Routes.ARTIST_MY_MUSIC) }
                        )
                        ProfileOption(
                            icon = Icons.Default.Album, 
                            title = "Manage Albums",
                            isLast = true,
                            onClick = { onNavigate(Routes.ARTIST_ALBUMS) }
                        )

                        
                        Spacer(Modifier.height(32.dp))
                        
                        // Account Settings Section
                        Text(
                            "Account Settings",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )
                        
                        ProfileOption(
                            icon = Icons.Default.Edit, 
                            title = "Edit Profile Info",
                            onClick = { onNavigate(Routes.ARTIST_EDIT_PROFILE) }
                        )
                        ProfileOption(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            title = "Logout",
                            isLast = true,
                            onClick = { 
                                viewModel.logout()
                                onNavigate(Routes.LOGIN)
                            }
                        )

                        
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
            
            if (uiState.isUpdating) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EchoPandaColors.AccentBlue)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
    }
}

@Composable
private fun ProfileOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    title: String,
    isLast: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        color = Color(0xFF121A26),
        shape = when {
            isLast -> RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
            else -> RoundedCornerShape(0.dp)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EchoPandaColors.AccentBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(16.dp))
                Text(title, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f))
            }
            if (!isLast) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White.copy(alpha = 0.05f)
                )
            }
        }
    }
}
