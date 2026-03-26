package com.ourspace.app.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.util.DateUtils
import com.ourspace.app.ui.components.EmptyState

val REQUEST_TYPES = listOf(
    "Call me",
    "Send photo",
    "Remind me",
    "Help me",
    "Bring snacks",
    "Custom"
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AsksScreen(
    userProfile: UserProfile?,
    viewModel: FeaturesViewModel,
    onBack: () -> Unit
) {
    val asks by viewModel.asks.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    
    var requestType by remember { mutableStateOf("Call me") }
    var requestText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Requests & Asks", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    TextButton(onClick = { showForm = !showForm }) {
                        Text(if (showForm) "Cancel" else "New Ask", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
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
                            Text("TYPE OF REQUEST", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                REQUEST_TYPES.forEach { type ->
                                    val isSelected = requestType == type
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { requestType = type }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = type,
                                            fontSize = 14.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (requestType == "Custom") {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("CUSTOM MESSAGE", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = requestText,
                                    onValueChange = { requestText = it },
                                    placeholder = { Text("e.g. Please pick up the dry cleaning") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            val isCustomEmpty = requestType == "Custom" && requestText.isBlank()
                            Button(
                                onClick = {
                                    val finalSendText = if (requestType == "Custom") requestText.trim() else requestType
                                    userProfile?.let { profile ->
                                        viewModel.addAsk(
                                            coupleId = profile.coupleId ?: "",
                                            fromUserId = profile.userId,
                                            toUserId = profile.partnerId ?: "",
                                            text = finalSendText,
                                            type = requestType
                                        )
                                        requestText = ""
                                        requestType = "Call me"
                                        showForm = false
                                    } ?: run {
                                        com.ourspace.app.util.GlobalErrorHandler.showMessage("Cannot send request while offline/loading")
                                    }
                                },
                                enabled = !isCustomEmpty,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Send Request", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            if (asks.isEmpty() && !showForm) {
                item {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.Send,
                        title = "No Requests Currently",
                        description = "Start by asking your partner for something sweet or helpful.",
                        modifier = Modifier.padding(top = 40.dp)
                    )
                }
            }

            items(asks, key = { it.id }) { ask ->
                val amIReceiver = ask.toUserId == userProfile?.userId
                val isPending = ask.status == "pending"

                val statusColor = when (ask.status) {
                    "accepted" -> Color(0xFF10B981) // Emerald
                    "declined" -> Color(0xFFE11D48) // Rose
                    "later" -> Color(0xFFD97706) // Amber
                    else -> Color(0xFF64748B)
                }
                
                val statusContainerColor = when (ask.status) {
                    "accepted" -> Color(0xFFECFDF5)
                    "declined" -> Color(0xFFFFF1F2)
                    "later" -> Color(0xFFFFFBEB)
                    else -> Color(0xFFF8FAFC)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (amIReceiver) "PARTNER ASKED YOU" else "YOU ASKED PARTNER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            if (!isPending) {
                                Row(
                                    modifier = Modifier.background(statusContainerColor, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = when(ask.status) {
                                            "accepted" -> Icons.Filled.CheckCircle
                                            "declined" -> Icons.Filled.Cancel
                                            else -> Icons.Default.DateRange
                                        },
                                        contentDescription = ask.status,
                                        tint = statusColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(ask.status.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor, letterSpacing = 1.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "\"${ask.requestText}\"", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = DateUtils.formatDateTime(ask.timestamp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)

                        if (isPending) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (amIReceiver) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ActionBtn(text = "Accept", textColor = Color(0xFF10B981), bgColor = Color(0xFFECFDF5), onClick = { viewModel.updateAskStatus(ask.id, "accepted") }, modifier = Modifier.weight(1f))
                                    ActionBtn(text = "Later", textColor = Color(0xFFD97706), bgColor = Color(0xFFFFFBEB), onClick = { viewModel.updateAskStatus(ask.id, "later") }, modifier = Modifier.weight(1f))
                                    ActionBtn(text = "Decline", textColor = Color(0xFFE11D48), bgColor = Color(0xFFFFF1F2), onClick = { viewModel.updateAskStatus(ask.id, "declined") }, modifier = Modifier.weight(1f))
                                }
                            } else {
                                Text(
                                    text = "Waiting for response...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
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
fun ActionBtn(text: String, textColor: Color, bgColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

// Local formatTime removed
