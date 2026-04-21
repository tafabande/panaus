package com.ourspace.app.ui.user
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ourspace.app.data.model.Interaction
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.util.DateUtils
import com.ourspace.app.ui.components.EmptyState
import com.ourspace.app.ui.features.FeaturesViewModel
import com.ourspace.app.ui.theme.AuraColors
import kotlin.random.Random

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    userViewModel: UserViewModel,
    featuresViewModel: FeaturesViewModel,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToMood: () -> Unit,
    onNavigateToMemories: () -> Unit,
    profileTheme: Color = MaterialTheme.colorScheme.primary
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
        // Background decor moved here to avoid blocking touches
        FloatingDecor()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
        if (userProfile == null) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                com.ourspace.app.ui.components.SkeletonLoader(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(24.dp)
                )
                com.ourspace.app.ui.components.SkeletonLoader(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    shape = RoundedCornerShape(24.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    com.ourspace.app.ui.components.SkeletonLoader(
                        modifier = Modifier.weight(1f).height(140.dp),
                        shape = RoundedCornerShape(24.dp)
                    )
                    com.ourspace.app.ui.components.SkeletonLoader(
                        modifier = Modifier.weight(1f).height(140.dp),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }
        } else {
        // App Top Bar / Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HoneyBee",
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
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(2.dp, profileTheme, CircleShape)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    AsyncImage(
                        model = userProfile?.avatarUrl ?: "https://api.dicebear.com/7.x/thumbs/png?seed=${userProfile?.userId}",
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Quick Status Update Feature
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface) // Blending surface
        ) {
            var showStatusDialog by remember { mutableStateOf(false) }
            val currentEmoji = userProfile?.statusEmoji ?: "😊"
            val currentNote = userProfile?.statusNote ?: "How are you feeling?"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStatusDialog = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(currentEmoji, style = MaterialTheme.typography.headlineSmall)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Quick Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = currentNote,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Status",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showStatusDialog) {
                var emoji by remember { mutableStateOf(currentEmoji) }
                var note by remember { mutableStateOf(if (currentNote == "How are you feeling?") "" else currentNote) }

                AlertDialog(
                    onDismissRequest = { showStatusDialog = false },
                    title = { Text("Update Current Status") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Simple Emoji Picker Row
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf("😊", "🤒", "😴", "🍕", "💻", "🏃").forEach { e ->
                                    Surface(
                                        onClick = { emoji = e },
                                        shape = CircleShape,
                                        color = if (emoji == e) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(e, fontSize = 20.sp)
                                        }
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = note,
                                onValueChange = { note = it },
                                label = { Text("What's up?") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            userViewModel.updateQuickStatus(emoji, note.ifBlank { "Feeling good!" })
                            showStatusDialog = false
                        }) {
                            Text("Update")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showStatusDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        // Split Mood/Status View
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // My Status
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { 
                        Log.d("DashboardScreen", "Navigating to Mood")
                        onNavigateToMood() 
                    }
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

            // Their Status & Interactions
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)) // Distinct but soft
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val interactionTypes = listOf(
                            Triple("poke", "Poke 👉", Color(0xFFFD36A4)),
                            Triple("hug", "Hug 🤗", Color(0xFFE91E63)),
                            Triple("kiss", "Kiss 💋", Color(0xFF8B00FF))
                        )
                        
                        val isPaired = !userProfile?.coupleId.isNullOrBlank()
                        
                        interactionTypes.forEach { interaction ->
                            val type = interaction.first
                            val label = interaction.second
                            
                            // Using FilledTonalButton for better visibility and larger hit area
                            FilledTonalButton(
                                onClick = {
                                    if (isPaired) {
                                        Log.d("DashboardScreen", "TRACE: User tapped interaction button: $type")
                                        userProfile?.let { u ->
                                            featuresViewModel.sendInteraction(u.coupleId!!, u.userId, type)
                                        }
                                    }
                                },
                                enabled = isPaired,
                                modifier = Modifier.weight(1f).height(40.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logging Mood Row
        Text("Log Wellness", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val wellnessOptions = listOf(
                Triple("Devastated", 1, "😭"),
                Triple("Sad", 2, "😢"),
                Triple("Low", 3, "😔"),
                Triple("Neutral", 4, "😐"),
                Triple("Good", 5, "🙂"),
                Triple("Happy", 6, "😊"),
                Triple("Great", 7, "😁"),
                Triple("Loved", 8, "😍"),
                Triple("Amazing", 9, "🤩"),
                Triple("Tired", 10, "😴"),
                Triple("Annoyed", 11, "😤"),
                Triple("Angry", 12, "😡"),
                Triple("Sick", 13, "🤒"),
                Triple("Nauseated", 14, "🤢"),
                Triple("Healthy", 15, "💪")
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
        
        // Aura Memories / Journey
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { 
                    Log.d("DashboardScreen", "Navigating to Memories")
                    onNavigateToMemories() 
                }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background decor
                Text(
                    "✨", 
                    fontSize = 80.sp, 
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = 20.dp, y = 20.dp).alpha(0.1f)
                )
                
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Our Journey", 
                        fontSize = 14.sp, 
                        color = MaterialTheme.colorScheme.primary, 
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "AURA MEMORIES", 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "View Album", 
                            fontSize = 14.sp, 
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ArrowForward, 
                            contentDescription = null, 
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Days at a glance / Mini timeline
        Text("Our Recent Days", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                val recentInteractions = interactions
                    .filter { it.senderId != userProfile?.userId }
                    .sortedByDescending { it.timestamp }
                    .take(5)

                if (recentInteractions.isEmpty()) {
                    Text(
                        "No recent activity to show.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    recentInteractions.forEachIndexed { index, interaction ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val color = when(interaction.type) {
                                "poke" -> MaterialTheme.colorScheme.primary
                                "hug" -> MaterialTheme.colorScheme.secondary
                                "kiss" -> MaterialTheme.colorScheme.tertiary
                                "status_update" -> MaterialTheme.colorScheme.outline
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                val partnerName = partnerProfile?.name ?: "Partner"
                                val action = when(interaction.type) {
                                    "poke" -> "sent you a poke 👉"
                                    "hug" -> "sent you a hug 🤗"
                                    "kiss" -> "sent you a kiss 💋"
                                    "status_update" -> "updated their status 📝"
                                    else -> "interacted with you"
                                }
                                Text("$partnerName $action", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                Text(DateUtils.formatRelativeTime(interaction.timestamp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (index < recentInteractions.size - 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
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
        } // End of conditional `else` rendering user content
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
                onDismiss = { featuresViewModel.markInteractionAsRead(poke.coupleId, poke.id) },
                onPokeBack = {
                    userProfile?.let { u ->
                        featuresViewModel.sendInteraction(u.coupleId ?: "", u.userId, "poke")
                        featuresViewModel.markInteractionAsRead(poke.coupleId, poke.id)
                    }
                }
            )
        }
    }

    // Removed FloatingDecor() call from here to background
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
