package com.example.echo_panda_mobile.presentation.views.artist

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.data.remote.AlbumDto
import com.example.echo_panda_mobile.data.remote.MbGenreDto
import com.example.echo_panda_mobile.data.remote.MbTagDto
import com.example.echo_panda_mobile.presentation.components.ArtistBottomBar
import com.example.echo_panda_mobile.presentation.navigation.Routes
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistUploadViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistUploadViewModelFactory
import java.util.*

internal data class FileInfo(val name: String, val size: Long, val duration: Int)

enum class UploadStep(val index: Int, val title: String) {
    AUDIO(0, "Audio"),
    DETAILS(1, "Details"),
    PUBLISH(2, "Publish")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistUploadScreen(
    currentUser: User?,
    onNavigate: (String) -> Unit,
    viewModel: ArtistUploadViewModel = viewModel(factory = ArtistUploadViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val uploadState by viewModel.uploadState.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val currentUploadingFile by viewModel.currentUploadingFile.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val isAlbumsLoading by viewModel.isAlbumsLoading.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val isCatalogLoading by viewModel.isCatalogLoading.collectAsState()
    
    var currentStep by remember { mutableStateOf(UploadStep.AUDIO) }
    
    // Form State
    var title by remember { mutableStateOf("") }
    var selectedGenreId by remember { mutableStateOf<String?>(null) }
    var selectedTagId by remember { mutableStateOf<String?>(null) }
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var audioInfo by remember { mutableStateOf<FileInfo?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAlbum by remember { mutableStateOf<AlbumDto?>(null) }
    
    var validationError by remember { mutableStateOf("") }

    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedAudioUri = it
            audioInfo = getFileInfo(context, it)
            validationError = ""
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
        validationError = ""
    }

    LaunchedEffect(uploadState) {
        if (uploadState is ArtistUploadViewModel.UploadUiState.Error) {
            Toast.makeText(context, (uploadState as ArtistUploadViewModel.UploadUiState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = Color(0xFF05070D),
        topBar = {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .background(Brush.verticalGradient(listOf(EchoPandaColors.AccentBlue.copy(alpha = 0.1f), Color.Transparent)))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "Upload Music", 
                    color = Color.White, 
                    fontSize = 22.sp, 
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(16.dp))
                StepIndicator(currentStep = currentStep)
            }
        },
        bottomBar = { 
            if (uploadState !is ArtistUploadViewModel.UploadUiState.Uploading) {
                ArtistBottomBar(Routes.ARTIST_UPLOAD, onNavigate) 
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(
                targetState = currentStep,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    if (targetState.index > initialState.index) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "StepTransition"
            ) { step ->
                when (step) {
                    UploadStep.AUDIO -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AudioUploadStep(
                                selectedAudioUri = selectedAudioUri,
                                audioInfo = audioInfo,
                                onSelectClick = { audioLauncher.launch("audio/*") },
                                onNext = { 
                                    if (selectedAudioUri != null) currentStep = UploadStep.DETAILS 
                                    else validationError = "Please select an audio file"
                                }
                            )
                        }
                    }
                    UploadStep.DETAILS -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            SongDetailsStep(
                                title = title,
                                onTitleChange = { title = it; validationError = "" },
                                genres = genres,
                                tags = tags,
                                isCatalogLoading = isCatalogLoading,
                                selectedGenreId = selectedGenreId,
                                onGenreSelected = { selectedGenreId = it; validationError = "" },
                                selectedTagId = selectedTagId,
                                onTagSelected = { selectedTagId = it; validationError = "" },
                                selectedAlbum = selectedAlbum,
                                onAlbumSelect = { selectedAlbum = it },
                                albums = albums,
                                isAlbumsLoading = isAlbumsLoading,
                                selectedImageUri = selectedImageUri,
                                onImageClick = { imageLauncher.launch("image/*") },
                                onBack = { currentStep = UploadStep.AUDIO },
                                onNext = {
                                    when {
                                        title.isBlank() -> validationError = "Song title is required"
                                        selectedGenreId.isNullOrBlank() -> validationError = "Genre is required"
                                        else -> currentStep = UploadStep.PUBLISH
                                    }
                                }
                            )
                        }
                    }
                    UploadStep.PUBLISH -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            ReviewPublishStep(
                                title = title,
                                genreName = genres.find { it.id == selectedGenreId }?.name ?: "—",
                                tagName = tags.find { it.id == selectedTagId }?.name,
                                albumName = selectedAlbum?.title ?: "Single",
                                audioInfo = audioInfo,
                                imageUri = selectedImageUri,
                                onBack = { currentStep = UploadStep.DETAILS },
                                onPublish = {
                                    viewModel.uploadSong(
                                        context = context,
                                        title = title.trim(),
                                        albumId = selectedAlbum?.id,
                                        categoryId = selectedGenreId,
                                        tagId = selectedTagId?.toIntOrNull(),
                                        lyrics = null,
                                        audioUri = selectedAudioUri,
                                        coverUri = selectedImageUri,
                                        trackNumber = 1
                                    )
                                }
                            )
                        }
                    }
                }
                
                if (validationError.isNotBlank()) {
                    Text(
                        validationError, 
                        color = Color.Red, 
                        fontSize = 12.sp, 
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp), 
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Global Upload Dialog / Success Screen
            when (uploadState) {
                is ArtistUploadViewModel.UploadUiState.Uploading -> {
                    UploadProgressDialog(
                        progress = uploadProgress,
                        statusMessage = (uploadState as ArtistUploadViewModel.UploadUiState.Uploading).message,
                        fileName = currentUploadingFile ?: "Processing..."
                    )
                }
                is ArtistUploadViewModel.UploadUiState.Success -> {
                    UploadSuccessView(
                        title = title,
                        albumName = selectedAlbum?.title ?: "Single",
                        imageUri = selectedImageUri,
                        onGoToMusic = { 
                            viewModel.resetState()
                            onNavigate(Routes.ARTIST_MY_MUSIC) 
                        },
                        onUploadAnother = {
                            viewModel.resetState()
                            // Reset form
                            title = ""
                            selectedGenreId = null
                            selectedTagId = null
                            selectedAudioUri = null
                            audioInfo = null
                            selectedImageUri = null
                            selectedAlbum = null
                            currentStep = UploadStep.AUDIO
                        }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun UploadSuccessView(
    title: String,
    albumName: String,
    imageUri: Uri?,
    onGoToMusic: () -> Unit,
    onUploadAnother: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2736)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF4ADE80).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4ADE80), modifier = Modifier.size(48.dp))
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Upload Complete!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Your song has been published successfully.", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, textAlign = TextAlign.Center)
                }
                
                // Song Preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray)) {
                        if (imageUri != null) {
                            AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(albumName, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onGoToMusic,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue)
                    ) {
                        Text("Go to My Music", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    
                    TextButton(
                        onClick = onUploadAnother,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Upload Another Song", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: UploadStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        UploadStep.entries.forEachIndexed { index, step ->
            val isCurrent = step == currentStep
            val isCompleted = step.index < currentStep.index
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> EchoPandaColors.AccentBlue
                                isCompleted -> Color(0xFF4ADE80)
                                else -> Color.White.copy(alpha = 0.1f)
                            }
                        )
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            (index + 1).toString(), 
                            color = if (isCurrent) Color.Black else Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    step.title, 
                    color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                )
            }
            
