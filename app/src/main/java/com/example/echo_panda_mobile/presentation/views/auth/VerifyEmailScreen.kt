package com.example.echo_panda_mobile.presentation.views.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen 07: Verify Email Address
 * Matches the UI for confirming the email before sending the OTP.
 */
@Composable
fun VerifyEmailScreen(
    email: String = "john@example.com",
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    // Colors based on your previous Echo-Panda design
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
        // Main Pop-up Card
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
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Verify Your Email Address",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = email,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "We will send the authentication code to the email address you entered. Do you want to continue?",
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = SolidColor(Color(0xFF1A2A33))
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Next Button
                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentClipped
                        )
                    ) {
                        Text(
                            text = "Next",
                            color = IntroBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}