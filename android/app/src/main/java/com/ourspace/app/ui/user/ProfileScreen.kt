package com.ourspace.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
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
private val surfaceColor = Color(0xFFf7f6f3)
private val surfaceContainerLow = Color(0xFFf1f1ee)
private val onSurfaceColor = Color(0xFF2e2f2d)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    userProfile: UserProfile,
    userViewModel: UserViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile & Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                fontFamily = FontFamily.Serif
            )
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = primaryColor)
            }
        }

        // Avatar Section
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(surfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userProfile.name.take(1).uppercase(),
                    fontSize = 48.sp,
                    color = primaryColor,
                    fontFamily = FontFamily.Serif
                )
            }
            // Edit badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = 40.dp, y = (-10).dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
                    .clickable { /* TODO */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Avatar", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Info Fields
        Text("Personal Details", fontSize = 18.sp, color = onSurfaceColor, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userProfile.name,
            onValueChange = {},
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = userProfile.email,
            onValueChange = {},
            label = { Text("Email") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = surfaceContainerLow,
                unfocusedContainerColor = surfaceContainerLow
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Customization
        Text("App Customization", fontSize = 18.sp, color = onSurfaceColor, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Theme Accent", fontSize = 16.sp, color = onSurfaceColor, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF923f5f)))
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFa52a65)))
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF6c5a00)))
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFe28743)))
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                userViewModel.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8E8E5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Sign Out", color = primaryColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
