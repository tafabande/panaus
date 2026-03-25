package com.ourspace.app.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
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
fun NotesScreen(
    userProfile: UserProfile,
    viewModel: FeaturesViewModel,
    onBack: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    var newMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(notes.size) {
        if (notes.isNotEmpty()) {
            listState.animateScrollToItem(notes.size - 1)
        }
    }

    Scaffold(
        containerColor = surfaceColor,
        topBar = {
            TopAppBar(
                title = { Text("Pocket Notes", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = primaryColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = onSurfaceColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceColor)
            )
        },
        bottomBar = {
            Surface(
                color = surfaceColor,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Write a note...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newMessage.isNotBlank()) {
                                viewModel.sendNote(
                                    coupleId = userProfile.coupleId ?: "",
                                    senderId = userProfile.userId,
                                    receiverId = userProfile.partnerId,
                                    content = newMessage
                                )
                                newMessage = ""
                            }
                        },
                        modifier = Modifier
                            .background(primaryColor, RoundedCornerShape(24.dp))
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            if (notes.isEmpty()) {
                item {
                    Text(
                        text = "No notes yet. Leave a little message!",
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                items(notes, key = { it.id }) { note ->
                    val isMe = note.senderId == userProfile.userId
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) primaryColor else Color.White
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 0.dp,
                                bottomEnd = if (isMe) 0.dp else 16.dp
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = note.content,
                                    color = if (isMe) Color.White else onSurfaceColor,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                                Text(
                                    text = DateUtils.formatTime(note.createdAt),
                                    color = if (isMe) Color(0xFFffccd5) else Color.Gray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
