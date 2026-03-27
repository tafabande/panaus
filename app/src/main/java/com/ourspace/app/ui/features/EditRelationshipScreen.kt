package com.ourspace.app.ui.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ourspace.app.data.model.RelationshipEvent
import java.util.*

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
                                viewModel.saveRelationshipEvent(event)
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

            Spacer(modifier = Modifier.weight(1f))

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
                        viewModel.saveRelationshipEvent(event)
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
