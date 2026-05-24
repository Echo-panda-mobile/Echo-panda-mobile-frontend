package com.example.echo_panda_mobile.presentation.views.admin

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgDark = Color(0xFF05070D)
private val CardBg = Color(0xFF161C24)
private val AccentPurple = Color(0xFFFF00FF)
private val TextMuted = Color.White.copy(alpha = 0.5f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddArtistScreen(onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var artistRole by remember { mutableStateOf("Single") } // Single or Group
    var gender by remember { mutableStateOf("Male") } // Male, Female, They
    var password by remember { mutableStateOf("") }
    
    // Automatically set gender to "They" if role is "Group"
    LaunchedEffect(artistRole) {
        if (artistRole == "Group") {
            gender = "They"
        } else if (gender == "They") {
            gender = "Male"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Artist", color = Color.White, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Form Fields
            Text("Artist Information", color = AccentPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            ArtistInputField(label = "Full Name", value = name, onValueChange = { name = it }, placeholder = "Enter artist or group name")
            ArtistInputField(label = "Email Address", value = email, onValueChange = { email = it }, placeholder = "artist@example.com")
            ArtistInputField(label = "Initial Password", value = password, onValueChange = { password = it }, placeholder = "Set a temporary password", isPassword = true)

            Spacer(modifier = Modifier.height(24.dp))

            // Role Selection
            Text("Artist Type", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RoleChip("Single", artistRole == "Single") { artistRole = "Single" }
                RoleChip("Group", artistRole == "Group") { artistRole = "Group" }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gender Selection (Only if Single)
            if (artistRole == "Single") {
                Text("Gender", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    RoleChip("Male", gender == "Male") { gender = "Male" }
                    RoleChip("Female", gender == "Female") { gender = "Female" }
                }
            } else {
                Text("Gender", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Surface(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = CardBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "They (Default for Groups)",
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { /* Implement logic to save to database/repository */ onBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("Create Artist Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ArtistInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
        )
    }
}

@Composable
fun RoleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .height(44.dp),
        color = if (selected) AccentPurple.copy(alpha = 0.2f) else CardBg,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, AccentPurple) else null
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = label,
                color = if (selected) AccentPurple else Color.White,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
