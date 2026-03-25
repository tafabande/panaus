package com.ourspace.app.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ourspace.app.data.model.UserProfile

// Tokens
private val primaryColor = Color(0xFF923f5f)
private val primaryContainer = Color(0xFFfe97b9)
private val surfaceColor = Color(0xFFf7f6f3)
private val surfaceContainerLow = Color(0xFFf1f1ee)
private val onSurfaceColor = Color(0xFF2e2f2d)
private val secondaryColor = Color(0xFFa52a65)
private val tertiaryColor = Color(0xFF6c5a00)

@Composable
fun AnalyticsScreen(
    userProfile: UserProfile,
    viewModel: FeaturesViewModel,
    onBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        // App Top Bar / Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Relationship Analytics",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    fontFamily = FontFamily.Serif
                )
            }
        }

        // Our Journey Banner
        Card(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = tertiaryColor.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(), 
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Our Journey", fontSize = 14.sp, color = tertiaryColor, fontFamily = FontFamily.Serif, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("1,248", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = primaryColor, fontFamily = FontFamily.Serif)
                Text("Days Together", fontSize = 16.sp, color = onSurfaceColor, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Mood Trends
        Text("Heartbeats & Moods", fontSize = 20.sp, color = onSurfaceColor, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                // Placeholder for an actual area chart
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Deep Connection", color = secondaryColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Mood trend over the last 30 days", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Love Metrics Row
        Text("Love Metrics", fontSize = 20.sp, color = onSurfaceColor, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard("342", "Shared Memories", Modifier.weight(1f))
            MetricCard("12", "Upcoming Dates", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MetricCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, color = onSurfaceColor, fontWeight = FontWeight.Medium)
        }
    }
}
