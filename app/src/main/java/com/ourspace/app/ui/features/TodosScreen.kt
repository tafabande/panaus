package com.ourspace.app.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodosScreen(
    userProfile: UserProfile,
    viewModel: FeaturesViewModel,
    onBack: () -> Unit
) {
    val todos by viewModel.todos.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    
    var title by remember { mutableStateOf("") }
    var assignedTo by remember { mutableStateOf("unassigned") }

    val activeTodos = remember(todos) { todos.filter { !it.isCompleted } }
    val completedTodos = remember(todos) { todos.filter { it.isCompleted } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shared To-Dos", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF64748B))
                    }
                },
                actions = {
                    IconButton(onClick = { showForm = !showForm }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Todo", tint = Color(0xFFF43F5E))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Task") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            // Simple assigned select emulation
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                FilterChip(
                                    selected = assignedTo == "unassigned",
                                    onClick = { assignedTo = "unassigned" },
                                    label = { Text("Anyone") }
                                )
                                FilterChip(
                                    selected = assignedTo == userProfile.userId,
                                    onClick = { assignedTo = userProfile.userId },
                                    label = { Text("Me") }
                                )
                                FilterChip(
                                    selected = assignedTo == userProfile.partnerId,
                                    onClick = { assignedTo = userProfile.partnerId ?: "unassigned" },
                                    label = { Text("Partner") }
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (title.isNotBlank()) {
                                        viewModel.addTodo(
                                            coupleId = userProfile.coupleId ?: "",
                                            creatorId = userProfile.userId,
                                            title = title,
                                            assignedTo = assignedTo
                                        )
                                        title = ""
                                        assignedTo = "unassigned"
                                        showForm = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
                            ) {
                                Text("Add Task")
                            }
                        }
                    }
                }
            }

            if (activeTodos.isEmpty() && completedTodos.isEmpty() && !showForm) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("You're all caught up!", color = Color(0xFF94A3B8))
                    }
                }
            }

            items(activeTodos, key = { it.id }) { todo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.toggleTodo(todo) }) {
                            Icon(Icons.Outlined.Circle, contentDescription = "Complete", tint = Color(0xFFFDA4AF))
                        }
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            Text(todo.title, fontSize = 14.sp, color = Color(0xFF1E293B))
                            if (todo.assignedTo != "unassigned") {
                                Text(
                                    text = if (todo.assignedTo == userProfile.userId) "ME" else "PARTNER",
                                    color = Color(0xFFF43F5E),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.deleteTodo(todo.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFFECDD3))
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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.toggleTodo(todo) }) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Uncomplete", tint = Color(0xFF10B981))
                            }
                            Text(
                                text = todo.title,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8),
                                textDecoration = TextDecoration.LineThrough
                            )
                            IconButton(onClick = { viewModel.deleteTodo(todo.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFFECDD3))
                            }
                        }
                    }
                }
            }
        }
    }
}
