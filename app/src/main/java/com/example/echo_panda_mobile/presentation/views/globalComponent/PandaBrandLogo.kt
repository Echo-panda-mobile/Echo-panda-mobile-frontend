package com.example.echo_panda_mobile.presentation.views.globalComponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * EchoPanda Brand Logo - Cute panda with music note
 * Combines a panda symbol with music elements for the Echo Panda app
 */
@Composable
fun PandaBrandLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        // Music note icon representing Echo
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = "Echo Panda Logo",
            modifier = Modifier.size(50.dp),
            tint = Color(0xFF00E5FF)
        )
    }
}
