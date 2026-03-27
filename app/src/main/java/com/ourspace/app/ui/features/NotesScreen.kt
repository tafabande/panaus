package com.ourspace.app.ui.features

import java.text.SimpleDateFormat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ourspace.app.data.model.Note
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.util.DateUtils
import com.ourspace.app.ui.components.EmptyState
import androidx.compose.material.icons.automirrored.filled.Notes

// Colors moved to MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    userProfile: UserProfile?,
    viewModel: FeaturesViewModel,
    onBack: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val partnerMood by viewModel.partnerMood.collectAsState()
    var showNoteDialog by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("Shared Space", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            val isUnlinked = userProfile?.coupleId.isNullOrBlank()
            FloatingActionButton(
                onClick = { 
                    if (!isUnlinked) {
                        showNoteDialog = Note(coupleId = userProfile?.coupleId ?: "", senderId = userProfile?.userId ?: "") 
                    }
                },
                containerColor = if (isUnlinked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                contentColor = if (isUnlinked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Partner Mood Banner
            if (partnerMood != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(partnerMood?.emoji ?: "😊", fontSize = 24.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Your partner is feeling ${partnerMood?.note?.ifBlank { "good" } ?: "good"}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Updated ${DateUtils.formatRelativeTime(partnerMood?.timestamp ?: 0L)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Notes Grid
            if (notes.isEmpty()) {
                val isUnlinked = userProfile?.coupleId.isNullOrBlank()
                EmptyState(
                    icon = Icons.AutoMirrored.Filled.Notes,
                    title = if (isUnlinked) "Shared Space" else "No Shared Notes Yet",
                    description = if (isUnlinked) 
                        "Link with a partner to start sharing notes, links, and memories in your private space."
                    else "Capture your first shared memory, link, or digital sticky note here."
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { showNoteDialog = note },
                            onDelete = { viewModel.deleteNote(userProfile?.coupleId ?: "", note.id) }
                        )
                    }
                }
            }
        }
    }

    if (showNoteDialog != null) {
        NoteEditDialog(
            note = showNoteDialog!!,
            onDismiss = { showNoteDialog = null },
            onSave = { updatedNote ->
                userProfile?.coupleId?.let { viewModel.saveNote(it, updatedNote) }
                showNoteDialog = null
            }
        )
    }
}

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(android.graphics.Color.parseColor(note.color)).copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.title.ifBlank { "Untitled" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.content,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(java.util.Date(note.timestamp)),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun NoteEditDialog(
    note: Note,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    var selectedColor by remember { mutableStateOf(note.color) }

    val colors = listOf("#FFFFFF", "#F8BBD0", "#E1BEE7", "#C5CAE9", "#B3E5FC", "#C8E6C9", "#FFF9C4", "#FFE0B2")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (note.id.isEmpty()) "New Note" else "Edit Note") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pick a color", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { colorStr ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(colorStr)))
                                .clickable { selectedColor = colorStr }
                                .let { 
                                    if (selectedColor == colorStr) {
                                        it.background(Color.Black.copy(alpha = 0.1f), CircleShape)
                                    } else it
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(note.copy(title = title, content = content, color = selectedColor, timestamp = System.currentTimeMillis()))
            }) {
                Text("Save", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}


