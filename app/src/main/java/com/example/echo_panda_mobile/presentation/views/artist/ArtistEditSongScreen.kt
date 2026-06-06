package com.example.echo_panda_mobile.presentation.views.artist

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.echo_panda_mobile.data.remote.MbGenreDto
import com.example.echo_panda_mobile.data.remote.MbTagDto
import com.example.echo_panda_mobile.presentation.theme.EchoPandaColors
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistEditSongViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.ArtistEditSongViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistEditSongScreen(
    songId: String,
    onBack: () -> Unit,
    viewModel: ArtistEditSongViewModel = viewModel(factory = ArtistEditSongViewModelFactory(LocalContext.current)),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var lyrics by remember { mutableStateOf("") }
    var selectedGenreId by remember { mutableStateOf<String?>(null) }
    var selectedTagId by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var validationError by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    LaunchedEffect(songId) {
        android.util.Log.d("ArtistEditSong", "Screen open songId=$songId")
        viewModel.load(songId)
    }

    LaunchedEffect(uiState.song) {
        uiState.song?.let { song ->
            android.util.Log.d(
                "ArtistEditSong",
                "Form populated id=${song.id} title=${song.title} albumId=${song.albumId} " +
                    "categoryId=${song.categoryId} tagId=${song.tagId}"
            )
            title = song.title.orEmpty()
            lyrics = song.lyrics.orEmpty()
            selectedGenreId = song.categoryId?.toString()
            selectedTagId = song.tagId?.toString()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collect {
            Toast.makeText(context, "Song updated successfully", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.deleteSuccess.collect {
            Toast.makeText(context, "Song deleted successfully", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            android.util.Log.e("ArtistEditSong", "UI error: $it")
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            // Keep message on screen when load failed (song still null)
            if (uiState.song != null) {
                viewModel.clearError()
            }
        }
    }

    val canSave = uiState.song != null && !uiState.isSaving && !uiState.isDeleting

    fun attemptSave() {
        android.util.Log.d("ArtistEditSong", "Save clicked title='$title' genreId=$selectedGenreId songLoaded=${uiState.song != null}")
        when {
            uiState.song == null -> {
                validationError = "Song not loaded. Go back and try again."
                Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
            }
            title.isBlank() -> {
                validationError = "Song title is required"
                Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
            }
            title.length < 3 -> {
                validationError = "Title must be at least 3 characters"
                Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
            }
            selectedGenreId.isNullOrBlank() -> {
                validationError = "Genre is required"
                Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
            }
            else -> {
                validationError = ""
                android.util.Log.d(
                    "ArtistEditSong",
                    "Validation passed — invoking save songId=$songId categoryId=$selectedGenreId tagId=$selectedTagId"
                )
                viewModel.save(
                    context = context,
                    songId = songId,
                    title = title,
                    categoryId = selectedGenreId!!.toInt(),
                    tagId = selectedTagId?.toIntOrNull(),
                    lyrics = lyrics,
                    coverUri = selectedImageUri,
                )
            }
        }
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
                actions = {
                    TextButton(
                        onClick = { attemptSave() },
                        enabled = canSave,
                    ) {
                        Text(
                            "Save",
                            color = if (canSave) EchoPandaColors.AccentBlue else Color.Gray,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF05070D),
                    titleContentColor = Color.White,
                ),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = EchoPandaColors.AccentBlue,
                )
            }
            uiState.song == null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        uiState.errorMessage ?: "Could not load this song.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.load(songId) },
                        colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue),
                    ) {
                        Text("Retry", color = Color.Black)
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    if (validationError.isNotBlank()) {
                        ErrorBanner(validationError)
                    }

                    CoverSection(
                        imageModel = selectedImageUri ?: uiState.song?.coverUrl ?: uiState.song?.getDisplayCoverUrl(),
                        enabled = !uiState.isSaving,
                        onPickImage = { imageLauncher.launch("image/*") },
                    )

                    EditSongField(
                        label = "Song Title *",
                        value = title,
                        onValueChange = { title = it; validationError = "" },
                        placeholder = "Enter song title",
                        enabled = !uiState.isSaving,
                        maxChars = 100,
                    )
                    Text("${title.length}/100", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)

                    GenreDropdown(
                        label = "Genre *",
                        options = uiState.genres,
                        selectedId = selectedGenreId,
                        enabled = !uiState.isSaving,
                        onSelected = {
                            selectedGenreId = it
                            validationError = ""
                        },
                    )

                    TagDropdown(
                        label = "Tag",
                        options = uiState.tags,
                        selectedId = selectedTagId,
                        enabled = !uiState.isSaving,
                        onSelected = { selectedTagId = it },
                    )

                    EditSongField(
                        label = "Lyrics",
                        value = lyrics,
                        onValueChange = { lyrics = it },
                        placeholder = "Paste your lyrics here...",
                        enabled = !uiState.isSaving,
                        singleLine = false,
                        maxChars = 10000,
                    )
                    Text("${lyrics.length}/10000", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { attemptSave() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = canSave,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EchoPandaColors.AccentBlue),
                    ) {
                        Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = canSave,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, EchoPandaColors.ErrorRed.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoPandaColors.ErrorRed),
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Song", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
            }

            if (uiState.isSaving || uiState.isDeleting) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.45f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EchoPandaColors.AccentBlue)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF121A26),
            title = { Text("Delete Song", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete \"${uiState.song?.title ?: "this song"}\"? This action cannot be undone.",
                    color = Color.White.copy(alpha = 0.7f),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSong(songId)
                    },
                ) {
                    Text("Delete", color = EchoPandaColors.ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3D0F0F)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Error, null, tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
            Text(message, color = Color(0xFFEF5350), fontSize = 13.sp, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CoverSection(
    imageModel: Any?,
    enabled: Boolean,
    onPickImage: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121A26), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .clickable(enabled = enabled) { onPickImage() },
            contentAlignment = Alignment.Center,
        ) {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Cover Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Cover Artwork", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Update song cover", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Change Cover",
                color = EchoPandaColors.AccentBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(enabled = enabled) { onPickImage() },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenreDropdown(
    label: String,
    options: List<MbGenreDto>,
    selectedId: String?,
    enabled: Boolean,
    onSelected: (String) -> Unit,
) {
    CatalogDropdown(
        label = label,
        options = options.map { it.id to it.name },
        selectedId = selectedId,
        enabled = enabled,
        placeholder = "Select genre",
        onSelected = onSelected,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagDropdown(
    label: String,
    options: List<MbTagDto>,
    selectedId: String?,
    enabled: Boolean,
    onSelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.id == selectedId }?.name ?: "None"

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
                colors = dropdownColors(),
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
                options.forEach { tag ->
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
private fun CatalogDropdown(
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
                colors = dropdownColors(),
                shape = RoundedCornerShape(12.dp),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1E2736)),
            ) {
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

@Composable
private fun dropdownColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = EchoPandaColors.AccentBlue,
    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
    focusedContainerColor = Color(0xFF121A26).copy(alpha = 0.5f),
    unfocusedContainerColor = Color(0xFF121A26).copy(alpha = 0.5f),
    disabledTextColor = Color.White.copy(alpha = 0.5f),
    disabledBorderColor = Color.White.copy(alpha = 0.05f),
    disabledContainerColor = Color(0xFF121A26).copy(alpha = 0.3f),
)

@Composable
private fun EditSongField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    maxChars: Int = Int.MAX_VALUE,
    modifier: Modifier = Modifier,
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
                disabledContainerColor = Color(0xFF121A26).copy(alpha = 0.3f),
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 4,
            enabled = enabled,
        )
    }
}
