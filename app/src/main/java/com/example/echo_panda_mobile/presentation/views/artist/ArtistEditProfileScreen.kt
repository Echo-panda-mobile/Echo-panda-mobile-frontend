package com.example.echo_panda_mobile.presentation.views.artist

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistProfileViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistEditProfileScreen(
    onBack: () -> Unit,
    viewModel: ArtistProfileViewModel = viewModel(factory = ArtistProfileViewModelFactory(LocalContext.current)),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.updateSuccess.collect {
            onBack()
        }
    }
    
    LaunchedEffect(uiState.user, uiState.bio) {
        uiState.user?.let {
            name = it.name
            bio = uiState.bio
        }
    }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            viewModel.updateProfileImage(context, it)
        }
    }

    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp).background(Color.White.copy(alpha = 0.05f), CircleShape),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { 
                            viewModel.updateProfile(name, bio)
                        },
                        enabled = !uiState.isUpdating,
                    ) {
                        Text("Save", color = EchoPandaColors.AccentBlue, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Profile Image Upload Section
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF121A26)),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.1f)),
                    ) {
                        val displayImage = selectedImageUri
                            ?: uiState.artistImageUrl?.takeIf { it.isNotBlank() }
                        if (displayImage != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(displayImage)
                                    .crossfade(enable = true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                null,
                                modifier = Modifier.padding(30.dp),
                                tint = Color.White.copy(alpha = 0.2f),
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(36.dp)
                            .background(EchoPandaColors.AccentBlue, CircleShape)
                            .border(2.dp, Color(0xFF05070D), CircleShape),
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp), tint = Color.Black)
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                // Form Fields
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    EditField(label = "Display Name", value = name, onValueChange = { name = it })
                    ReadOnlyField(label = "Email", value = uiState.user?.email.orEmpty())
                    EditField(label = "Biography", value = bio, onValueChange = { bio = it }, singleLine = false)
                }
            }
            
            if (uiState.isUpdating) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.4f),
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
private fun ReadOnlyField(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.White.copy(alpha = 0.6f),
                disabledBorderColor = Color.White.copy(alpha = 0.08f),
                disabledContainerColor = Color(0xFF0D121C),
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )
    }
}

@Composable
private fun EditField(label: String, value: String, onValueChange: (String) -> Unit, singleLine: Boolean = true) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = EchoPandaColors.AccentBlue,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = Color(0xFF121A26),
                unfocusedContainerColor = Color(0xFF121A26),
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 4
        )
    }
}
