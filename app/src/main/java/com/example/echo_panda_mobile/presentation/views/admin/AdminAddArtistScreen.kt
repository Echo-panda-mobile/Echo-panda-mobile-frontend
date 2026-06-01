package com.example.echo_panda_mobile.presentation.views.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.AdminAddArtistViewModel

private val BgDark = Color(0xFF05070D)
private val CardBg = Color(0xFF161C24)
private val AccentPurple = Color(0xFFFF00FF)
private val TextMuted = Color.White.copy(alpha = 0.5f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddArtistScreen(
    onBack: () -> Unit,
    viewModel: AdminAddArtistViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.createdSuccessfully) {
        if (uiState.createdSuccessfully) {
            uiState.successMessage?.let { snackbarHostState.showSnackbar(it) }
            viewModel.consumeSuccess()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add New Artist",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !uiState.isLoading) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            Text(
                "Artist information",
                color = AccentPurple,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            ArtistInputField(
                label = "Full name",
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                placeholder = "Enter artist or group name"
            )
            ArtistInputField(
                label = "Email address",
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "artist@example.com"
            )
            ArtistInputField(
                label = "Password",
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "Min. 8 characters (mobile login)",
                isPassword = true
            )
            ArtistInputField(
                label = "Artist slug (URL-friendly name)",
                value = uiState.slug,
                onValueChange = viewModel::onSlugChange,
                placeholder = "auto-generated from name"
            )
            ArtistInputField(
                label = "Bio (optional)",
                value = uiState.bio,
                onValueChange = viewModel::onBioChange,
                placeholder = "Short biography",
                isMultiline = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Verification & Status",
                color = AccentPurple,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Verification Status
            Text(
                "Verification Status",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("pending", "approved", "rejected").forEach { status ->
                    RoleChip(
                        status.replaceFirstChar { it.uppercase() },
                        uiState.verificationStatus == status
                    ) {
                        viewModel.onVerificationStatusChange(status)
                    }
                }
            }

            ArtistInputField(
                label = "Verification Reason (optional)",
                value = uiState.verificationReason,
                onValueChange = viewModel::onVerificationReasonChange,
                placeholder = "Why this artist was approved/rejected",
                isMultiline = true
            )

            // Is Active Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Start artist as active",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(
                    checked = uiState.isActive,
                    onCheckedChange = viewModel::onIsActiveChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentPurple,
                        checkedTrackColor = AccentPurple.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Artist type",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RoleChip("Single", uiState.artistType == "Single") {
                    viewModel.onArtistTypeChange("Single")
                }
                RoleChip("Group", uiState.artistType == "Group") {
                    viewModel.onArtistTypeChange("Group")
                }
            }

            if (uiState.artistType == "Single") {
                Text(
                    "Gender",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RoleChip("Male", uiState.gender == "Male") {
                        viewModel.onGenderChange("Male")
                    }
                    RoleChip("Female", uiState.gender == "Female") {
                        viewModel.onGenderChange("Female")
                    }
                }
            } else {
                Text(
                    "Gender",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = CardBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "They (default for groups)",
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.createArtist() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Create artist account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
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
    isPassword: Boolean = false,
    isMultiline: Boolean = false
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .then(if (isMultiline) Modifier.heightIn(min = 100.dp) else Modifier),
            placeholder = {
                Text(placeholder, color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = !isMultiline,
            maxLines = if (isMultiline) 5 else 1,
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            }
        )
    }
}

@Composable
fun RoleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(44.dp),
        color = if (selected) AccentPurple.copy(alpha = 0.2f) else CardBg,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(1.dp, AccentPurple)
        } else {
            null
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = label,
                color = if (selected) AccentPurple else Color.White,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
