package com.example.echo_panda_mobile.presentation.views.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo_panda_mobile.presentation.components.AdminBottomBar
import com.example.echo_panda_mobile.presentation.components.AdminTopBar

private val BgDark = Color(0xFF05070D)
private val CardBg = Color(0xFF161C24)
private val AccentPurple = Color(0xFFFF00FF)
private val TextMuted = Color.White.copy(alpha = 0.5f)

data class AdminUserRecord(
    val id: String,
    val name: String,
    val email: String,
    val joinedDate: String,
    val status: String,
    val role: String // "User", "Artist", "Admin"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    selectedNav: Int,
    onNavSelect: (Int) -> Unit,
    onNavigateToDetail: (String, String) -> Unit,
    onNavigateToAddArtist: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Users") }
    val filters = listOf("Users", "Artists", "Admins")

    val mockUsers = listOf(
        AdminUserRecord("CeSwfxPJ...", "Pory Morokot", "morokotpory@gmail.com", "May 24, 2026", "ACTIVE", "User"),
        AdminUserRecord("qcHqjVPg...", "Unknown User", "xiximocha@gmail.com", "May 24, 2026", "BANNED", "User")
    )

    val mockArtists = listOf(
        AdminUserRecord("art1...", "Artist Panda", "panda@echo.com", "Jan 10, 2026", "ACTIVE", "Artist"),
        AdminUserRecord("art2...", "Neon Vibes", "neon@echo.com", "Feb 15, 2026", "ACTIVE", "Artist")
    )

    val mockAdmins = listOf(
        AdminUserRecord("adm1...", "Super Admin", "admin@echo.com", "Dec 01, 2025", "ACTIVE", "Admin"),
        AdminUserRecord("adm2...", "Staff Member", "staff@echo.com", "Mar 20, 2026", "ACTIVE", "Admin")
    )

    Scaffold(
        topBar = {
            AdminTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onProfileClick = { }
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
                "Users" -> UserListSection("User", mockUsers, onNavigateToDetail, {})
                "Artists" -> UserListSection("Artist", mockArtists, onNavigateToDetail, onNavigateToAddArtist)
                "Admins" -> UserListSection("Admin", mockAdmins, onNavigateToDetail, {})
            }
        }
    }
}

@Composable
fun UserListSection(
    roleName: String, 
    users: List<AdminUserRecord>, 
    onNavigateToDetail: (String, String) -> Unit,
    onAddClick: () -> Unit
) {
    var listSearchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header Text
        Column {
            Text(
                text = buildAnnotatedString {
                    append("$roleName ")
                    withStyle(style = SpanStyle(color = AccentPurple)) {
                        append("List")
                    }
                },
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Manage registered $roleName accounts".lowercase(),
                color = TextMuted,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search Bar and Add Button Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Inline Search Bar
            TextField(
                value = listSearchQuery,
                onValueChange = { listSearchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = { 
                    Text(
                        "Search by name or email...", 
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp
                    ) 
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Add Button
            if (roleName == "Artist") {
                Button(
                    onClick = onAddClick,
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPurple,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Table Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderText("USER", Modifier.weight(3f))
                HeaderText("JOINED", Modifier.weight(1.5f))
                HeaderText("STATUS", Modifier.weight(1f))
                HeaderText("ACTIONS", Modifier.weight(1f), textAlign = TextAlign.End)
            }
        }

        // Table Body
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.02f),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        ) {
            LazyColumn {
                items(users) { user ->
                    UserRow(user, onNavigateToDetail)
                    if (user != users.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color.White.copy(alpha = 0.05f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderText(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        color = TextMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = modifier,
        textAlign = textAlign
    )
}

@Composable
fun UserRow(user: AdminUserRecord, onNavigateToDetail: (String, String) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Info (Avatar + Name/ID/Email)
        Column(modifier = Modifier.weight(3f), verticalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.firstOrNull()?.toString() ?: "",
                    color = AccentPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(user.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(user.email, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
            Text("ID: ${user.id}", color = TextMuted, fontSize = 9.sp)
        }

        // Joined Date
        Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                tint = AccentPurple.copy(alpha = 0.6f),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(user.joinedDate, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        }

        // Status (Icon based)
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val isActive = user.status == "ACTIVE"
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Block,
                contentDescription = user.status,
                tint = if (isActive) Color(0xFF00C853) else Color(0xFFFF5252),
                modifier = Modifier.size(20.dp)
            )
        }

        // Actions
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Actions",
                    tint = TextMuted
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(CardBg)
            ) {
                DropdownMenuItem(
                    text = { Text("View Detail", color = Color.White, fontSize = 14.sp) },
                    onClick = {
                        showMenu = false
                        onNavigateToDetail(user.id, user.role)
                    }
                )

                val actionText = if (user.status == "ACTIVE") "Ban this account?" else "Unban this account?"
                DropdownMenuItem(
                    text = { Text(actionText, color = Color.White, fontSize = 14.sp) },
                    onClick = {
                        showMenu = false
                        showDialog = true
                    }
                )
            }
        }
    }

    if (showDialog) {
        val isActive = user.status == "ACTIVE"
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = CardBg,
            title = { 
                Text(
                    if (isActive) "Confirm Ban" else "Confirm Unban", 
                    color = Color.White 
                ) 
            },
            text = { 
                Text(
                    "Are you sure you want to ${if (isActive) "ban" else "unban"} ${user.name}?", 
                    color = TextMuted 
                ) 
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(if (isActive) "Ban" else "Unban", color = if (isActive) Color(0xFFFF5252) else Color(0xFF00C853))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}
