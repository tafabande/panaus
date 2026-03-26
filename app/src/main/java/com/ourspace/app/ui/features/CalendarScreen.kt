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

private val primaryColor = Color(0xFF923f5f)
private val surfaceColor = Color(0xFFf7f6f3)
private val onSurfaceColor = Color(0xFF2e2f2d)

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

    val context = androidx.compose.ui.platform.LocalContext.current
    val calendar = java.util.Calendar.getInstance()

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, y, m, d -> selectedDate = DateUtils.formatToIsoDate(y, m, d) },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, h, m -> selectedTime = DateUtils.formatToDisplayTime(h, m) },
        calendar.get(java.util.Calendar.HOUR_OF_DAY),
        calendar.get(java.util.Calendar.MINUTE),
        false
    )

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
                            Text("New Itinerary", fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = FontFamily.Serif, color = primaryColor, modifier = Modifier.padding(bottom = 16.dp))
                            
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Event Title") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.DarkGray
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = selectedDate,
                                    onValueChange = { },
                                    label = { Text("Date") },
                                    modifier = Modifier.weight(1f).clickable { datePickerDialog.show() },
                                    enabled = false,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = onSurfaceColor,
                                        disabledBorderColor = Color.LightGray,
                                        disabledLabelColor = Color.Gray
                                    )
                                )
                                OutlinedTextField(
                                    value = selectedTime,
                                    onValueChange = { },
                                    label = { Text("Time") },
                                    modifier = Modifier.weight(1f).clickable { timePickerDialog.show() },
                                    enabled = false,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = onSurfaceColor,
                                        disabledBorderColor = Color.LightGray,
                                        disabledLabelColor = Color.Gray
                                    )
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
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                ) {
                                    Text("Save Event")
                                }
                            }
                        }
                    }
                }
            }

            items(events.filter { it.date == selectedDate }, key = { it.id }) { event ->
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
                    .background(if (isSelected) primaryColor else Color.White)
                    .clickable { onDateSelect(isoDate) }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(dayOfWeek, fontSize = 10.sp, color = if (isSelected) Color.White else Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(dayOfMonth, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else onSurfaceColor)
                
                if (hasEvents) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.size(4.dp).background(if (isSelected) Color.White else primaryColor, androidx.compose.foundation.shape.CircleShape))
                }
            }
        }
    }
}
