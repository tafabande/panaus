package com.ourspace.app.ui.features

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.ui.features.FeaturesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AnalyticsScreen(
    userProfile: UserProfile?,
    viewModel: FeaturesViewModel,
    onBack: () -> Unit = {}
) {
    val memories by viewModel.memories.collectAsState()
    val events by viewModel.events.collectAsState()
    
    val scrollState = rememberScrollState()

    // Calculate Days Together
    val daysTogether = userProfile?.anniversary?.let { anniv ->
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val annivDate = sdf.parse(anniv)
            val diff = System.currentTimeMillis() - (annivDate?.time ?: System.currentTimeMillis())
            val days = diff / (1000 * 60 * 60 * 24)
            if (days < 0) "0" else String.format("%,d", days)
        } catch (e: Exception) { "0" }
    } ?: "0"

    // Upcoming Dates Count
    val upcomingDates = events.filter { 
        it.category.contains("Date", ignoreCase = true) || 
        it.title.contains("Date", ignoreCase = true) 
    }.size.toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
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
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Serif
                )
            }
        }

        // Our Journey Banner
        Card(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(), 
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Our Journey", fontSize = 14.sp, color = MaterialTheme.colorScheme.tertiary, fontFamily = FontFamily.Serif, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(daysTogether, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Serif)
                Text("Days Together", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Mood Trends
        Text("Heartbeats & Moods", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                // Placeholder for an actual area chart
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Deep Connection", color = MaterialTheme.colorScheme.secondary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Mood trend over the last 30 days", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Love Metrics Row
        Text("Love Metrics", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(memories.size.toString(), "Shared Memories", Modifier.weight(1f))
            MetricCard(upcomingDates, "Upcoming Dates", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(100.dp)) // Cushion for NavBar
    }
}

@Composable
fun MetricCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
        }
    }
}