            if (index < UploadStep.entries.size - 1) {
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .width(40.dp)
                        .background(if (isCompleted) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.1f))
                )
            }
        }
    }
}

@Composable
private fun AudioUploadStep(
    selectedAudioUri: Uri?,
    audioInfo: FileInfo?,
    onSelectClick: () -> Unit,
    onNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF121A26))
                .border(
                    BorderStroke(2.dp, if (selectedAudioUri != null) EchoPandaColors.AccentBlue else Color.White.copy(alpha = 0.05f)),
                    RoundedCornerShape(24.dp)
                )
                .clickable { onSelectClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CloudUpload, 
                    null, 
                    tint = EchoPandaColors.AccentBlue, 
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("Select your audio file here", color = Color.White, fontWeight = FontWeight.Bold)
                Text("MP3, WAV, M4A up to 100MB", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onSelectClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Browse Files", color = Color.White)
                }
            }
        }
        
        if (selectedAudioUri != null && audioInfo != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2736)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AudioFile, null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(audioInfo.name, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            "${formatTime(audioInfo.duration)} • ${String.format(Locale.getDefault(), "%.1f", audioInfo.size / (1024.0 * 1024.0))} MB",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4ADE80))
                }
            }
        }
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = selectedAudioUri != null,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue)
        ) {
            Text("Next", color = Color.Black, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.Black)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SongDetailsStep(
    title: String,
    onTitleChange: (String) -> Unit,
    genres: List<MbGenreDto>,
    tags: List<MbTagDto>,
    isCatalogLoading: Boolean,
    selectedGenreId: String?,
    onGenreSelected: (String) -> Unit,
    selectedTagId: String?,
    onTagSelected: (String?) -> Unit,
    selectedAlbum: AlbumDto?,
    onAlbumSelect: (AlbumDto?) -> Unit,
    albums: List<AlbumDto>,
    isAlbumsLoading: Boolean,
    selectedImageUri: Uri?,
    onImageClick: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var showAlbumDropdown by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Song Title
        UploadField(
            label = "Song Title *", 
            value = title, 
            onValueChange = onTitleChange, 
            placeholder = "Enter song title",
            maxChars = 100,
            helperText = "${title.length}/100"
        )
        
        // Album Selection
        Column {
            Text("Select Album (Optional)", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Box {
                OutlinedCard(
                    onClick = { showAlbumDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF121A26)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            selectedAlbum?.title ?: "Select an album", 
                            color = if (selectedAlbum != null) Color.White else Color.Gray,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, null, tint = Color.White.copy(alpha = 0.5f))
                    }
                }
                
                DropdownMenu(
                    expanded = showAlbumDropdown,
                    onDismissRequest = { showAlbumDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.85f).background(Color(0xFF1E2736))
                ) {
                    if (isAlbumsLoading && albums.isEmpty()) {
                        DropdownMenuItem(text = { Text("Loading...", color = Color.White) }, onClick = {})
                    }
                    
                    DropdownMenuItem(
                        text = { Text("None (Single)", color = Color.White) },
                        onClick = { onAlbumSelect(null); showAlbumDropdown = false }
                    )
                    
                    albums.forEach { album ->
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = album.getDisplayCoverUrl(),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(album.title, color = Color.White)
                                }
                            },
                            onClick = { onAlbumSelect(album); showAlbumDropdown = false }
                        )
                    }
                }
            }
        }
        
        // Artwork Upload
        Column {
            Text("Cover Artwork *", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF121A26))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                        .clickable { onImageClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(32.dp))
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text("High resolution 1:1 image recommended.", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onImageClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (selectedImageUri != null) "Change Image" else "Select Image", fontSize = 12.sp)
                    }
                }
            }
        }

        UploadGenreDropdown(
            genres = genres,
            selectedId = selectedGenreId,
            enabled = !isCatalogLoading,
            onSelected = onGenreSelected,
        )

        UploadTagDropdown(
            tags = tags,
            selectedId = selectedTagId,
            enabled = !isCatalogLoading,
            onSelected = onTagSelected,
        )

        if (isCatalogLoading && genres.isEmpty()) {
            Text(
                "Loading genres and tags…",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text("Back", color = Color.White)
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue)
            ) {
                Text("Next", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ReviewPublishStep(
    title: String,
    genreName: String,
    tagName: String?,
    albumName: String,
    audioInfo: FileInfo?,
    imageUri: Uri?,
    onBack: () -> Unit,
    onPublish: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("Review Your Song", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121A26)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black)
                ) {
                    if (imageUri != null) {
                        AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.MusicNote, null, modifier = Modifier.align(Alignment.Center).size(40.dp), tint = Color.White.copy(alpha = 0.2f))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Song Title", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Album", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    Text(albumName, color = Color.White, fontSize = 14.sp)
                }
            }
        }
        
        SummaryItem(icon = Icons.Default.AudioFile, label = "Audio File", value = audioInfo?.name ?: "Unknown")
        SummaryItem(icon = Icons.Default.Timer, label = "Duration", value = audioInfo?.let { formatTime(it.duration) } ?: "0:00")
        SummaryItem(icon = Icons.Default.Category, label = "Genre", value = genreName)
        if (!tagName.isNullOrBlank()) {
            SummaryItem(icon = Icons.Default.Label, label = "Tag", value = tagName)
        }
        
        Spacer(Modifier.weight(1f))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text("Back", color = Color.White)
            }
            Button(
                onClick = onPublish,
                modifier = Modifier.weight(1.5f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue)
            ) {
                Icon(Icons.Default.Publish, null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text("Publish Song", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun SummaryItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun UploadProgressDialog(
    progress: Int,
    statusMessage: String,
    fileName: String
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2736)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier.size(60.dp).clip(CircleShape).background(EchoPandaColors.AccentBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CloudUpload, null, tint = EchoPandaColors.AccentBlue, modifier = Modifier.size(32.dp))
                }
                
                Text("Uploading Music...", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                Text(
                    "$progress%", 
                    color = EchoPandaColors.AccentBlue, 
                    fontSize = 32.sp, 
                    fontWeight = FontWeight.Black
                )
                
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = EchoPandaColors.AccentBlue,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(statusMessage, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(fileName, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                
                Text("This may take a few moments", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadGenreDropdown(
    genres: List<MbGenreDto>,
    selectedId: String?,
    enabled: Boolean,
    onSelected: (String) -> Unit,
) {
    UploadCatalogDropdown(
        label = "Genre *",
        options = genres.map { it.id to it.name },
        selectedId = selectedId,
        enabled = enabled,
        placeholder = "Select genre",
        onSelected = onSelected,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadTagDropdown(
    tags: List<MbTagDto>,
    selectedId: String?,
    enabled: Boolean,
    onSelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = tags.find { it.id == selectedId }?.name ?: "None"

    Column {
        Text("Tag", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded },
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = uploadDropdownColors(),
                shape = RoundedCornerShape(12.dp),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1E2736)),
            ) {
                DropdownMenuItem(
                    text = { Text("None", color = Color.White) },
                    onClick = {
                        onSelected(null)
                        expanded = false
                    },
                )
                tags.forEach { tag ->
                    DropdownMenuItem(
                        text = { Text(tag.name, color = Color.White) },
                        onClick = {
                            onSelected(tag.id)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadCatalogDropdown(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    enabled: Boolean,
    placeholder: String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.first == selectedId }?.second ?: placeholder

    Column {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded },
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = uploadDropdownColors(),
                shape = RoundedCornerShape(12.dp),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1E2736)),
            ) {
                if (options.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No options available", color = Color.White.copy(alpha = 0.5f)) },
                        onClick = { expanded = false },
                        enabled = false,
                    )
                } else {
                    options.forEach { (id, name) ->
                        DropdownMenuItem(
                            text = { Text(name, color = Color.White) },
                            onClick = {
                                onSelected(id)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun uploadDropdownColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = EchoPandaColors.AccentBlue,
    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
    focusedContainerColor = Color(0xFF121A26),
    unfocusedContainerColor = Color(0xFF121A26),
    disabledTextColor = Color.White.copy(alpha = 0.5f),
    disabledBorderColor = Color.White.copy(alpha = 0.05f),
    disabledContainerColor = Color(0xFF121A26).copy(alpha = 0.5f),
)

@Composable
private fun UploadField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    maxChars: Int = Int.MAX_VALUE,
    helperText: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (helperText != null) {
                Text(helperText, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
            }
        }
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
                focusedContainerColor = Color(0xFF121A26),
                unfocusedContainerColor = Color(0xFF121A26)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 4
        )
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", mins, secs)
}

private fun getFileInfo(context: android.content.Context, uri: Uri): FileInfo {
    var name = "Unknown file"
    var size = 0L
    var duration = 0
    
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (nameIndex != -1) name = cursor.getString(nameIndex)
            if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
        }
    }
    
    try {
        val retriever = android.media.MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        duration = (retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L).toInt() / 1000
        retriever.release()
    } catch (e: Exception) { }
    
    return FileInfo(name, size, duration)
}
