package com.ourspace.app.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    userProfile: UserProfile,
    viewModel: FeaturesViewModel,
    onBack: () -> Unit
) {
    val events by viewModel.events.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Date Night") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shared Calendar", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF64748B))
                    }
                },
                actions = {
                    IconButton(onClick = { showForm = !showForm }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Event", tint = Color(0xFFF43F5E))
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
                            Text("Add New Event", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))
                            
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Event Title") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = date,
                                    onValueChange = { date = it },
                                    label = { Text("Date (YYYY-MM-DD)") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = time,
                                    onValueChange = { time = it },
                                    label = { Text("Time") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            // Simple string input for category to save time instead of dropdown
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Category (e.g Date Night)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { showForm = false },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF1F2))
                                ) {
                                    Text("Cancel", color = Color(0xFFE11D48))
                                }
                                Button(
                                    onClick = {
                                        if (title.isNotBlank() && date.isNotBlank()) {
                                            viewModel.addEvent(
                                                coupleId = userProfile.coupleId ?: "",
                                                creatorId = userProfile.userId,
                                                title = title,
                                                date = date,
                                                time = time,
                                                category = category
                                            )
                                            title = ""
                                            date = ""
                                            time = ""
                                            showForm = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
                                ) {
                                    Text("Save Event")
                                }
                            }
                        }
                    }
                }
            }

            if (events.isEmpty() && !showForm) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Your calendar is completely empty.", color = Color(0xFF94A3B8))
                    }
                }
            }

            items(events) { event ->
                val (monthStr, dayStr) = DateUtils.formatMonthDay(event.date)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(monthStr, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                            Text(dayStr, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                        }
                        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                            Text(event.title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                            Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (event.time.isNotBlank()) {
                                    Text(event.time, fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(end = 8.dp))
                                }
                                Text(
                                    text = event.category,
                                    fontSize = 11.sp,
                                    color = Color(0xFFF43F5E),
                                    modifier = Modifier.background(Color(0xFFFFF1F2), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

     }
}

// Local formatMonthDay removed
