package com.ourspace.app.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.ui.components.SkeletonLoader

@Composable
fun DiscoverScreen(
    userProfile: UserProfile?,
    viewModel: FeaturesViewModel
) {
    // Assuming viewModel would have a state for available discoverable profiles.
    // For now we mock the loading and display structure.
    
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        // Simulate network fetch
        kotlinx.coroutines.delay(2000)
        isLoading = false
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = "Discover",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            if (isLoading) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(5) {
                        SkeletonLoader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                }
            } else {
                // Liquid, soft UI cards representing people
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(3) { index -> // Placeholder data
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(MaterialTheme.colorScheme.surface) // Translucent
                                .padding(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(32.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Potential Match ${index + 1}", style = MaterialTheme.typography.titleLarge)
                                    Text("Tap to view profile", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
