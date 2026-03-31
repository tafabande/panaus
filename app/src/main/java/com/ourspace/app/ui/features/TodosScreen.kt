package com.ourspace.app.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.ui.components.EmptyState
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodosScreen(
    userProfile: UserProfile?,
    viewModel: FeaturesViewModel,
    onBack: () -> Unit
) {
    val todos by viewModel.todos.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    
    var title by remember { mutableStateOf("") }
    var assignedTo by remember { mutableStateOf("unassigned") }
    var category by remember { mutableStateOf("General") }

    val categories = listOf("General", "Grocery", "Chore", "Date", "Work")

    val activeTodos = remember(todos) { todos.filter { !it.isCompleted } }
    val completedTodos = remember(todos) { todos.filter { it.isCompleted } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shared To-Dos", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { showForm = !showForm }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Todo", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            if (showForm) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it.trim() },
                                label = { Text("Task") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            // Assigned Select
                            Text("Assign To:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = assignedTo == "unassigned",
                                    onClick = { assignedTo = "unassigned" },
                                    label = { Text("Anyone") }
                                )
                                FilterChip(
                                    selected = assignedTo == userProfile?.userId,
                                    onClick = { assignedTo = userProfile?.userId ?: "unassigned" },
                                    label = { Text("Me") }
                                )
                                FilterChip(
                                    selected = assignedTo == userProfile?.partnerId,
                                    onClick = { assignedTo = userProfile?.partnerId ?: "unassigned" },
                                    label = { Text("Partner") }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Category Select
                            Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.forEach { cat ->
                                    FilterChip(
                                        selected = category == cat,
                                        onClick = { category = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (title.isNotBlank()) {
                                        userProfile?.let { profile ->
                                            viewModel.addTodo(
                                                coupleId = profile.coupleId ?: "",
                                                creatorId = profile.userId,
                                                title = title.trim(),
                                                assignedTo = assignedTo,
                                                category = category
                                            )
                                            title = ""
                                            assignedTo = "unassigned"
                                            category = "General"
                                            showForm = false
                                        } ?: run {
                                            com.ourspace.app.util.GlobalErrorHandler.showMessage("Cannot add task while offline/loading")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Add Task")
                            }
                        }
                    }
                }
            }

            if (activeTodos.isEmpty() && completedTodos.isEmpty() && !showForm) {
                item {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                        title = "No To-Dos Yet",
                        description = "Start by adding a task for you or your partner.",
                        modifier = Modifier.padding(top = 40.dp)
                    )
                }
            }

            items(activeTodos, key = { it.id }) { todo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.toggleTodo(userProfile?.coupleId ?: "", todo) }) {
                            Icon(Icons.Outlined.Circle, contentDescription = "Complete", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        }
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            Text(todo.title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(todo.category, fontSize = 9.sp) },
                                    modifier = Modifier.height(18.dp)
                                )
                                if (todo.assignedTo != "unassigned") {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (todo.assignedTo == userProfile?.userId) "FOR ME" else "FOR PARTNER",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { viewModel.deleteTodo(userProfile?.coupleId ?: "", todo.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            if (completedTodos.isNotEmpty()) {
                item {
                    Text("COMPLETED", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.padding(top = 16.dp))
                }
                items(completedTodos, key = { it.id }) { todo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.toggleTodo(userProfile?.coupleId ?: "", todo) }) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Uncomplete", tint = Color(0xFF10B981)) // Keep green for success
                            }
                            Text(
                                text = todo.title,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.outline,
                                textDecoration = TextDecoration.LineThrough
                            )
                            IconButton(onClick = { viewModel.deleteTodo(userProfile?.coupleId ?: "", todo.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }
    }
}
