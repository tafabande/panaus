package com.ourspace.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import com.ourspace.app.data.model.Interaction
import com.ourspace.app.ui.features.FeaturesViewModel
import com.ourspace.app.data.util.DateUtils
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import kotlin.random.Random

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    userViewModel: UserViewModel,
    featuresViewModel: FeaturesViewModel,
    onNavigateToAnalytics: () -> Unit
) {
    val userProfile by userViewModel.userProfile.collectAsState()
    val partnerProfile by userViewModel.partnerProfile.collectAsState()
    val interactions by featuresViewModel.interactions.collectAsState()
    val scrollState = rememberScrollState()
    var selectedWellness by remember { mutableStateOf<String?>(null) }

    val isBirthday = DateUtils.isToday(userProfile?.birthday) || DateUtils.isToday(partnerProfile?.birthday)
    val isAnniversary = DateUtils.isToday(userProfile?.anniversary)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
        if (userProfile == null) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                trackColor = Color.Transparent
            )
        }
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
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Serif // Simulating Noto Serif
                )
                Text(
                    text = if (userProfile == null) "Connecting..." else "Welcome back, ${userProfile?.name ?: "darling"}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.SansSerif
                )
                if (userProfile == null) {
                    TextButton(onClick = { /* UserViewModel already observes, but we could trigger a refresh */ }) {
                        Text("Retry", fontSize = 12.sp)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateToAnalytics) {
                    Icon(
                        imageVector = Icons.Filled.BarChart,
                        contentDescription = "Analytics",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                val userColor = com.ourspace.app.ui.theme.AuraColors.fromName(userProfile?.themeColor)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(2.dp, userColor, CircleShape)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    com.ourspace.app.ui.components.AsyncImage(
                        model = userProfile?.avatarUrl ?: "https://api.dicebear.com/7.x/thumbs/png?seed=${userProfile?.userId}",
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.layout.ContentScale.Crop
                    )
                }
            }
        }

        // Split Mood/Status View
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // My Status
            Card(
                modifier = Modifier.weight(1f).height(140.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val moodText = userProfile?.mood ?: "Peaceful"
                    Text("My Heart", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary, fontFamily = FontFamily.Serif)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(moodText, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Update >", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                }
            }

            // Their Status
            Card(
                modifier = Modifier.weight(1f).height(140.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val partnerMood = partnerProfile?.mood ?: "Joyful"
                    val partnerName = partnerProfile?.name ?: "Partner"
                    val lastInteraction = interactions.firstOrNull { it.senderId != userProfile?.userId }
                    
                    Text("$partnerName's Art", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary, fontFamily = FontFamily.Serif)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(partnerMood, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    
                    if (lastInteraction != null) {
                        Text(
                            "Last ${lastInteraction.type}: ${DateUtils.formatRelativeTime(lastInteraction.timestamp)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                userProfile?.let { u ->
                                    featuresViewModel.sendInteraction(u.coupleId ?: "", u.userId, "poke")
                                }
                            },
                        ) {
                            Text("Poke 👉", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logging Mood Row
        Text("Log Wellness", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val wellnessOptions = listOf(
                Triple("Healthy", 5, "😊"),
                Triple("Sick", 1, "🤒"),
                Triple("Nauseated", 2, "🤢")
            )
            wellnessOptions.forEach { (label, moodValue, emoji) ->
                val isSelected = selectedWellness == label
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedWellness = label
                        userProfile?.let { profile ->
                            featuresViewModel.addMood(
                                userId = profile.userId,
                                coupleId = profile.coupleId ?: "",
                                moodValue = moodValue,
                                emoji = emoji,
                                note = "Feeling $label"
                            )
                        }
                    },
                    label = { Text(label) },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Days at a glance / Mini timeline
        Text("Our Recent Days", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Activity Row 1
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Coffee Date at The Local", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                        Text("Yesterday", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Activity Row 2
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Long Walk in the Park", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                        Text("Sunday", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // Location Placeholder (GMS removal requirement)
        Text("Location", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { com.ourspace.app.util.GlobalErrorHandler.showMessage("Still not implemented - upgrade version") },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📍", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Real-time Check-in", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                    Text("Share your location safely", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Spacer(modifier = Modifier.height(100.dp)) // NavBar cushion
    }

    if (isBirthday || isAnniversary) {
        CelebrationOverlay(
            title = if (isBirthday) "Happy Birthday! 🎂" else "Happy Anniversary! ❤️",
            subtitle = if (isBirthday) "Special day for a special someone." else "Another year of love and growth."
        )
    }

    // Interaction Pulse for incoming pokes (only those that are UNREAD and from PARTNER)
    val unreadPartnerPokes = interactions.filter { 
        it.status == "unread" && it.senderId != userProfile?.userId 
    }.sortedByDescending { it.timestamp }
    
    val latestPoke = unreadPartnerPokes.firstOrNull()
    
    AnimatedVisibility(
        visible = latestPoke != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        latestPoke?.let { poke ->
            IncomingInteractionPulse(
                interaction = poke,
                onDismiss = { featuresViewModel.markInteractionAsRead(poke.id) },
                onPokeBack = {
                    userProfile?.let { u ->
                        featuresViewModel.sendInteraction(u.coupleId ?: "", u.userId, "poke")
                        featuresViewModel.markInteractionAsRead(poke.id)
                    }
                }
            )
        }
    }

    // Lovely floating decor
    FloatingDecor()
}
}

@Composable
fun CelebrationOverlay(title: String, subtitle: String) {
    var visible by remember { mutableStateOf(true) }
    
    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { visible = false },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text("Tap to dismiss", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
            
            RepeatingHearts()
        }
    }
}

@Composable
fun RepeatingHearts() {
    val infiniteTransition = rememberInfiniteTransition(label = "hearts")
    val items = remember { List(15) { Random.nextFloat() } }
    
    items.forEachIndexed { index, startDelay ->
        val yOffset by infiniteTransition.animateFloat(
            initialValue = -100f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 4000, 
                    delayMillis = (startDelay * 3000).toInt(), 
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "heart_y_$index"
        )
        
        Box(modifier = Modifier.offset(x = (index * 40).dp, y = yOffset.dp)) {
            Text("❤️", fontSize = 24.sp)
        }
    }
}
@Composable
fun IncomingInteractionPulse(
    interaction: Interaction,
    onDismiss: () -> Unit,
    onPokeBack: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        ElevatedCard(
            modifier = Modifier
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (interaction.type == "poke") "👉" else "❤️", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "You've been ${interaction.type}d!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    ) {
                        Text("Dismiss", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    
                    Button(
                        onClick = onPokeBack,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Poke Back")
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingDecor() {
    val items = listOf("✨", "🌸", "☁️", "💖")
    val count = 6
    
    Box(modifier = Modifier.fillMaxSize()) {
        repeat(count) { i ->
            val randomX = remember { Random.nextFloat() }
            val randomY = remember { Random.nextFloat() }
            val item = items[i % items.size]
            
            val infiniteTransition = rememberInfiniteTransition(label = "float")
            val yOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 20f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000 + i * 500, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "y"
            )

            Text(
                text = item,
                modifier = Modifier
                    .offset(x = (randomX * 300).dp, y = (randomY * 600 + yOffset).dp)
                    .alpha(0.3f),
                fontSize = 16.sp
            )
        }
    }
}
