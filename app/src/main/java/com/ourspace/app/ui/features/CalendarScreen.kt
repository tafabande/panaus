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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.util.DateUtils
import com.ourspace.app.ui.components.EmptyState
import com.ourspace.app.ui.components.AuraDatePickerField
import com.ourspace.app.ui.components.AuraTimePickerField
import androidx.compose.material.icons.automirrored.filled.EventNote

// Colors moved to MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    userProfile: UserProfile?,
    viewModel: FeaturesViewModel,
    onBack: () -> Unit
) {
    // onBack is used to navigate away from the screen
    val events by viewModel.events.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    
    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    var selectedTime by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Date Night") }

    // Standardized pickers used below

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("Shared Calendar", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    val isUnlinked = userProfile?.coupleId.isNullOrBlank()
                    IconButton(
                        onClick = { 
                            if (!isUnlinked) {
                                showForm = !showForm 
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.Add, 
                            contentDescription = "Add Event", 
                            tint = if (isUnlinked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
            item { 
                HorizontalCalendarOverview(
                    events = events,
                    selectedDate = selectedDate,
                    onDateSelect = { selectedDate = it }
                )
            }

            if (showForm) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("New Itinerary", fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = FontFamily.Serif, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 16.dp))
                            
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Event Title") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AuraDatePickerField(
                                    value = selectedDate,
                                    onValueChange = { selectedDate = it },
                                    label = "Date",
                                    modifier = Modifier.weight(1f)
                                )
                                AuraTimePickerField(
                                    value = selectedTime,
                                    onValueChange = { selectedTime = it },
                                    label = "Time",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Category") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { showForm = false },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = {
                                        if (title.isNotBlank() && selectedDate.isNotBlank()) {
                                            userProfile?.let { profile ->
                                                viewModel.addEvent(
                                                    coupleId = profile.coupleId ?: "",
                                                    creatorId = profile.userId,
                                                    title = title,
                                                    date = selectedDate,
                                                    time = selectedTime,
                                                    category = category
                                                )
                                                title = ""
                                                selectedTime = ""
                                                showForm = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Save Event")
                                }
                            }
                        }
                    }
                }
            }

            val filteredEvents = events.filter { it.date == selectedDate }
            val isUnlinked = userProfile?.coupleId.isNullOrBlank()
            
            if (filteredEvents.isEmpty() && !showForm) {
                item {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.EventNote,
                        title = if (isUnlinked) "Shared Calendar" else "Quiet Day",
                        description = if (isUnlinked)
                            "Link with a partner to start planning your future together."
                        else "No plans for this date yet. Why not schedule a surprise?"
                    )
                }
            } else {
                items(filteredEvents, key = { it.id }) { event ->
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
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(monthStr, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                Text(dayStr, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Serif)
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 20.dp)) {
                                Text(event.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (event.time.isNotBlank()) {
                                        Text(event.time, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 12.dp))
                                    }
                                    Text(
                                        text = event.category,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
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
}

@Composable
fun HorizontalCalendarOverview(
    events: List<com.ourspace.app.data.model.CalendarEvent>,
    selectedDate: String,
    onDateSelect: (String) -> Unit
) {
    val days = (0..14).map { offset ->
        val c = java.util.Calendar.getInstance()
        c.add(java.util.Calendar.DAY_OF_YEAR, offset)
        c.time
    }

    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(days) { date ->
            val isoDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(date)
            val dayOfMonth = java.text.SimpleDateFormat("d", java.util.Locale.US).format(date)
            val dayOfWeek = java.text.SimpleDateFormat("EEE", java.util.Locale.US).format(date)
            val isSelected = isoDate == selectedDate
            val hasEvents = events.any { it.date == isoDate }

            Column(
                modifier = Modifier
                    .width(55.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White)
                    .clickable { onDateSelect(isoDate) }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(dayOfWeek, fontSize = 10.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(dayOfMonth, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                
                if (hasEvents) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.size(4.dp).background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape))
                }
            }
        }
    }
}
