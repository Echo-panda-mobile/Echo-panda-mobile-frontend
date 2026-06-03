package com.example.echo_panda_mobile.presentation.views.artist

import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import  androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.data.remote.SongDto
import com.example.echo_panda_mobile.presentation.navigation.Routes
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistUploadViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistUploadViewModelFactory
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistEditSongScreen(
    songId: String,
    currentUser: User?,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ArtistUploadViewModel = viewModel(factory = ArtistUploadViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val uploadState by viewModel.uploadState.collectAsState()
    val scrollState2 = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("") }
    var lyrics by remember { mutableStateOf("") }
    var bpm by remember { mutableStateOf("") }
    var isExplicit by remember { mutableStateOf(false) }
    var artists by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showAdvanced by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    LaunchedEffect(uploadState) {
        if (uploadState is ArtistUploadViewModel.UploadUiState.Success) {
            Toast.makeText(context, (uploadState as ArtistUploadViewModel.UploadUiState.Success).message, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onNavigate(Routes.ARTIST_MY_MUSIC)
        } else if (uploadState is ArtistUploadViewModel.UploadUiState.Error) {
            Toast.makeText(context, (uploadState as ArtistUploadViewModel.UploadUiState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    // TODO: Load song details once backend is connected
    LaunchedEffect(Unit) {
        isLoading = false
        // Call viewModel to load song details
    }

    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            TopAppBar(
                title = { Text("Edit Song", fontWeight = FontWeight.Black, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF05070D),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EchoPandaColors.AccentBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState2)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Validation Error Display
                if (validationError.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3D0F0F))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, null, tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
                            Text(validationError, color = Color(0xFFEF5350), fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Cover Art Preview and Upload
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF121A26), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable(enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading) { imageLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Cover Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cover Artwork", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Update artwork", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Change Cover",
                            color = EchoPandaColors.AccentBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading) { imageLauncher.launch("image/*") }
                        )
                    }
                }

                // Form Fields
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    EditSongField(
                        label = "Song Title *",
                        value = title,
                        onValueChange = { title = it; validationError = "" },
                        placeholder = "Enter song title",
                        enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading,
                        maxChars = 100
                    )

                    Text("${title.length}/100", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)

                    EditSongField(
                        label = "Genre *",
                        value = genre,
                        onValueChange = { genre = it; validationError = "" },
                        placeholder = "e.g. Pop, Rock, Indie",
                        enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading
                    )

                    EditSongField(
                        label = "Mood",
                        value = mood,
                        onValueChange = { mood = it },
                        placeholder = "e.g. Happy, Sad, Energetic",
                        enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading
                    )

                    // Advanced Fields Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showAdvanced = !showAdvanced }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Advanced Settings", color = EchoPandaColors.AccentBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Icon(if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = EchoPandaColors.AccentBlue)
                    }

                    if (showAdvanced) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EditSongField(
                                label = "BPM",
                                value = bpm,
                                onValueChange = { bpm = it },
                                placeholder = "e.g. 120",
                                enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading,
                                modifier = Modifier.weight(0.4f)
                            )

                            Column(modifier = Modifier.weight(0.6f)) {
                                Text("Explicit Content", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(8.dp))
                                Switch(
                                    checked = isExplicit,
                                    onCheckedChange = { isExplicit = it },
                                    enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading,
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        }

                        EditSongField(
                            label = "Featured Artists",
                            value = artists,
                            onValueChange = { artists = it },
                            placeholder = "Separate names with commas",
                            enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading,
                            singleLine = false
                        )
                    }

                    EditSongField(
                        label = "Lyrics",
                        value = lyrics,
                        onValueChange = { lyrics = it },
                        placeholder = "Paste your lyrics here...",
                        enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading,
                        singleLine = false,
                        maxChars = 10000
                    )

                    Text("${lyrics.length}/10000", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            // Validation
                            when {
                                title.isBlank() -> validationError = "Song title is required"
                                title.length < 3 -> validationError = "Title must be at least 3 characters"
                                genre.isBlank() -> validationError = "Genre is required"
                                bpm.isNotBlank() && bpm.toIntOrNull() == null -> validationError = "BPM must be a valid number"
                                bpm.isNotBlank() && (bpm.toInt() < 20 || bpm.toInt() > 320) -> validationError = "BPM must be between 20 and 320"
                                else -> {
                                    validationError = ""
                                    val coverFile = selectedImageUri?.let {
                                        val inputStream = context.contentResolver.openInputStream(it)
                                        val file = File(context.cacheDir, "cover_${System.currentTimeMillis()}")
                                        val outputStream = FileOutputStream(file)
                                        inputStream?.copyTo(outputStream)
                                        outputStream.close()
                                        inputStream?.close()
                                        file
                                    }
                                    
                                    // TODO: Call update song API
                                    Toast.makeText(context, "Song updated successfully", Toast.LENGTH_SHORT).show()
                                    onNavigate(Routes.ARTIST_MY_MUSIC)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue)
                    ) {
                        Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }

                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(0.5f)
                            .height(60.dp),
                        enabled = uploadState !is ArtistUploadViewModel.UploadUiState.Uploading,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun EditSongField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    maxChars: Int = Int.MAX_VALUE,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= maxChars) onValueChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = EchoPandaColors.AccentBlue,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = Color(0xFF121A26).copy(alpha = 0.5f),
                unfocusedContainerColor = Color(0xFF121A26).copy(alpha = 0.5f),
                disabledTextColor = Color.White.copy(alpha = 0.5f),
                disabledBorderColor = Color.White.copy(alpha = 0.05f),
                disabledContainerColor = Color(0xFF121A26).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 4,
            enabled = enabled
        )
    }
}
