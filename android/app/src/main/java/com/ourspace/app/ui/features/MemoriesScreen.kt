package com.ourspace.app.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import kotlin.random.Random

private val primaryColor = Color(0xFF923f5f)
private val surfaceColor = Color(0xFFf7f6f3)
private val onSurfaceColor = Color(0xFF2e2f2d)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoriesScreen(
    userProfile: UserProfile,
    viewModel: FeaturesViewModel,
    onBack: () -> Unit = {}
) {
    Scaffold(
        containerColor = surfaceColor,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Memories Album", 
                        fontFamily = FontFamily.Serif, 
                        color = primaryColor,
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceColor)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO */ },
                containerColor = primaryColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Upload Memory")
            }
        }
    ) { innerPadding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalItemSpacing = 16.dp,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(10) { index ->
                MemoryItemPlaceholder(index)
            }
        }
    }
}

@Composable
fun MemoryItemPlaceholder(index: Int) {
    val height = 150.dp + Random(index).nextInt(0, 100).dp
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "October ${10 + index}, 2025",
            fontSize = 12.sp,
            color = onSurfaceColor,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium
        )
    }
}
