package com.example.echo_panda_mobile.presentation.views.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.RegisterViewModel

// ─── Shared colour tokens (same as LoginScreen) ───────────────────────────────
private val BgDark      = Color(0xFF0A0A0F)
private val BgCard      = Color(0xFF13131A)
private val AccentGreen = Color(0xFF1DB954)
private val AccentGlow  = Color(0xFF1DB95440)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextMuted   = Color(0xFF8A8A9A)
private val InputBg     = Color(0xFF1E1E2A)
private val InputBorder = Color(0xFF2A2A3A)
private val ErrorRed    = Color(0xFFFF4D4D)

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (route: String) -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.navigateTo) {
        uiState.navigateTo?.let { route ->
            onRegisterSuccess(route)
            viewModel.onNavigationHandled()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Decorative glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-60).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentGlow, Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // ── Header ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AccentGreen, Color(0xFF17A349))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EP",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Create account",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Join EchoPanda today",
                color = TextMuted,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Role Selector ─────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "I am joining as a...",
                    color = TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoleCard(
                        label     = "Listener",
                        icon      = Icons.Default.AccountCircle,
                        selected  = uiState.selectedRole == "user",
                        onClick   = { viewModel.onRoleChange("user") },
                        modifier  = Modifier.weight(1f)
                    )
                    RoleCard(
                        label     = "Artist",
                        icon      = Icons.Default.Face,
                        selected  = uiState.selectedRole == "artist",
                        onClick   = { viewModel.onRoleChange("artist") },
                        modifier  = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Form Card ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name
                AuthTextField(
                    value         = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label         = "Full Name",
                    leadingIcon   = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // Email
                AuthTextField(
                    value         = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    label         = "Email",
                    leadingIcon   = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // Password
                AuthTextField(
                    value         = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label         = "Password",
                    leadingIcon   = Icons.Default.Lock,
                    isPassword    = true,
                    isPasswordVisible    = uiState.isPasswordVisible,
                    onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // Confirm Password
                AuthTextField(
                    value         = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label         = "Confirm Password",
                    leadingIcon   = Icons.Default.ThumbUp,
                    isPassword    = true,
                    isPasswordVisible    = uiState.isConfirmPasswordVisible,
                    onTogglePasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.onRegisterClick()
                        }
                    )
                )

                // Error message
                AnimatedVisibility(visible = uiState.errorMessage != null) {
                    uiState.errorMessage?.let { error ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ErrorRed.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text     = error,
                                color    = ErrorRed,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Register button
                Button(
                    onClick  = viewModel::onRegisterClick,
                    enabled  = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = AccentGreen,
                        disabledContainerColor = AccentGreen.copy(alpha = 0.5f)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color       = Color.White,
                            modifier    = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text       = "Create Account",
                            color      = Color.White,
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Login link ────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(text = "Already have an account? ", color = TextMuted, fontSize = 14.sp)
                Text(
                    text       = "Sign In",
                    color      = AccentGreen,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ─── Role Selection Card ──────────────────────────────────────────────────────
@Composable
private fun RoleCard(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) AccentGreen else InputBorder
    val bgColor     = if (selected) AccentGreen.copy(alpha = 0.10f) else BgCard
    val textColor   = if (selected) AccentGreen else TextMuted
    val iconTint    = if (selected) AccentGreen else TextMuted

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = iconTint,
                modifier           = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text       = label,
                color      = textColor,
                fontSize   = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}