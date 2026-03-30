package com.ourspace.app.ui.features

import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ourspace.app.data.model.RelationshipEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRelationshipScreen(
    userProfile: com.ourspace.app.data.model.UserProfile?,
    viewModel: FeaturesViewModel,
    onNavigateBack: () -> Unit
) {
    val coupleId = userProfile?.coupleId
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    if (coupleId.isNullOrBlank()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Add Milestone") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Link with a partner to add milestones to your journey.")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Milestone", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotEmpty() && date.isNotEmpty()) {
                                val event = RelationshipEvent(
                                    coupleId = coupleId,
                                    title = title,
                                    description = description,
                                    date = date,
                                    timestamp = System.currentTimeMillis()
                                )
                                viewModel.saveRelationshipEvent(coupleId, event, selectedImageUri)
                                onNavigateBack()
                            }
                        },
                        enabled = title.isNotEmpty() && date.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Capture a special moment in your timeline.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Name") },
                placeholder = { Text("e.g., First Date, We moved in together") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            com.ourspace.app.ui.components.AuraDatePickerField(
                value = date,
                onValueChange = { date = it },
                label = "Date"
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("Add some details or thoughts about this moment...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Image Picker Section
            Text("Add a Photo (Optional)", style = MaterialTheme.typography.titleSmall)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { 
                        imagePicker.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("Pick a Photo", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isNotEmpty() && date.isNotEmpty()) {
                        val event = RelationshipEvent(
                            coupleId = coupleId,
                            title = title,
                            description = description,
                            date = date,
                            timestamp = System.currentTimeMillis()
                        )
                        viewModel.saveRelationshipEvent(coupleId, event, selectedImageUri)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotEmpty() && date.isNotEmpty(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Save to Timeline")
            }
        }
    }
}
