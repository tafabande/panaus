package com.ourspace.app.ui.features

import android.net.Uri
import android.util.Log
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
import androidx.compose.material.icons.filled.ArrowBack
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
import com.ourspace.app.ui.components.EmptyState
import androidx.compose.material.icons.filled.PhotoLibrary
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoriesScreen(
    userProfile: UserProfile?,
    viewModel: FeaturesViewModel,
    onBack: () -> Unit
) {
    val memories by viewModel.memories.collectAsState()
    val optimisticMemories by viewModel.optimisticMemories.collectAsState()
    val allMemories = (optimisticMemories + memories)
    
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var showCaptionDialog by remember { mutableStateOf(false) }
    var captionText by remember { mutableStateOf("") }

    // Multi-photo picker replaced with single for captioning flow
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingUri = uri
            showCaptionDialog = true
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d("MemoriesScreen", "Launching photo picker")
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
        if (allMemories.isEmpty()) {
            EmptyState(
                icon = Icons.Default.PhotoLibrary,
                title = "No Memories Yet",
                description = "Tap the + button to start building your shared album.",
                modifier = Modifier.padding(top = 40.dp)
            )
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
                items(allMemories, key = { it.id }) { memory ->
                    val height = 150.dp + Random(memory.id.hashCode()).nextInt(0, 100).dp
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))) {
                        AsyncImage(
                            model = memory.imageUrl,
                            contentDescription = "Memory photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height),
                            contentScale = ContentScale.Crop
                        )

                        // Caption Overlay
                        if (memory.caption.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = memory.caption,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (memory.status == "SENDING") {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    Text("Sending...", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        } else if (memory.status == "FAILED") {
                             Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Failed", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCaptionDialog && pendingUri != null) {
        AlertDialog(
            onDismissRequest = { 
                showCaptionDialog = false
                pendingUri = null
                captionText = ""
            },
            title = { Text("Add Caption") },
            text = {
                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    label = { Text("Say something about this memory...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (userProfile != null && pendingUri != null) {
                        viewModel.uploadMemory(userProfile.coupleId ?: "", userProfile.userId, pendingUri!!, captionText)
                    }
                    showCaptionDialog = false
                    pendingUri = null
                    captionText = ""
                }) {
                    Text("Upload", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCaptionDialog = false
                    pendingUri = null
                    captionText = ""
                }) {
                    Text("Cancel")
                }
            }
        )
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
