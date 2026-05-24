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
import com.example.echo_panda_mobile.presentation.components.AdminBottomBar
import com.example.echo_panda_mobile.presentation.components.AdminTopBar

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
    onBack: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Category") }
    val filters = listOf("Category", "Tag")

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
                "Tag" -> CollectionTagsSection(onNavigateToTagDetail, onNavigateToTagAlbums)
                "Category" -> CategoryLibrarySection(onNavigateToCategoryDetail, onNavigateToCategoryAlbums)
            }
        }
    }
}

@Composable
fun CategoryLibrarySection(onNavigateToCategoryDetail: (String) -> Unit, onNavigateToCategoryAlbums: (String) -> Unit) {
    var listSearchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    
    val mockCategories = listOf(
        AdminCategoryRecord("1", "Pop", true, 1),
        AdminCategoryRecord("2", "Rock", true, 2),
        AdminCategoryRecord("3", "Jazz", false, 3),
        AdminCategoryRecord("4", "Classical", true, 4),
        AdminCategoryRecord("5", "Lo-fi", true, 5)
    )

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

        // Search Bar for Categories
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = listSearchQuery,
                onValueChange = { listSearchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = { Text("Search categories...", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
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

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.03f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column {
                // Table Header
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
                    items(mockCategories) { category ->
                        CategoryRowItem(category, onNavigateToCategoryDetail, onNavigateToCategoryAlbums)
                        if (category != mockCategories.last()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.03f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryRowItem(category: AdminCategoryRecord, onNavigateToCategoryDetail: (String) -> Unit, onNavigateToCategoryAlbums: (String) -> Unit) {
    var isChecked by remember { mutableStateOf(category.isActive) }
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // NAME Column
        Text(
            text = category.name,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(3f)
        )
        
        // STATUS Column
        Box(modifier = Modifier.weight(1.5f), contentAlignment = Alignment.Center) {
            Switch(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
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
        
        // ACTION Column
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = TextMuted, modifier = Modifier.size(20.dp))
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(CardBg)
            ) {
                DropdownMenuItem(
                    text = { Text("View Detail", color = Color.White) },
                    onClick = { 
                        showMenu = false 
                        onNavigateToCategoryDetail(category.id)
                    },
                    leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("Manage Albums", color = Color.White) },
                    onClick = { 
                        showMenu = false 
                        onNavigateToCategoryAlbums(category.id)
                    },
                    leadingIcon = { Icon(Icons.Default.Album, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("Delete Category", color = Color(0xFFFF5252)) },
                    onClick = { showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp)) }
                )
            }
        }
    }
}

@Composable
fun CollectionTagsSection(onNavigateToTagDetail: (String) -> Unit, onNavigateToTagAlbums: (String) -> Unit) {
    var listSearchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    
    val mockTags = listOf(
        CollectionTag("1", "korean song", "Active", "No description", 1, true),
        CollectionTag("2", "KPOP song", "Active", "No description", 2, true)
    )

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

        // Search Bar for Tags
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = listSearchQuery,
                onValueChange = { listSearchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = { Text("Search tags...", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
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

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.03f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column {
                // Table Header
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
                    items(mockTags) { tag ->
                        CollectionTagRow(tag, onNavigateToTagDetail, onNavigateToTagAlbums)
                        if (tag != mockTags.last()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.03f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionTagRow(tag: CollectionTag, onNavigateToTagDetail: (String) -> Unit, onNavigateToTagAlbums: (String) -> Unit) {
    var isChecked by remember { mutableStateOf(tag.isActive) }
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // NAME Column
        Text(
            text = tag.name,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(3f)
        )
        
        // STATUS Column
        Box(modifier = Modifier.weight(1.5f), contentAlignment = Alignment.Center) {
            Switch(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
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
        
        // ACTION Column
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = TextMuted, modifier = Modifier.size(20.dp))
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(CardBg)
            ) {
                DropdownMenuItem(
                    text = { Text("View Detail", color = Color.White) },
                    onClick = { 
                        showMenu = false
                        onNavigateToTagDetail(tag.id)
                    },
                    leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("Manage Albums", color = Color.White) },
                    onClick = { 
                        showMenu = false
                        onNavigateToTagAlbums(tag.id)
                    },
                    leadingIcon = { Icon(Icons.Default.Album, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("Delete Tag", color = Color(0xFFFF5252)) },
                    onClick = { showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp)) }
                )
            }
        }
    }
}

data class CollectionTag(
    val id: String,
    val name: String,
    val status: String,
    val description: String,
    val order: Int,
    val isActive: Boolean
)

data class AdminCategoryRecord(val id: String, val name: String, val isActive: Boolean, val order: Int)
