package com.ourspace.app.ui.features

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ourspace.app.data.model.UserProfile
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoriesScreen(
    userProfile: UserProfile?,
    viewModel: FeaturesViewModel,
    onBack: () -> Unit = {}
) {
    // Local list of picked photo URIs (in-session; full cloud upload is a future feature)
    var pickedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Multi-photo picker
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            pickedPhotos = (pickedPhotos + uris).distinct()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Memories Album",
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Upload Memory")
            }
        }
    ) { innerPadding ->
        if (pickedPhotos.isEmpty()) {
            // Show placeholder grid while no photos chosen
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalItemSpacing = 16.dp,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(10) { index ->
                    MemoryItemPlaceholder(index)
                }
            }
        } else {
            // Show real picked photos in full staggered grid
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalItemSpacing = 16.dp,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pickedPhotos) { uri ->
                    val height = 150.dp + Random.nextInt(0, 100).dp
                    AsyncImage(
                        model = uri,
                        contentDescription = "Memory photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryItemPlaceholder(index: Int) {
    val height = 150.dp + Random(index).nextInt(0, 100).dp
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Tap + to add memories",
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "October ${10 + index}, 2025",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium
        )
    }
}
