package com.example.echo_panda_mobile.presentation.views.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.echo_panda_mobile.presentation.viewsmodel.AdminCategoryDetailViewModel

private val BgDark = Color(0xFF05070D)
private val CardBg = Color(0xFF161C24)
private val AccentPurple = Color(0xFFFF00FF)
private val TextMuted = Color.White.copy(alpha = 0.5f)
private val ActiveGreen = Color(0xFF00C853)
private val InactiveRed = Color(0xFFFF5252)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoryDetailScreen(
    categoryId: String,
    onBack: () -> Unit,
    viewModel: AdminCategoryDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadGenreImage(it) }
    }

    // State for editing mode
    var isEditing by remember { mutableStateOf(false) }
    
    // Values that can be edited
    var editedName by remember { mutableStateOf("") }

    LaunchedEffect(categoryId) {
        viewModel.loadCategory(categoryId)
    }

    LaunchedEffect(uiState.genre) {
        uiState.genre?.let {
            editedName = it.name
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Category Details", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AccentPurple)
            }

            uiState.errorMessage?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(8.dp))
            }

            uiState.successMessage?.let {
                Text(it, color = AccentPurple, modifier = Modifier.padding(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AccentPurple.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    val imageUrl = uiState.displayImageUrl
                    when {
                        !imageUrl.isNullOrBlank() -> {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Category image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        uiState.isUploadingImage -> {
                            CircularProgressIndicator(color = AccentPurple, modifier = Modifier.size(28.dp))
                        }
                        else -> {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    enabled = uiState.genre != null && !uiState.isUploadingImage && !uiState.isLoading,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentPurple)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Upload category image",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                TextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Category Name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(uiState.genre?.name ?: "Loading...", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Order ID: ${uiState.genre?.id ?: categoryId}", color = TextMuted, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(40.dp))

            // Info items
            DetailItem("Slug", uiState.genre?.slug ?: "-", Icons.Default.Link)
            DetailItem("Songs Count", uiState.genre?.songsCount?.toString() ?: "0", Icons.Default.MusicNote)

            DetailToggleRow(
                label = "Active",
                description = "Visible on the home page when enabled",
                checked = uiState.genre?.isActive ?: false,
                enabled = uiState.genre != null && !uiState.isLoading,
                onCheckedChange = { viewModel.setCategoryActive(it) }
            )
            DetailToggleRow(
                label = "Show as row",
                description = "Display albums in a horizontal row on the home page",
                checked = uiState.genre?.showAsRow ?: false,
                enabled = uiState.genre != null && !uiState.isLoading,
                onCheckedChange = { viewModel.setCategoryShowAsRow(it) }
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { 
                        if (isEditing) {
                            uiState.genre?.let { viewModel.updateCategory(it.id, editedName) }
                            isEditing = false
                        } else {
                            isEditing = true
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEditing) Color(0xFF00C853) else Color(0xFF1E88E5)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isLoading
                ) {
                    Icon(
                        if (isEditing) Icons.Default.Save else Icons.Default.Edit, 
                        contentDescription = null, 
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEditing) "Save Changes" else "Edit Category", fontWeight = FontWeight.Bold)
                }
                
                if (isEditing) {
                    Button(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { uiState.genre?.let { viewModel.deleteCategory(it.id) } },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252).copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delete", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = CardBg,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(description, color = TextMuted, fontSize = 12.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ActiveGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = InactiveRed,
                    uncheckedBorderColor = Color.Transparent
                ),
                modifier = Modifier.scale(0.85f)
            )
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color = Color.White) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = CardBg,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, color = TextMuted, fontSize = 12.sp)
                Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
