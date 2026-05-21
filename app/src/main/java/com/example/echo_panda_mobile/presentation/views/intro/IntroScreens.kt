package com.example.echo_panda_mobile.presentation.views.intro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

// ─── ECHO PANDA PREMIUM DESIGN SYSTEM ──────────────────────────────────────────────────
private object IntroTheme {
    val Background = Color(0xFF07090E)      // Deepest premium midnight dark
    val AccentCyan = Color(0xFF00E5FF)      // Ultra-vibrant neon cyan
    val SurfaceDark = Color(0xFF111622)     // Elevated surface dark color
    val TextPrimary = Color(0xFFFFFFFF)
    val TextMuted = Color(0xFF94A3B8)       // Elegant slate gray for descriptions
}

private data class OnboardingSlideData(
    val titlePrefix: String,
    val highlightedText: String,
    val titleSuffix: String,
    val description: String,
    val imageUrl: String
)

private val onboardingSlides = listOf(
    OnboardingSlideData(
        titlePrefix = "From the ",
        highlightedText = "latest",
        titleSuffix = " hits",
        description = "Play your absolute favorite tracks and emerging artists on Echo Panda instantly.",
        imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=1000&auto=format&fit=crop"
    ),
    OnboardingSlideData(
        titlePrefix = "Your music, your ",
        highlightedText = "vibe",
        titleSuffix = "",
        description = "Create tailored personal playlists and let our smart AI curate the perfect daily beats.",
        imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=1000&auto=format&fit=crop"
    ),
    OnboardingSlideData(
        titlePrefix = "Listen completely ",
        highlightedText = "offline",
        titleSuffix = "",
        description = "Enjoy uncompromised premium audio quality anytime, anywhere without using data.",
        imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=1000&auto=format&fit=crop"
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EchoPandaOnboardingView(
    onFinished: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingSlides.size })
    val scope = rememberCoroutineScope()
    var hasFinished by remember { mutableStateOf(false) }
    val finishOnce = {
        if (!hasFinished) {
            hasFinished = true
            onFinished()
        }
    }
    val isLastPage = pagerState.currentPage == onboardingSlides.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IntroTheme.Background)
    ) {
        // 1. IMMERSIVE FULL-SCREEN BACKGROUND IMAGES WITH GRADIENT BLEED
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = onboardingSlides[page].imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.68f), // Fits imagery into top focus zone elegantly
                    contentScale = ContentScale.Crop
                )

                // High-fidelity cinematic dark gradient overlays
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    IntroTheme.Background
                                ),
                                startY = 0f,
                                endY = 1400f
                            )
                        )
                )
            }
        }

        // 2. SOPHISTICATED TOP ROW (SKIP BUTTON)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            AnimatedVisibility(
                visible = !isLastPage,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                TextButton(
                    onClick = finishOnce,
                    colors = ButtonDefaults.textButtonColors(contentColor = IntroTheme.TextMuted)
                ) {
                    Text(
                        text = "Skip",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // 3. THE FLOATING CONTEMPORARY BOTTOM INFORMATION PANEL
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 28.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(IntroTheme.SurfaceDark.copy(alpha = 0.92f))
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val slide = onboardingSlides[pagerState.currentPage]

            // Premium Large Scaled Headline Block
            Text(
                text = buildAnnotatedString {
                    append(slide.titlePrefix)
                    withStyle(SpanStyle(color = IntroTheme.AccentCyan, fontWeight = FontWeight.Black)) {
                        append(slide.highlightedText)
                    }
                    if (slide.titleSuffix.isNotEmpty()) {
                        append(slide.titleSuffix)
                    }
                },
                color = IntroTheme.TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle Description Text Block (Adds premium context separation)
            Text(
                text = slide.description,
                color = IntroTheme.TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Clean Minimalist Page Indicator Bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingSlides.size) { i ->
                    val isActive = i == pagerState.currentPage
                    val indicatorWidth by animateDpAsState(if (isActive) 24.dp else 8.dp, label = "width")
                    val indicatorColor by animateColorAsState(
                        if (isActive) IntroTheme.AccentCyan else Color.White.copy(alpha = 0.15f),
                        label = "color"
                    )

                    Box(
                        modifier = Modifier
                            .height(5.dp)
                            .width(indicatorWidth)
                            .clip(CircleShape)
                            .background(indicatorColor)
                    )
                }
            }

            // High Accent Solid Action Button (Fixes the navigation issue perfectly)
            Button(
                onClick = {
                    if (isLastPage) {
                        finishOnce() // Fires immediately on final step! No extra clicking required.
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp), // Modern geometric rounded rectangle
                colors = ButtonDefaults.buttonColors(
                    containerColor = IntroTheme.AccentCyan,
                    contentColor = Color(0xFF001E22) // High contrast deep teal for pure readability
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = if (isLastPage) "Get Started" else "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}