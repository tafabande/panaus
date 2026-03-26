package com.ourspace.app.ui.user

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.util.DateUtils
import com.ourspace.app.ui.components.AuraDatePickerField
import java.util.Calendar

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

    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

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
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, top = 8.dp),
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
                IconButton(
                    onClick = {
                        if (isEditMode) {
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
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = if (isEditMode) "Save" else "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // PFP and Avatar Section
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                val themeColor = com.ourspace.app.ui.theme.AuraColors.fromName(userProfile?.themeColor)
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .border(BorderStroke(4.dp, themeColor), CircleShape)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = userProfile?.avatarUrl ?: "https://api.dicebear.com/7.x/thumbs/png?seed=${userProfile?.userId}",
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(userProfile?.name ?: "User", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(userProfile?.email ?: "", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)

                    // Added Debug Logout Button
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            userViewModel.logout()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text("Debug Logout", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
                    }
                }
            }

            AnimatedVisibility(visible = isEditMode) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                    SectionHeader("Customize Avatar")
                    val avatars = listOf("Felix", "Aneka", "Abbie", "Milo", "Leo", "Sasha")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(avatars) { seed ->
                            val url = "https://api.dicebear.com/9.x/avataaars/svg?seed=$seed"
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(if (userProfile?.avatarUrl == url) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { userViewModel.updateExtendedProfile(mapOf("avatarUrl" to url)) }
                                    .padding(4.dp)
                            ) {
                                AsyncImage(model = url, contentDescription = seed, modifier = Modifier.fillMaxSize())
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader("Profile Theme Color")
                    val colorsList = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEEAD", "#D4A5A5", "#9B59B6")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(colorsList) { hex ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        BorderStroke(
                                            if (userProfile?.themeColor == hex) 3.dp else 0.dp,
                                            MaterialTheme.colorScheme.onSurface
                                        ),
                                        CircleShape
                                    )
                                    .clickable { userViewModel.updateExtendedProfile(mapOf("themeColor" to hex)) }
                            )
                        }
                    }
                }
            }

            // Relationship Status / Pairing
            SectionHeader("Connection")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .animateContentSize(animationSpec = spring()),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (userProfile?.partnerId != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Linked with Partner", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("You are currently experiencing life together.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { userViewModel.unlinkPartner() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text("Unlink Partner", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Text("Not Linked", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Share your Aura Code to pair:", fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userProfile?.userId ?: "...",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp).weight(1f)
                            )
                            IconButton(onClick = {
                                userProfile?.userId?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                }
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        var partnerCodeInput by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = partnerCodeInput,
                            onValueChange = { partnerCodeInput = it },
                            label = { Text("Enter Partner's Code") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { userViewModel.pairWithPartner(partnerCodeInput) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = partnerCodeInput.isNotBlank() && pairingState !is PairingState.Loading,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (pairingState is PairingState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Text("Link Aura", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Appearance / Theme
            SectionHeader("Aesthetics & Theme")
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Theme Mode", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(bottom = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeButton("Light", userProfile?.themePreference == "LIGHT") { userViewModel.setTheme("LIGHT") }
                        ThemeButton("Dark", userProfile?.themePreference == "DARK") { userViewModel.setTheme("DARK") }
                        ThemeButton("Auto", userProfile?.themePreference == "SYSTEM" || userProfile?.themePreference == null) { userViewModel.setTheme("SYSTEM") }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Discoverable", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Let others find you and your partner by your Aura codes.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Switch(
                            checked = userProfile?.isDiscoverable == true,
                            onCheckedChange = { userViewModel.setDiscoverability(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Knowledge Base / Preferences
            SectionHeader("The Knowledge Base")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = spring()),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    ProfileTextField("Full Name", editName, isEditMode) { editName = it }
                    
                    if (isEditMode) {
                        AuraDatePickerField(
                            value = birthday,
                            onValueChange = { birthday = it },
                            label = "Birthday",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        AuraDatePickerField(
                            value = anniversary,
                            onValueChange = { anniversary = it },
                            label = "Anniversary",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        ProfileTextField("Birthday", birthday, false) { }
                        ProfileTextField("Anniversary", anniversary, false) { }
                    }

                    ProfileTextField("Food Preferences", foodPrefs, isEditMode) { foodPrefs = it }
                    ProfileTextField("Favorite Colors", colors, isEditMode) { colors = it }
                    ProfileTextField("Favorite Songs", songs, isEditMode) { songs = it }
                    ProfileTextField("Aesthetic Notes", aesthetics, isEditMode, singleLine = false) { aesthetics = it }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Out
            TextButton(
                onClick = {
                    userViewModel.logout()
                    onLogout()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
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
fun RowScope.ThemeButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
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
    onClick: (() -> Unit)? = null,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .let { if (onClick != null && enabled) it.clickable { onClick() } else it },
            enabled = enabled && onClick == null,
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
