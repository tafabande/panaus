package com.ourspace.app.ui.user

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ourspace.app.data.model.UserProfile

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    userProfile: UserProfile?,
    userViewModel: UserViewModel
) {
    val scrollState = rememberScrollState()
    val isSaving by userViewModel.isSavingProfile.collectAsState()
    val pairingState by userViewModel.pairingState.collectAsState()

    // Local state for editable fields
    var editName by remember(userProfile?.name) { mutableStateOf(userProfile?.name ?: "") }
    var birthday by remember(userProfile?.birthday) { mutableStateOf(userProfile?.birthday ?: "") }
    var anniversary by remember(userProfile?.anniversary) { mutableStateOf(userProfile?.anniversary ?: "") }
    var foodPrefs by remember(userProfile?.foodPreferences) { mutableStateOf(userProfile?.foodPreferences ?: "") }
    var colors by remember(userProfile?.favoriteColors) { mutableStateOf(userProfile?.favoriteColors ?: "") }
    var songs by remember(userProfile?.favoriteSongs) { mutableStateOf(userProfile?.favoriteSongs ?: "") }
    var aesthetics by remember(userProfile?.aestheticNote) { mutableStateOf(userProfile?.aestheticNote ?: "") }
    
    var isEditMode by remember { mutableStateOf(false) }
    var partnerCodeInput by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aura Profile",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Serif
                )
                IconButton(onClick = {
                    if (isEditMode) {
                        // Save all changes
                        val updates = mapOf(
                            "name" to editName,
                            "birthday" to birthday,
                            "anniversary" to anniversary,
                            "foodPreferences" to foodPrefs,
                            "favoriteColors" to colors,
                            "favoriteSongs" to songs,
                            "aestheticNote" to aesthetics
                        )
                        userViewModel.updateExtendedProfile(updates)
                        isEditMode = false
                    } else {
                        isEditMode = true
                    }
                }) {
                    Icon(
                        imageVector = if (isEditMode) Icons.Default.Save else Icons.Default.Edit,
                        contentDescription = if (isEditMode) "Save" else "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Relationship Status / Pairing
            SectionHeader("Connection")
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (userProfile?.partnerId != null) {
                        Text("Linked with Partner", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("You are currently experiencing life together.", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { userViewModel.unlinkPartner() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Unlink Partner")
                        }
                    } else {
                        Text("Not Linked", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Share your Aura Code to pair:", fontSize = 12.sp)
                        Text(
                            text = userProfile?.userId ?: "...",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = partnerCodeInput,
                            onValueChange = { partnerCodeInput = it },
                            label = { Text("Enter Partner's Code") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { userViewModel.pairWithPartner(partnerCodeInput) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = partnerCodeInput.isNotBlank() && pairingState !is PairingState.Loading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (pairingState is PairingState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Text("Link Aura")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Appearance / Theme
            SectionHeader("Aesthetics & Theme")
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeButton("LIGHT", "Light", userProfile?.themePreference == "LIGHT") { userViewModel.setTheme("LIGHT") }
                ThemeButton("DARK", "Dark", userProfile?.themePreference == "DARK") { userViewModel.setTheme("DARK") }
                ThemeButton("SYSTEM", "Auto", userProfile?.themePreference == "SYSTEM" || userProfile?.themePreference == null) { userViewModel.setTheme("SYSTEM") }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Knowledge Base / Preferences
            SectionHeader("The Knowledge Base")
            ProfileTextField("Full Name", editName, isEditMode) { editName = it }
            ProfileTextField("Birthday", birthday, isEditMode) { birthday = it }
            ProfileTextField("Anniversary", anniversary, isEditMode) { anniversary = it }
            ProfileTextField("Food Preferences", foodPrefs, isEditMode) { foodPrefs = it }
            ProfileTextField("Favorite Colors", colors, isEditMode) { colors = it }
            ProfileTextField("Favorite Songs", songs, isEditMode) { songs = it }
            ProfileTextField("Aesthetic Notes", aesthetics, isEditMode, singleLine = false) { aesthetics = it }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Out
            TextButton(
                onClick = {
                    userViewModel.logout()
                    onLogout()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun RowScope.ThemeButton(value: String, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(label, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    label: String,
    value: String,
    enabled: Boolean,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = singleLine,
            maxLines = if (singleLine) 1 else 5,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = Color.Transparent
            )
        )
    }
}
