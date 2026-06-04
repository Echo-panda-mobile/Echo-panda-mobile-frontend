package com.example.echo_panda_mobile.presentation.views.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.data.repository.FirebaseAuthManager
import com.example.echo_panda_mobile.presentation.views.globalComponent.AuthInputField
import com.example.echo_panda_mobile.presentation.views.globalComponent.PandaBrandLogo
import com.example.echo_panda_mobile.presentation.viewsmodel.LoginViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.LoginViewModelFactory

// ─── HARDCODED DESIGN SYSTEM COLORS ──────────────────────────────────────────────────
private val IntroBg = Color(0xFF03070B)
private val SocialBtnBg = Color(0xFF161C24).copy(alpha = 0.7f) // Refined translucent card color
private val AccentCyan = Color(0xFF00E5FF)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF7E8B97)
private val InputFieldBg = Color(0xFF0C131A)

@Composable
fun LoginScreen(
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit,
    onAuthenticateSuccess: (String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(LocalContext.current))
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val authManager = remember { FirebaseAuthManager(context) }
    var passwordVisible by remember { mutableStateOf(false) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val isAnyLoading = uiState.isLoading

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    loginViewModel.onGoogleSignIn(idToken)
                } else {
                    loginViewModel.onGoogleSignInFailed("Google ID Token not found.")
                }
            } catch (e: Exception) {
                loginViewModel.onGoogleSignInFailed("Google sign-in failed: ${e.localizedMessage}")
            }
        } else {
            loginViewModel.onGoogleSignInFailed("Google sign-in was cancelled or failed.")
        }
    }

    LaunchedEffect(uiState.navigateTo) {
        uiState.navigateTo?.let {
            onAuthenticateSuccess(it)
            loginViewModel.onNavigationHandled()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IntroBg)
    ) {
        // ─── AMBIENT NEON SPOTLIGHT HEADER BACKGROUND GLOW ────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp)
                .align(Alignment.TopCenter)
                .blur(100.dp)
                .graphicsLayer { alpha = 0.4f }
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(130.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .blur(22.dp)
                        .graphicsLayer { alpha = 0.85f }
                        .background(
                            Brush.radialGradient(colors = listOf(AccentCyan, Color.Transparent)),
                            shape = RoundedCornerShape(45.dp)
                        )
                )

                PandaBrandLogo(modifier = Modifier.size(96.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "WELCOME BACK",
                color = TextPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.8.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Log in with your details or continue\nwith your social account",
                color = TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(38.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(InputFieldBg, RoundedCornerShape(28.dp))
                    .border(1.dp, AccentCyan.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 4.dp)
            ) {
                AuthInputField(
                    value = uiState.email,
                    onValueChange = {
                        loginViewModel.onEmailChange(it)
                        hasAttemptedSubmit = false
                    },
                    placeholder = "Email"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(InputFieldBg, RoundedCornerShape(28.dp))
                    .border(1.dp, AccentCyan.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 4.dp)
            ) {
                AuthInputField(
                    value = uiState.password,
                    onValueChange = {
                        loginViewModel.onPasswordChange(it)
                        hasAttemptedSubmit = false
                    },
                    placeholder = "Password",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordToggle = { passwordVisible = !passwordVisible }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    color = AccentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToForgotPassword() }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            val errorMessage = uiState.errorMessage
            if (hasAttemptedSubmit && !errorMessage.isNullOrBlank()) {
                Surface(
                    color = Color(0xFF2C0B0B),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFFB4B4),
                        modifier = Modifier.padding(14.dp),
                        fontSize = 13.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)
                Text(
                    text = "OR CONTINUE WITH",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            SocialButton(
                text = if (uiState.isLoading) "Signing in…" else "Sign in with Google",
                bgColor = SocialBtnBg,
                textColor = TextPrimary,
                onClick = {
                    if (!isAnyLoading) {
                        hasAttemptedSubmit = true
                        launcher.launch(authManager.getGoogleSignInClient().signInIntent)
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    loginViewModel.onLoginClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan,
                    contentColor = IntroBg
                ),
                enabled = !isAnyLoading,
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
                        text = "LOGIN",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Didn't have an account? ", color = TextMuted, fontSize = 14.sp)
                Text(
                    text = "Register",
                    color = AccentCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onNavigateToSignUp() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SocialButton(
    text: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            GoogleBrandIcon(modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun GoogleBrandIcon(modifier: Modifier = Modifier) {
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
