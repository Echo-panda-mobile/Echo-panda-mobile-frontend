package com.example.echo_panda_mobile.presentation.views.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.data.remote.BackendUser
import com.example.echo_panda_mobile.presentation.components.AdminBottomBar
import com.example.echo_panda_mobile.presentation.components.AdminTopBar
import com.example.echo_panda_mobile.presentation.viewsmodel.AdminUserManagementViewModel

private val BgDark = Color(0xFF05070D)
private val CardBg = Color(0xFF161C24)
private val AccentPurple = Color(0xFFFF00FF)
private val TextMuted = Color.White.copy(alpha = 0.5f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    selectedNav: Int,
    onNavSelect: (Int) -> Unit,
    onNavigateToDetail: (String, String) -> Unit,
    onNavigateToAddArtist: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: AdminUserManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Users") }
    val filters = listOf("Users", "Artists", "Admins")

    val visibleUsers = when (selectedFilter) {
        "Artists" -> uiState.artistUsers
        "Admins" -> uiState.adminUsers
        else -> uiState.users
    }.filter { user ->
        listOf(user.name, user.email, user.role)
            .joinToString(" ")
            .contains(searchQuery, ignoreCase = true)
    }

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

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentPurple)
                }
            } else {
                uiState.errorMessage?.let { message ->
                    Text(message, color = Color(0xFFFF6B6B), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                when (selectedFilter) {
                    "Users" -> UserListSection(
                        roleName = "User",
                        users = visibleUsers,
                        onNavigateToDetail = onNavigateToDetail,
                        onAddClick = {}
                    )
                    "Artists" -> UserListSection(
                        roleName = "Artist",
                        users = visibleUsers,
                        onNavigateToDetail = onNavigateToDetail,
                        onAddClick = onNavigateToAddArtist
                    )
                    else -> UserListSection(
                        roleName = "Admin",
                        users = visibleUsers,
                        onNavigateToDetail = onNavigateToDetail,
                        onAddClick = {}
                    )
                }
            }
        }
    }
}

@Composable
fun UserListSection(
    roleName: String,
    users: List<BackendUser>,
    onNavigateToDetail: (String, String) -> Unit,
    onAddClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = buildAnnotatedString {
                    append(roleName)
                    append(" ")
                    withStyle(style = SpanStyle(color = AccentPurple)) {
                        append("List")
                    }
                },
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Manage registered $roleName accounts",
                color = TextMuted,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = {
                    Text(
                        "Search is available from the top bar",
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
                enabled = false,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    disabledContainerColor = CardBg,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

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
                    Text(text = "Add", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                HeaderText("EMAIL", Modifier.weight(2f))
                HeaderText("ROLE", Modifier.weight(1f))
                HeaderText("ACTIONS", Modifier.weight(1f), textAlign = TextAlign.End)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.02f),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        ) {
            if (users.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No $roleName records found.", color = TextMuted)
                }
            } else {
                LazyColumn {
                    items(users, key = { it.id }) { user ->
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
}

@Composable
private fun HeaderText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
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
fun UserRow(user: BackendUser, onNavigateToDetail: (String, String) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            Text("ID: ${user.id}", color = TextMuted, fontSize = 9.sp)
        }

        Column(modifier = Modifier.weight(2f)) {
            Text(user.email, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            user.artist?.let { artist ->
                Spacer(modifier = Modifier.height(4.dp))
                Text("Linked artist: ${artist.name}", color = TextMuted, fontSize = 11.sp)
            }
        }

        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = user.role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = TextMuted)
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
                        onNavigateToDetail(user.id.toString(), user.role)
                    }
                )
            }
        }
    }
}