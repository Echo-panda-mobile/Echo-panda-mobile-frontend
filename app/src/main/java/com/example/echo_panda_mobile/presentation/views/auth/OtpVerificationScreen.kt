package com.example.echo_panda_mobile.presentation.views.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen 08: Enter OTP
 * Matches the UI for entering the verification code.
 */
@Composable
fun OtpVerificationScreen(
    email: String = "john@example.com",
    onVerify: (String) -> Unit,
    onResend: () -> Unit
) {
    // Standard Echo-Panda Color Palette
    val IntroBg = Color(0xFF040B11)
    val CardBg = Color(0xFF0D1219)
    val AccentClipped = Color(0xFF00E5FF)
    val TextMuted = Color(0xFF6F7F8C)
    val BoxBg = Color(0xFF091016) // Darker background for input boxes

    // State for the 4-digit OTP
    var otpCode by remember { mutableStateOf("") }

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
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter OTP",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "A verification codes has been\nsent to $email",
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // OTP Input Row (4 Boxes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    repeat(4) { index ->
                        val char = if (otpCode.length > index) otpCode[index].toString() else ""
                        val isFocused = otpCode.length == index

                        Box(
                            modifier = Modifier
                                .size(width = 55.dp, height = 60.dp)
                                .background(BoxBg, RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isFocused) AccentClipped else Color(0xFF1A2A33),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                color = if (char.isNotEmpty()) Color.White else AccentClipped,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Visual cursor for the focused box
                            if (isFocused) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(24.dp)
                                        .background(AccentClipped)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Verify Button
                Button(
                    onClick = { onVerify(otpCode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentClipped
                    )
                ) {
                    Text(
                        text = "Verify",
                        color = IntroBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Resend Link
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Didn't receive the code? ",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    TextButton(onClick = onResend, contentPadding = PaddingValues(0.dp)) {
                        Text(
                            text = "Resend (30s)",
                            color = AccentClipped,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}