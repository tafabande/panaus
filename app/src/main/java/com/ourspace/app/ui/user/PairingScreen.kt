package com.ourspace.app.ui.user

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(
    onLogout: () -> Unit,
    onPaired: () -> Unit,
    onSkip: (() -> Unit)? = null,
    viewModel: UserViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val pairingState by viewModel.pairingState.collectAsState()
    var partnerCode by remember { mutableStateOf("") }
    var copied by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(userProfile) {
        if (userProfile != null && userProfile?.coupleId != null) {
            onPaired()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logout Button Top Right (simplified for layout)
                Text(
                    text = "Logout",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { 
                            viewModel.logout()
                            onLogout()
                        }
                )

                Text(
                    text = "Connect",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Text(
                    text = "Pair with your partner to start your shared space.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (pairingState is PairingState.Error) {
                    Text(
                        text = (pairingState as PairingState.Error).message,
                        color = Color(0xFFE11D48),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF1F2), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (pairingState is PairingState.Error) {
                    Text(
                        text = (pairingState as PairingState.Error).message,
                        color = Color(0xFFE11D48),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF1F2), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- NEW: Request Handling UI ---
                when (val state = pairingState) {
                    is PairingState.ReceivingRequest -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), // green-50
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                        ) {
                            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("New Connection Request!", fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                Text("Someone wants to pair with you.", fontSize = 12.sp, color = Color(0xFF166534))
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.acceptRequest(state.fromId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Accept & Connect")
                                }
                            }
                        }
                    }
                    is PairingState.RequestSent -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF9C3), RoundedCornerShape(16.dp)) // yellow-100
                                .padding(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Waiting for partner...", fontWeight = FontWeight.Bold, color = Color(0xFF854D0E))
                                Text("Request sent to code: ${state.toCode}", fontSize = 12.sp, color = Color(0xFF854D0E))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    else -> {}
                }
                // --------------------------------

                // Invite Code Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF1F2), RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        text = "YOUR INVITE CODE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFB7185), // rose-400
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = userProfile?.partnerCode ?: "No Code",
                                color = Color(0xFFE11D48),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp),
                                maxLines = 1,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Invite Code", userProfile?.partnerCode ?: "")
                                clipboard.setPrimaryClip(clip)
                                copied = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (copied) "Copied" else "Copy",
                                color = Color(0xFFF43F5E),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    LaunchedEffect(copied) {
                        if (copied) {
                            delay(2000)
                            copied = false
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Discoverability Toggle
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Discoverable",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Allow partners to pair with your code",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Switch(
                        checked = userProfile?.isDiscoverable ?: false,
                        onCheckedChange = { viewModel.setDiscoverability(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFF43F5E),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFFF1F5F9)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = partnerCode,
                    onValueChange = { partnerCode = it },
                    label = { Text("Partner's Code") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E293B),
                        unfocusedTextColor = Color(0xFF1E293B),
                        focusedBorderColor = Color(0xFFF43F5E),
                        unfocusedBorderColor = Color.DarkGray
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.pairWithPartner(partnerCode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                    enabled = pairingState != PairingState.Loading
                ) {
                    if (pairingState == PairingState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Link Accounts", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                if (onSkip != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onSkip) {
                        Text("Skip for now", color = Color(0xFF64748B), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
