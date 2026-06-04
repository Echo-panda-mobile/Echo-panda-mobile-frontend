package com.example.echo_panda_mobile.presentation.views.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.example.echo_panda_mobile.data.remote.GenreData
import com.example.echo_panda_mobile.data.remote.TagData
import com.example.echo_panda_mobile.presentation.components.AdminBottomBar
import com.example.echo_panda_mobile.presentation.components.AdminTopBar
import com.example.echo_panda_mobile.presentation.viewsmodel.AdminLibraryUiState
import com.example.echo_panda_mobile.presentation.viewsmodel.AdminLibraryViewModel

private val BgDark = Color(0xFF05070D)
private val CardBg = Color(0xFF161C24)
private val AccentPurple = Color(0xFFFF00FF)
private val TextMuted = Color.White.copy(alpha = 0.5f)
private val ActiveGreen = Color(0xFF00C853)
private val InactiveRed = Color(0xFFFF5252)

@Composable
fun AdminLibraryScreen(
    selectedNav: Int,
    onNavSelect: (Int) -> Unit,
    onNavigateToTagDetail: (String) -> Unit = {},
    onNavigateToTagAlbums: (String) -> Unit = {},
    onNavigateToCategoryDetail: (String) -> Unit = {},
    onNavigateToCategoryAlbums: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: AdminLibraryViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Category") }
    val filters = listOf("Category", "Tag")
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AdminTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onProfileClick = onProfileClick
            )
        },
        bottomBar = {
            AdminBottomBar(selectedIndex = selectedNav, onSelect = onNavSelect)
        },
        containerColor = BgDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentPurple)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFFF5252),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }

            uiState.successMessage?.let { message ->
                Text(
                    text = message,
                    color = AccentPurple,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            selectedContainerColor = AccentPurple.copy(alpha = 0.2f),
                            labelColor = TextMuted,
                            selectedLabelColor = AccentPurple
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color.White.copy(alpha = 0.1f),
                            selectedBorderColor = AccentPurple,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.dp,
                            enabled = true,
                            selected = selectedFilter == filter
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedFilter) {
                "Tag" -> CollectionTagsSection(
                    tags = uiState.tags,
                    onNavigateToTagDetail = onNavigateToTagDetail,
                    onNavigateToTagAlbums = onNavigateToTagAlbums,
                    onCreateTag = { viewModel.createTag(it) },
                    onDeleteTag = { viewModel.deleteTag(it) },
                    onUpdateTag = { id, name -> viewModel.updateTag(id, name) },
                    onToggleTagActive = { id, isActive -> viewModel.setTagActive(id, isActive) }
                )
                "Category" -> CategoryLibrarySection(
                    genres = uiState.genres,
                    onNavigateToCategoryDetail = onNavigateToCategoryDetail,
                    onNavigateToCategoryAlbums = onNavigateToCategoryAlbums,
                    onCreateCategory = { viewModel.createGenre(it) },
                    onDeleteCategory = { viewModel.deleteGenre(it) },
                    onUpdateCategory = { id, name -> viewModel.updateGenre(id, name) },
                    onToggleCategoryActive = { id, isActive -> viewModel.setGenreActive(id, isActive) }
                )
            }
        }
    }
}

