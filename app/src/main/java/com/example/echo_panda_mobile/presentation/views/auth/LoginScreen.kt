package com.example.echo_panda_mobile.presentation.views.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.R
import com.example.echo_panda_mobile.presentation.views.globalComponent.AuthInputField
import com.example.echo_panda_mobile.presentation.views.globalComponent.PandaBrandLogo
import com.example.echo_panda_mobile.presentation.viewsmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

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
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrBlank()) {
                    viewModel.onGoogleSignIn(idToken)
                }
            } catch (e: ApiException) {
                // Silent catch block for API cancellation states
            }
        }
    }

    LaunchedEffect(uiState.navigateTo) {
        uiState.navigateTo?.let {
            onAuthenticateSuccess(it)
            viewModel.onNavigationHandled()
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
            // Calculated dynamic spacer for ideal top positioning
            Spacer(modifier = Modifier.height(48.dp))

            // ─── HIGH QUALITY CORE LOGO CONTAINER WITH COMPOSITE BLURRY GLOW ──────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(130.dp)
            ) {
                // Dynamic underlying glow core layer matching the exact branding light
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

                // EchoPanda Brand Logo
                PandaBrandLogo(modifier = Modifier.size(96.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── HEADINGS TYPOGRAPHY STACK ───────────────────────────────────────────
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

            // ─── INPUT FIELDS SECTION ────────────────────────────────────────────────
            // Email Input Box Container with custom thin cyan alpha border
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
                        viewModel.onEmailChange(it)
                        hasAttemptedSubmit = false
                    },
                    placeholder = "Email"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input Box Container with custom thin cyan alpha border
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
                        viewModel.onPasswordChange(it)
                        hasAttemptedSubmit = false
                    },
                    placeholder = "Password",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordToggle = { passwordVisible = !passwordVisible }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Forgot Password Link Wrapper aligned perfectly to the right border node
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

            // Pushes operational buttons down cleanly to ensure balanced vertical weight
            Spacer(modifier = Modifier.weight(1f))

            // Operational Error Layout Alert Message Banner Node
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

            // Mid-screen Section Context Divider Component Line
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

            // ─── 1. GOOGLE SINGLE SIGN-IN PILL BUTTON (Placed on top per design) ───
            SocialButton(
                text = if (uiState.isLoading) "Signing in…" else "Sign in with Google",
                bgColor = SocialBtnBg,
                textColor = TextPrimary,
                onClick = {
                    if (!uiState.isLoading) {
                        launcher.launch(googleSignInClient.signInIntent)
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ─── 2. MAIN LOGIN EXECUTIVE BUTTON (Placed below Google per design) ───
            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    viewModel.onLoginClick()
                },
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
                        text = "LOGIN",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Registration Terminal Footer Link Area
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