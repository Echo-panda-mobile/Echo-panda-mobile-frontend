@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.echo_panda_mobile.presentation.views.user.profile

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.echo_panda_mobile.presentation.viewsmodel.UserProfileViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.UserProfileViewModelFactory
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors

@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onEditProfile: () -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: UserProfileViewModel = viewModel(factory = UserProfileViewModelFactory(LocalContext.current))
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

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = EchoPandaColors.BgCardDark,
            title = { Text("Log Out", color = Color.White) },
            text = { Text("Are you sure you want to log out?", color = EchoPandaColors.TextMutedDark) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout(onLogoutSuccess)
                }) {
                    Text("Log Out", color = EchoPandaColors.ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var newName by remember { mutableStateOf(uiState.user?.name ?: "") }
        var newEmail by remember { mutableStateOf(uiState.user?.email ?: "") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = EchoPandaColors.BgCardDark,
            title = { Text("Edit Profile", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Name") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(newName, newEmail)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue)
                ) {
                    Text("Save", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(EchoPandaColors.BgCardDark, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = EchoPandaColors.AccentBlue)
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSettings,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .background(EchoPandaColors.BgCardDark, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Settings, "Settings", tint = EchoPandaColors.AccentBlue)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = EchoPandaColors.BgDarkEnd
                )
            )
        },
        containerColor = EchoPandaColors.BgDarkEnd
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EchoPandaColors.AccentBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Profile Image
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F2537)),
                        contentAlignment = Alignment.Center
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
                                contentDescription = null,
                                modifier = Modifier.size(70.dp),
                                tint = EchoPandaColors.AccentBlue
                            )
                        }
                    }
                    
                    // Edit Profile Picture Icon
                    IconButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EchoPandaColors.AccentBlue)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Change Profile Picture",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name and Email
                Text(
                    text = uiState.user?.name ?: "John Doe",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = uiState.user?.email ?: "john.doe@example.com",
                    fontSize = 14.sp,
                    color = EchoPandaColors.TextMutedDark
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileStatCard(
                        count = uiState.playlists.size.toString(),
                        label = "Playlists",
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        count = "0",
                        label = "Followers",
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        count = uiState.followingCount.toString(),
                        label = "Following",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Account Actions Section
                Text(
                    text = "Account Actions",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AccountActionItem(
                        icon = Icons.Default.Edit,
                        title = "Edit Profile",
                        subtitle = "Update your information",
                        onClick = { showEditProfileDialog = true }
                    )
                    AccountActionItem(
                        icon = Icons.Default.Favorite,
                        title = "Liked Songs",
                        subtitle = "View your saved songs",
                        onClick = { /* Navigate to Liked Songs */ }
                    )
                    AccountActionItem(
                        icon = Icons.Default.FileDownload,
                        title = "Download Management",
                        subtitle = "Manage your downloads",
                        onClick = { /* Navigate to Downloads */ }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Log Out Button
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ProfileStatCard(count: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(EchoPandaColors.BgCardDark, RoundedCornerShape(16.dp))
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = EchoPandaColors.AccentBlue
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = EchoPandaColors.TextMutedDark
        )
    }
}

@Composable
fun AccountActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(EchoPandaColors.BgCardDark, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EchoPandaColors.AccentBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Text(text = subtitle, color = EchoPandaColors.TextMutedDark, fontSize = 12.sp)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = EchoPandaColors.TextMutedDark
        )
    }
}
