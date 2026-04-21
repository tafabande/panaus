package com.ourspace.app.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ourspace.app.data.model.RelationshipEvent
import com.ourspace.app.data.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    userProfile: com.ourspace.app.data.model.UserProfile?,
    viewModel: FeaturesViewModel,
    onAddEvent: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val timelineItems by viewModel.combinedTimeline.collectAsState()
    
    var showSelectionDialog by remember { mutableStateOf(false) }
    var showCaptionDialog by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var captionText by remember { mutableStateOf("") }

    val photoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingUri = uri
            showCaptionDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Our Journey", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSelectionDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        if (timelineItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No events yet. Start capturing your story!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(timelineItems) { item ->
                    TimelineEntry(item)
                }
            }
        }
    }

    if (showSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showSelectionDialog = false },
            title = { Text("Add to Journey") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(
                        onClick = {
                            showSelectionDialog = false
                            onAddEvent()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text("Add Milestone", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    TextButton(
                        onClick = {
                            showSelectionDialog = false
                            photoPicker.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text("Add Photo", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showCaptionDialog && pendingUri != null) {
        AlertDialog(
            onDismissRequest = { 
                showCaptionDialog = false
                pendingUri = null
                captionText = ""
            },
            title = { Text("Add Photo Caption") },
            text = {
                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    label = { Text("Memory details...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (userProfile != null && pendingUri != null) {
                        viewModel.uploadMemory(
                            userProfile.coupleId ?: "",
                            userProfile.userId,
                            pendingUri!!,
                            captionText.trim()
                        )
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
fun TimelineEntry(item: TimelineItem) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Vertical line and dot logic could be added here for a true timeline "look"
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getItemColor(item).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getItemIcon(item),
                    contentDescription = null,
                    tint = getItemColor(item),
                    modifier = Modifier.size(20.dp)
                )
            }
            // Timeline track (connector)
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = item.displayDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = getItemTitle(item),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (getItemDescription(item).isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getItemDescription(item),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item is TimelineItem.Photo) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = item.memory.imageUrl,
                        contentDescription = "Memory photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else if (item is TimelineItem.Relationship && item.event.imageUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = item.event.imageUrl,
                        contentDescription = "Milestone photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

fun getItemIcon(item: TimelineItem): ImageVector = when (item) {
    is TimelineItem.Relationship -> Icons.Default.Star
    is TimelineItem.Photo -> Icons.Default.PhotoLibrary
    is TimelineItem.Anniversary -> Icons.Default.Favorite
}

fun getItemColor(item: TimelineItem): Color = when (item) {
    is TimelineItem.Relationship -> Color(0xFFFFB74D) // Gold/Orange
    is TimelineItem.Photo -> Color(0xFF64B5F6) // Blue
    is TimelineItem.Anniversary -> Color(0xFFE57373) // Red/Pink
}

fun getItemTitle(item: TimelineItem): String = when (item) {
    is TimelineItem.Relationship -> item.event.title
    is TimelineItem.Photo -> if (item.memory.caption.isNotEmpty()) item.memory.caption else "Shared a moment"
    is TimelineItem.Anniversary -> item.event.title
}

fun getItemDescription(item: TimelineItem): String = when (item) {
    is TimelineItem.Relationship -> item.event.description
    is TimelineItem.Photo -> ""
    is TimelineItem.Anniversary -> "Celebrating our milestone!"
}