@Composable
fun CategoryLibrarySection(
    genres: List<GenreData>,
    onNavigateToCategoryDetail: (String) -> Unit,
    onNavigateToCategoryAlbums: (String) -> Unit,
    onCreateCategory: (String) -> Unit,
    onDeleteCategory: (Int) -> Unit,
    onUpdateCategory: (Int, String) -> Unit,
    onToggleCategoryActive: (Int, Boolean) -> Unit
) {
    var listSearchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var categoryToDelete by remember { mutableStateOf<GenreData?>(null) }
    var categoryToEdit by remember { mutableStateOf<GenreData?>(null) }
    var editedCategoryName by remember { mutableStateOf("") }

    val filteredGenres = remember(genres, listSearchQuery) {
        genres.filter {
            it.name.contains(listSearchQuery, ignoreCase = true) || (it.slug?.contains(listSearchQuery, ignoreCase = true) == true)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        append("Library ")
                        withStyle(style = SpanStyle(color = AccentPurple)) {
                            append("Categories")
                        }
                    },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage music categories and their visibility",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BasicTextField(
                value = listSearchQuery,
                onValueChange = { listSearchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                singleLine = true,
                cursorBrush = SolidColor(AccentPurple),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Box(Modifier.weight(1f)) {
                            if (listSearchQuery.isEmpty()) {
                                Text(
                                    "Search categories...",
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )

            IconButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF8E24AA), Color(0xFFFF4081))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category", tint = Color.White)
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = CardBg,
                title = { Text("Create New Category", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = TextMuted,
                            focusedLabelColor = AccentPurple,
                            unfocusedLabelColor = TextMuted
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCreateDialog = false
                            onCreateCategory(newCategoryName)
                            newCategoryName = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text("Create", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }

        if (categoryToEdit != null) {
            AlertDialog(
                onDismissRequest = { categoryToEdit = null },
                containerColor = CardBg,
                title = { Text("Edit Category", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = editedCategoryName,
                        onValueChange = { editedCategoryName = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = TextMuted,
                            focusedLabelColor = AccentPurple,
                            unfocusedLabelColor = TextMuted
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            categoryToEdit?.let { onUpdateCategory(it.id, editedCategoryName) }
                            categoryToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToEdit = null }) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }

        if (categoryToDelete != null) {
            AlertDialog(
                onDismissRequest = { categoryToDelete = null },
                containerColor = CardBg,
                title = { Text("Delete Category", color = Color.White) },
                text = { Text("Are you sure you want to delete '${categoryToDelete?.name}'? This action cannot be undone.", color = Color.White.copy(alpha = 0.7f)) },
                confirmButton = {
                    Button(
                        onClick = {
                            categoryToDelete?.let { onDeleteCategory(it.id) }
                            categoryToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToDelete = null }) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.03f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("NAME", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(3f))
                    Text("STATUS", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                    Text("ACTION", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 500.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(filteredGenres) { genre ->
                        CategoryRowItem(
                            genre = genre,
                            onNavigateToCategoryDetail = onNavigateToCategoryDetail,
                            onNavigateToCategoryAlbums = onNavigateToCategoryAlbums,
                            onDelete = { categoryToDelete = genre },
                            onEdit = {
                                categoryToEdit = genre
                                editedCategoryName = genre.name
                            },
                            onToggleActive = { isActive -> onToggleCategoryActive(genre.id, isActive) }
                        )
                        if (genre != filteredGenres.last()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.03f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryRowItem(
    genre: GenreData,
    onNavigateToCategoryDetail: (String) -> Unit,
    onNavigateToCategoryAlbums: (String) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onNavigateToCategoryAlbums(genre.id.toString()) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = genre.name,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(3f)
        )

        Box(modifier = Modifier.weight(1.5f), contentAlignment = Alignment.Center) {
            Switch(
                checked = genre.isActive,
                onCheckedChange = onToggleActive,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ActiveGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = InactiveRed,
                    uncheckedBorderColor = Color.Transparent
                ),
                modifier = Modifier.scale(0.8f)
            )
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "Action", tint = TextMuted, modifier = Modifier.size(20.dp))
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(CardBg)
            ) {
                DropdownMenuItem(
                    text = { Text("Edit Category", color = Color.White) },
                    onClick = {
                        showMenu = false
                        onEdit()
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("View Detail", color = Color.White) },
                    onClick = {
                        showMenu = false
                        onNavigateToCategoryDetail(genre.id.toString())
                    },
                    leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("Delete Category", color = Color(0xFFFF5252)) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp)) }
                )
            }
        }
    }
}

@Composable
fun CollectionTagsSection(
    tags: List<TagData>,
    onNavigateToTagDetail: (String) -> Unit,
    onNavigateToTagAlbums: (String) -> Unit,
    onCreateTag: (String) -> Unit,
    onDeleteTag: (Int) -> Unit,
    onUpdateTag: (Int, String) -> Unit,
    onToggleTagActive: (Int, Boolean) -> Unit
) {
    var listSearchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var tagToDelete by remember { mutableStateOf<TagData?>(null) }
    var tagToEdit by remember { mutableStateOf<TagData?>(null) }
    var editedTagName by remember { mutableStateOf("") }

    val filteredTags = remember(tags, listSearchQuery) {
        tags.filter {
            it.name.contains(listSearchQuery, ignoreCase = true) || (it.slug?.contains(listSearchQuery, ignoreCase = true) == true)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        append("Collection ")
                        withStyle(style = SpanStyle(color = AccentPurple)) {
                            append("Tags")
                        }
                    },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.Label,
                        contentDescription = null,
                        tint = AccentPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Organize albums into custom sections for the home page",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BasicTextField(
                value = listSearchQuery,
                onValueChange = { listSearchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                singleLine = true,
                cursorBrush = SolidColor(AccentPurple),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Box(Modifier.weight(1f)) {
                            if (listSearchQuery.isEmpty()) {
                                Text(
                                    "Search tags...",
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )

            IconButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF8E24AA), Color(0xFFFF4081))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Tag", tint = Color.White)
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = CardBg,
                title = { Text("Create New Tag", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("Tag Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = TextMuted,
                            focusedLabelColor = AccentPurple,
                            unfocusedLabelColor = TextMuted
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCreateDialog = false
                            onCreateTag(newTagName)
                            newTagName = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text("Create", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }

        if (tagToEdit != null) {
            AlertDialog(
                onDismissRequest = { tagToEdit = null },
                containerColor = CardBg,
                title = { Text("Edit Tag", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = editedTagName,
                        onValueChange = { editedTagName = it },
                        label = { Text("Tag Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = TextMuted,
                            focusedLabelColor = AccentPurple,
                            unfocusedLabelColor = TextMuted
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            tagToEdit?.let { onUpdateTag(it.id, editedTagName) }
                            tagToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { tagToEdit = null }) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }

        if (tagToDelete != null) {
            AlertDialog(
                onDismissRequest = { tagToDelete = null },
                containerColor = CardBg,
                title = { Text("Delete Tag", color = Color.White) },
                text = { Text("Are you sure you want to delete '${tagToDelete?.name}'? This action cannot be undone.", color = Color.White.copy(alpha = 0.7f)) },
                confirmButton = {
                    Button(
                        onClick = {
                            tagToDelete?.let { onDeleteTag(it.id) }
                            tagToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { tagToDelete = null }) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.03f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("NAME", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(3f))
                    Text("STATUS", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                    Text("ACTION", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 500.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(filteredTags) { tag ->
                        CollectionTagRow(
                            tag = tag,
                            onNavigateToTagDetail = onNavigateToTagDetail,
                            onNavigateToTagAlbums = onNavigateToTagAlbums,
                            onDelete = { tagToDelete = tag },
                            onEdit = {
                                tagToEdit = tag
                                editedTagName = tag.name
                            },
                            onToggleActive = { isActive -> onToggleTagActive(tag.id, isActive) }
                        )
                        if (tag != filteredTags.last()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.03f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionTagRow(
    tag: TagData,
    onNavigateToTagDetail: (String) -> Unit,
    onNavigateToTagAlbums: (String) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onNavigateToTagAlbums(tag.id.toString()) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tag.name,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(3f)
        )

        Box(modifier = Modifier.weight(1.5f), contentAlignment = Alignment.Center) {
            Switch(
                checked = tag.isActive,
                onCheckedChange = onToggleActive,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ActiveGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = InactiveRed,
                    uncheckedBorderColor = Color.Transparent
                ),
                modifier = Modifier.scale(0.8f)
            )
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "Action", tint = TextMuted, modifier = Modifier.size(20.dp))
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(CardBg)
            ) {
                DropdownMenuItem(
                    text = { Text("Edit Tag", color = Color.White) },
                    onClick = {
                        showMenu = false
                        onEdit()
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("View Detail", color = Color.White) },
                    onClick = {
                        showMenu = false
                        onNavigateToTagDetail(tag.id.toString())
                    },
                    leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("Delete Tag", color = Color(0xFFFF5252)) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp)) }
                )
            }
        }
    }
}
