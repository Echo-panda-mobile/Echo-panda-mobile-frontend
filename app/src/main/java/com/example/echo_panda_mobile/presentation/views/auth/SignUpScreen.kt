package com.example.echo_panda_mobile.presentation.views.auth

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.R
import com.example.echo_panda_mobile.presentation.views.globalComponent.AuthInputField
import com.example.echo_panda_mobile.presentation.views.globalComponent.PandaBrandLogo
import com.example.echo_panda_mobile.presentation.viewsmodel.RegisterViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

// ─── PREMIUM UNIFIED DESIGN SYSTEM CONFIGURATION ──────────────────────────────────────
private val IntroBg = Color(0xFF03070B)         // Velvet Obsidian Deep Black Canvas
private val IntroCard = Color(0xFF0C131A)       // Input fields premium elevation background
private val SocialBtnBg = Color(0xFF161C24).copy(alpha = 0.7f) // Translucent social frame plate
private val AccentCyan = Color(0xFF00E5FF)      // Echo Panda High-Fidelity Neon Cyan
private val TextPrimary = Color(0xFFFFFFFF)     // Clean bold white text headers
private val TextMuted = Color(0xFF7E8B97)       // Subdued corporate metadata grey

@Composable
fun SignUpScreen(
    onBack: () -> Unit,
    onSignUpSuccess: (String) -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val googleSignInOptions = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.applicationContext.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context.applicationContext, googleSignInOptions)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK || result.data == null) {
            viewModel.onGoogleSignInFailed("Google sign-in was cancelled or failed to return data.")
            return@rememberLauncherForActivityResult
        }

        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrBlank()) {
                viewModel.onGoogleSignIn(idToken)
            } else {
                viewModel.onGoogleSignInFailed("Google sign-in failed: No ID token found.")
            }
        } catch (e: ApiException) {
            val msg = when (e.statusCode) {
                7 -> "Network error. Please check your connection."
                10 -> "Configuration error (SHA-1/Package Name). Contact support."
                12500 -> "Sign-in failed. Ensure Google Play Services are up to date."
                12501 -> "Sign-in cancelled."
                else -> "Google sign-in error: ${e.message}"
            }
            viewModel.onGoogleSignInFailed(msg)
        } catch (e: Exception) {
            viewModel.onGoogleSignInFailed("Google sign-in failed: ${e.localizedMessage ?: "Unknown error."}")
        }
    }

    // Handle seamless redirection node operations when signup signals success
    LaunchedEffect(uiState.navigateTo) {
        uiState.navigateTo?.let {
            onSignUpSuccess(it)
            viewModel.onNavigationHandled()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(IntroBg)) {

        // ─── AMBIENT BACKGROUND BIG RADIAL BLOW ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .align(Alignment.TopCenter)
                .blur(100.dp)
                .graphicsLayer { alpha = 0.35f }
                .background(
                    Brush.radialGradient(
                        colors = listOf(AccentCyan, Color.Transparent),
                        radius = 650f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ─── TOP ACTION NAVIGATION ROW ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(IntroCard, CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to previous screen target",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── BRANDING LOGO NODE WITH CORE GLOW METRIC ─────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .blur(18.dp)
                        .graphicsLayer { alpha = 0.8f }
                        .background(
                            Brush.radialGradient(colors = listOf(AccentCyan, Color.Transparent)),
                            shape = RoundedCornerShape(40.dp)
                        )
                )

                PandaBrandLogo(modifier = Modifier.size(86.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── TYPOGRAPHY DISPLAY HEADER ───────────────────────────────────────────
            Text(
                text = "GET STARTED",
                color = TextPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.8.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Create a premium account to sync your\nplaylists globally with Echo Panda",
                color = TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ─── INPUT FIELDS FORM CANVAS AREA ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(IntroCard, RoundedCornerShape(28.dp))
                    .border(1.dp, AccentCyan.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 4.dp)
            ) {
                AuthInputField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder = "Full Name"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(IntroCard, RoundedCornerShape(28.dp))
                    .border(1.dp, AccentCyan.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 4.dp)
            ) {
                AuthInputField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "Email Address"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(IntroCard, RoundedCornerShape(28.dp))
                    .border(1.dp, AccentCyan.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 4.dp)
            ) {
                AuthInputField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "Password",
                    isPassword = true,
                    passwordVisible = uiState.isPasswordVisible,
                    onPasswordToggle = viewModel::onTogglePasswordVisibility
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(IntroCard, RoundedCornerShape(28.dp))
                    .border(1.dp, AccentCyan.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 4.dp)
            ) {
                AuthInputField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    placeholder = "Confirm Password",
                    isPassword = true,
                    passwordVisible = uiState.isConfirmPasswordVisible,
                    onPasswordToggle = viewModel::onToggleConfirmPasswordVisibility
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form Validation Feedback Error Alert Banner
            if (uiState.errorMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    color = Color(0xFF2C0B0B),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = Color(0xFFFFB4B4),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Context Divider Grid Component
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)
                Text(
                    text = "OR SIGN UP WITH",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── 1. GOOGLE SIGN-UP PILL BUTTON ───────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable {
                        if (!uiState.isLoading) {
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                    },
                shape = RoundedCornerShape(28.dp),
                color = SocialBtnBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    GoogleIconVector(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (uiState.isLoading) "Signing in…" else "Sign up with Google",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ─── 2. SUBMIT REGISTRATION EXECUTIVE BUTTON ────────────────────────────────
            Button(
                onClick = { viewModel.onRegisterClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan,
                    contentColor = IntroBg
                ),
                enabled = !uiState.isLoading,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = IntroBg,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "REGISTER",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Redirect Back to Interactive Login Screen Frame Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account? ", color = TextMuted, fontSize = 14.sp)
                Text(
                    text = "Login",
                    color = AccentCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onBack() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// Fixed visibility context scope placement for Google vector canvas drawing node
@Composable
private fun GoogleIconVector(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidth = width * 0.18f
        val radius = width / 2f

        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        val path = Path().apply {
            moveTo(radius, radius)
            lineTo(width, radius)
        }
        drawPath(
            path = path,
            color = Color(0xFF4285F4),
            style = Stroke(width = strokeWidth)
        )
    }
}