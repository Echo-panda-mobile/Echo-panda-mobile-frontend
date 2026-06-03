package com.example.echo_panda_mobile.presentation.views.artist

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.presentation.navigation.Routes
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistUploadViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistUploadViewModelFactory
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistCreateAlbumScreen(
    currentUser: User?,
    onBack: () -> Unit,
    viewModel: ArtistUploadViewModel = viewModel(factory = ArtistUploadViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val uploadState by viewModel.uploadState.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val currentUploadingFile by viewModel.currentUploadingFile.collectAsState()
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    LaunchedEffect(uploadState) {
        if (uploadState is ArtistUploadViewModel.UploadUiState.Success) {
            Toast.makeText(context, (uploadState as ArtistUploadViewModel.UploadUiState.Success).message, Toast.LENGTH_SHORT).show()
            viewModel.loadMyAlbums() // Refresh albums list
            onBack()
        } else if (uploadState is ArtistUploadViewModel.UploadUiState.Error) {
            Toast.makeText(context, (uploadState as ArtistUploadViewModel.UploadUiState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Album", color = Color.White, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp).background(Color.White.copy(alpha = 0.05f), CircleShape),
                        enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Background subtle gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(EchoPandaColors.AccentBlue.copy(alpha = 0.05f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Upload Progress Indicator
                if (uploadState is ArtistUploadViewModel.UploadUiState.Uploading) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2736))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                (uploadState as ArtistUploadViewModel.UploadUiState.Uploading).message,
                                color = EchoPandaColors.AccentBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            
                            LinearProgressIndicator(
                                progress = { uploadProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = EchoPandaColors.AccentBlue,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "$uploadProgress%",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    currentUploadingFile ?: "Processing...",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Album Cover Picker with Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF121A26))
                        .border(
                            1.dp, 
                            if (selectedImageUri != null) EchoPandaColors.AccentBlue else Color.White.copy(alpha = 0.05f), 
                            RoundedCornerShape(24.dp)
                        )
                        .clickable(enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading) { imageLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(56.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Select Album Artwork", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("1:1 ratio, min 1000x1000px", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                        }
                    } else {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Album Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Change button overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Album Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Album Title", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EchoPandaColors.AccentBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = Color(0xFF121A26),
                            unfocusedContainerColor = Color(0xFF121A26),
                            disabledTextColor = Color.White.copy(alpha = 0.5f),
                            disabledBorderColor = Color.White.copy(alpha = 0.05f),
                            disabledContainerColor = Color(0xFF121A26).copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Description (Optional)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EchoPandaColors.AccentBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = Color(0xFF121A26),
                            unfocusedContainerColor = Color(0xFF121A26),
                            disabledTextColor = Color.White.copy(alpha = 0.5f),
                            disabledBorderColor = Color.White.copy(alpha = 0.05f),
                            disabledContainerColor = Color(0xFF121A26).copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading
                    )
                }

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.createAlbum(
                                context = context,
                                title = title,
                                artist = currentUser?.name ?: "Unknown Artist",
                                description = description.ifBlank { null },
                                coverUri = selectedImageUri
                            )
                        } else {
                            Toast.makeText(context, "Album title is required", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue)
                ) {
                    if (uploadState is ArtistUploadViewModel.UploadUiState.Uploading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Text("Create Album", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
                
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
