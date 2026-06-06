package com.example.echo_panda_mobile.presentation.views.admin

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.echo_panda_mobile.data.model.Album
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
    onBack: () -> Unit,
    onNavigateToAlbumDetail: (String) -> Unit = {}
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
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.loadUser() },
            modifier = Modifier.padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.user == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                }

                uiState.errorMessage != null && uiState.user == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                    val user = uiState.user!!
                    UserDetailContent(
                        user = user,
                        profileImageUrl = uiState.profileImageUrl,
                        albums = uiState.albums,
                        isArtistLoading = uiState.isArtistContentLoading,
                        onBack = onBack,
                        onNavigateToAlbumDetail = onNavigateToAlbumDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun UserDetailContent(
    user: BackendUser,
    profileImageUrl: String?,
    albums: List<Album>,
    isArtistLoading: Boolean,
    onBack: () -> Unit,
    onNavigateToAlbumDetail: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(AccentPurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (!profileImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Artist profile photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = user.name.firstOrNull()?.toString() ?: "",
                    color = AccentPurple,
                    fontWeight = FontWeight.Black,
                    fontSize = 40.sp
                )
            }
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

        user.artist?.let { artist ->
            DetailItem("Linked Artist", artist.name, Icons.Default.Star)
            if (!artist.image_url.isNullOrBlank()) {
                DetailItem("Artist Image", artist.image_url, Icons.Default.Image)
            }
        }

        if (user.role.trim().lowercase() == "artist" || user.artist != null) {
            ArtistContentSection(
                albums = albums,
                isLoading = isArtistLoading,
                onNavigateToAlbumDetail = onNavigateToAlbumDetail
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

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

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ArtistContentSection(
    albums: List<Album>,
    isLoading: Boolean,
    onNavigateToAlbumDetail: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Artist Albums",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPurple, modifier = Modifier.size(24.dp))
            }
        } else if (albums.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardBg,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "No albums found for this artist.",
                    color = TextMuted,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardBg,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    albums.forEach { album ->
                        AlbumDetailItem(album, onNavigateToAlbumDetail)
                        if (album != albums.last()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.White.copy(alpha = 0.05f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumDetailItem(album: Album, onNavigateToAlbumDetail: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToAlbumDetail(album.id) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            if (!album.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = album.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Album, contentDescription = null, tint = TextMuted)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(album.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("ID: ${album.id}", color = TextMuted, fontSize = 11.sp)
        }
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
