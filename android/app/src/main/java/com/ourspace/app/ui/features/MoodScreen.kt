package com.ourspace.app.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.util.DateUtils

data class MoodOption(val value: Int, val emoji: String, val label: String)

val MOODS = listOf(
    MoodOption(1, "😢", "Sad"),
    MoodOption(2, "😐", "Okay"),
    MoodOption(3, "🙂", "Good"),
    MoodOption(4, "😁", "Great"),
    MoodOption(5, "😍", "Amazing")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
    userProfile: UserProfile,
    viewModel: FeaturesViewModel,
    onBack: () -> Unit
) {
    val moods by viewModel.moods.collectAsState()
    var selectedMood by remember { mutableStateOf<MoodOption?>(null) }
    var note by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mood Tracker", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF64748B))
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

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("How are you feeling right now?", fontWeight = FontWeight.Medium, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MOODS.forEach { m ->
                                val isSelected = selectedMood?.value == m.value
                                Text(
                                    text = m.emoji,
                                    fontSize = 32.sp,
                                    modifier = Modifier
                                        .scale(if (isSelected) 1.25f else 1f)
                                        .clickable { selectedMood = m }
                                        .padding(4.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = { Text("Add a little note... (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            minLines = 3,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFF43F5E),
                                unfocusedBorderColor = Color(0xFFFFE4E6)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                selectedMood?.let {
                                    viewModel.addMood(
                                        userId = userProfile.userId,
                                        coupleId = userProfile.coupleId ?: "",
                                        moodValue = it.value,
                                        emoji = it.emoji,
                                        note = note
                                    )
                                    selectedMood = null
                                    note = ""
                                }
                            },
                            enabled = selectedMood != null,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
                        ) {
                            Text("Log Mood", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "RECENT HISTORY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            if (moods.isEmpty()) {
                item {
                    Text(
                        text = "No moods logged yet.",
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(moods) { mood ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = mood.emoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (mood.userId == userProfile.userId) "You" else "Partner",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = DateUtils.formatDateTime(mood.createdAt),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                if (mood.note.isNotBlank()) {
                                    Text(
                                        text = "\"${mood.note}\"",
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.padding(top = 4.dp).background(Color(0xFFFFF1F2), RoundedCornerShape(8.dp)).padding(8.dp).fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

     }
}

// Local formatTime removed
