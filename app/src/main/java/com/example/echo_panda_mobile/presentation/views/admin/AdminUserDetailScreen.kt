package com.example.echo_panda_mobile.presentation.views.admin

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echo_panda_mobile.data.remote.BackendUser
import com.example.echo_panda_mobile.presentation.viewsmodel.AdminUserDetailViewModel
import com.example.echo_panda_mobile.presentation.viewsmodel.AdminUserDetailViewModelFactory

private val BgDark = Color(0xFF05070D)
private val CardBg = Color(0xFF161C24)
private val AccentPurple = Color(0xFFFF00FF)
private val TextMuted = Color.White.copy(alpha = 0.5f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(
    userId: String,
    role: String,
    onBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AdminUserDetailViewModel = viewModel(
        factory = AdminUserDetailViewModelFactory(application, userId, role)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Details", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentPurple)
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = Color(0xFFFF6B6B),
                        fontSize = 14.sp
                    )
                }
            }

            uiState.user != null -> {
                val user = uiState.user
                if (user != null) {
                    UserDetailContent(
                        user = user,
                        paddingValues = paddingValues,
                        onBack = onBack
                    )
                }
            }
        }
    }
}

@Composable
private fun UserDetailContent(
    user: BackendUser,
    paddingValues: PaddingValues,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(AccentPurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.firstOrNull()?.toString() ?: "",
                color = AccentPurple,
                fontWeight = FontWeight.Black,
                fontSize = 40.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(user.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(user.email, color = TextMuted, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(32.dp))

        DetailItem("User ID", user.id.toString(), Icons.Default.Fingerprint)
        DetailItem(
            label = "Role",
            value = user.role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            icon = Icons.Default.Stars
        )
        DetailItem("Linked Artist ID", user.artist_id?.toString() ?: "None", Icons.Default.MusicNote)

        user.artist?.let { artist ->
            DetailItem("Linked Artist", artist.name, Icons.Default.Star)
            if (!artist.image_url.isNullOrBlank()) {
                DetailItem("Artist Image", artist.image_url, Icons.Default.Image)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Back", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DetailItem(label: String, value: String, icon: ImageVector, color: Color = Color.White) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = CardBg,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, color = TextMuted, fontSize = 12.sp)
                Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
