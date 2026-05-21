package com.example.echo_panda_mobile.presentation.views.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen 09: Account Created Successfully
 * Final confirmation screen with success icon.
 */
@Composable
fun SuccessAccountScreen(
    onGoHome: () -> Unit
) {
    // Standard Echo-Panda Color Palette
    val IntroBg = Color(0xFF040B11)
    val CardBg = Color(0xFF0D1219)
    val AccentClipped = Color(0xFF00E5FF)
    val TextMuted = Color(0xFF6F7F8C)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IntroBg),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(
                    width = 1.dp,
                    brush = SolidColor(Color(0xFF1A2A33)),
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 40.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- Success Icon with Glow Effect ---
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp)
                ) {
                    // Outer Blur Glow
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .blur(20.dp)
                            .background(AccentClipped.copy(alpha = 0.4f), CircleShape)
                    )

                    // Main Circle
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = AccentClipped
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = IntroBg,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Account Created\nSuccessfully",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your account created successfully.\nListen your favourite music",
                    color = TextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Go to Home Button
                Button(
                    onClick = onGoHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentClipped
                    )
                ) {
                    Text(
                        text = "Go to Home",
                        color = IntroBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}