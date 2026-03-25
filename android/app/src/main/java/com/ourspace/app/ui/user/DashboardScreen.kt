package com.ourspace.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ourspace.app.ui.features.FeaturesViewModel

// Aura Amour Design Tokens
private val primaryColor = Color(0xFF923f5f)
private val primaryContainer = Color(0xFFfe97b9)
private val surfaceColor = Color(0xFFf7f6f3)
private val surfaceContainerLow = Color(0xFFf1f1ee)
private val surfaceContainerHighest = Color(0xFFddddd9)
private val onSurfaceColor = Color(0xFF2e2f2d)
private val secondaryColor = Color(0xFFa52a65)
private val tertiaryColor = Color(0xFF6c5a00)

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    userViewModel: UserViewModel,
    featuresViewModel: FeaturesViewModel,
    onNavigateToAnalytics: () -> Unit
) {
    val userProfile by userViewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        // App Top Bar / Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Aura Amour",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    fontFamily = FontFamily.Serif // Simulating Noto Serif
                )
                Text(
                    text = "Welcome back, ${userProfile?.name ?: "darling"}",
                    fontSize = 16.sp,
                    color = onSurfaceColor,
                    fontFamily = FontFamily.SansSerif
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(primaryContainer)
            ) {
                // Profile Avatar placeholder
            }
        }

        // Split Mood/Status View
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // My Status
            Card(
                modifier = Modifier.weight(1f).height(140.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("My Heart", fontSize = 14.sp, color = secondaryColor, fontFamily = FontFamily.Serif)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Peaceful", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = primaryColor)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Update >", fontSize = 12.sp, color = primaryColor.copy(alpha = 0.7f))
                }
            }

            // Their Status
            Card(
                modifier = Modifier.weight(1f).height(140.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Their Heart", fontSize = 14.sp, color = secondaryColor, fontFamily = FontFamily.Serif)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Joyful", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = primaryColor)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Send a hug >", fontSize = 12.sp, color = primaryColor.copy(alpha = 0.7f))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Days at a glance / Mini timeline
        Text("Our Recent Days", fontSize = 20.sp, color = onSurfaceColor, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Activity Row 1
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(primaryColor))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Coffee Date at The Local", fontSize = 16.sp, color = onSurfaceColor, fontWeight = FontWeight.Medium)
                        Text("Yesterday", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Activity Row 2
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(tertiaryColor))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Long Walk in the Park", fontSize = 16.sp, color = onSurfaceColor, fontWeight = FontWeight.Medium)
                        Text("Sunday", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                userViewModel.logout()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = surfaceContainerHighest),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Debug Logout", color = primaryColor)
        }
    }
}
