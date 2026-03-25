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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.util.DateUtils

private val primaryColor = Color(0xFF923f5f)
private val surfaceColor = Color(0xFFf7f6f3)
private val onSurfaceColor = Color(0xFF2e2f2d)

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
        containerColor = surfaceColor,
        topBar = {
            TopAppBar(
                title = { Text("Shared Calendar", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = primaryColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = onSurfaceColor)
                    }
                },
                actions = {
                    IconButton(onClick = { showForm = !showForm }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Event", tint = primaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceColor)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("New Itinerary", fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = FontFamily.Serif, color = primaryColor, modifier = Modifier.padding(bottom = 16.dp))
                            
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Event Title") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = date,
                                    onValueChange = { date = it },
                                    label = { Text("Date (YYYY-MM-DD)") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                                OutlinedTextField(
                                    value = time,
                                    onValueChange = { time = it },
                                    label = { Text("Time") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Category (e.g Date Night)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { showForm = false },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = surfaceColor)
                                ) {
                                    Text("Cancel", color = onSurfaceColor)
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
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
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
                        Text("Your calendar is completely empty.", color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
            }

            items(events, key = { it.id }) { event ->
                val (monthStr, dayStr) = DateUtils.formatMonthDay(event.date)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .background(surfaceColor, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(monthStr, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = primaryColor)
                            Text(dayStr, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor, fontFamily = FontFamily.Serif)
                        }
                        Column(modifier = Modifier.weight(1f).padding(start = 20.dp)) {
                            Text(event.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = onSurfaceColor)
                            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (event.time.isNotBlank()) {
                                    Text(event.time, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(end = 12.dp))
                                }
                                Text(
                                    text = event.category,
                                    fontSize = 11.sp,
                                    color = primaryColor,
                                    modifier = Modifier.background(Color(0xFFffccd5).copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
