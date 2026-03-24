package com.ourspace.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ourspace.app.ui.features.FeaturesViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import com.ourspace.app.R

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onNavigateToPairing: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToTodos: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToMoods: () -> Unit,
    onNavigateToAsks: () -> Unit,
    onNavigateToLocation: () -> Unit,
    userViewModel: UserViewModel,
    featuresViewModel: FeaturesViewModel
) {
    val userProfile by userViewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF1F2))
                        .padding(8.dp),
                    tint = Color(0xFFF43F5E)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Our Space",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Hi, ${userProfile?.name ?: "there"}",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
            Button(
                onClick = {
                    userViewModel.logout()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF1F2)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("Sign Out", color = Color(0xFFE11D48), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (userProfile?.coupleId == null) {
            Card(
                onClick = onNavigateToPairing,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "You're flying solo! ✈️",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE11D48),
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Pair with your partner to unlock all shared features.",
                            color = Color(0xFFE11D48),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Button(
                        onClick = onNavigateToPairing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Pair Now")
                    }
                }
            }
        }

        // Widgets
        WidgetCard(title = "CURRENT MOOD", subtitle = "View and log moods in the tracker.", onClick = onNavigateToMoods)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                WidgetCard(title = "LOCATION", subtitle = "Check-in", onClick = onNavigateToLocation)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f)) {
                WidgetCard(title = "REQUESTS", subtitle = "Pokes & Asks", onClick = onNavigateToAsks)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        WidgetCard(title = "NEXT EVENT", subtitle = "Shared Calendar", onClick = onNavigateToCalendar)
        Spacer(modifier = Modifier.height(16.dp))
        WidgetCard(title = "RECENT NOTE", subtitle = "Check your pocket notes!", onClick = onNavigateToNotes)
        Spacer(modifier = Modifier.height(16.dp))
        WidgetCard(title = "SHARED TO-DOS", subtitle = "Manage tasks together", onClick = onNavigateToTodos)
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                WidgetCard(title = "MEMORIES", subtitle = "Photos", onClick = { /* TODO Phase 6 */ })
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f)) {
                WidgetCard(title = "ANALYTICS", subtitle = "Premium", onClick = { /* TODO Phase 6 */ })
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title, 
                fontSize = 11.sp, 
                fontWeight = FontWeight.SemiBold, 
                color = Color(0xFF64748B),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF1F2), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(text = subtitle, fontSize = 14.sp, color = Color(0xFF1E293B))
            }
        }
    }
}
