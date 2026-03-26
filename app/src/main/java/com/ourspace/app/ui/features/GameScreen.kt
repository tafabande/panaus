package com.ourspace.app.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.model.QuizResponse
import com.ourspace.app.data.model.GameResult

// ── Game content ──────────────────────────────────────────────────────────────

private val OUR_STORY_QUESTIONS = listOf(
    "Where was our very first date?",
    "What was the first song we danced to?",
    "Who said 'I love you' first?",
    "What is your partner's favourite childhood memory?",
    "What is something your partner is secretly proud of?",
    "Describe your partner in three words.",
    "What is the funniest moment we've shared together?",
    "What habit of your partner do you secretly adore?",
    "What is the most important lesson your relationship has taught you?",
    "What do you hope your partner remembers most about today?"
)

private val LOVE_LANGUAGE_QUESTIONS = listOf(
    "Words of Affirmation: Name one thing your partner said that stayed with you.",
    "Acts of Service: What is one small thing your partner does that means the world?",
    "Quality Time: What is your favourite memory of uninterrupted time together?",
    "Physical Touch: What is your partner's favourite way to be comforted?",
    "Gift Giving: What is the most meaningful gift you ever received from your partner?",
    "When do you feel most loved by your partner?",
    "How does your partner show they care when you're having a bad day?",
    "What is your love language and does your partner know it?",
    "What can your partner do more of to make you feel cherished?",
    "What is one love language you'd like to practise more?"
)

private val MEMORY_MATCH_PROMPTS = listOf(
    "Name a place we visited together.",
    "Name a meal we shared that you still think about.",
    "Name a film we watched together at home.",
    "Name something we laughed uncontrollably about.",
    "Name a song that takes you back to us.",
    "Name a challenge we overcame together.",
    "Name a spontaneous thing we did together.",
    "Name a tradition we've created as a couple."
)

private val COLLAB_ART_PROMPTS = listOf(
    "Take turns adding one line to describe your perfect day together.",
    "Each write three words that describe your relationship — share and compare.",
    "Describe your partner as a season. Why that season?",
    "Write the opening line of your love story if it were a novel.",
    "Each name one colour that represents your partner. Explain your choice.",
    "What would the title of your relationship's movie be?",
    "Write a haiku about something you love about your partner.",
    "Describe your partner using only food metaphors."
)

data class GameContent(val title: String, val questions: List<String>)

@Composable
fun GameScreen(
    userProfile: UserProfile?,
    viewModel: FeaturesViewModel,
    onBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    // Dialog state
    var activeGame by remember { mutableStateOf<GameContent?>(null) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var quizAnswers by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    
    val quizId = activeGame?.title?.uppercase()?.replace(" ", "_") ?: ""
    val results by viewModel.quizResults.collectAsState()
    val currentResult = results[quizId]

    // Start observing results when a game is selected
    LaunchedEffect(quizId) {
        if (quizId.isNotEmpty() && userProfile?.coupleId != null) {
            viewModel.observeQuiz(userProfile.coupleId, quizId)
        }
    }

    // Show game dialog when a card is tapped
    activeGame?.let { game ->
        GameDialog(
            game = game,
            questionIndex = currentQuestionIndex,
            answer = quizAnswers[currentQuestionIndex] ?: "",
            result = currentResult,
            onAnswerChange = { quizAnswers = quizAnswers + (currentQuestionIndex to it) },
            onNext = {
                if (currentQuestionIndex < game.questions.size - 1)
                    currentQuestionIndex++
            },
            onPrev = {
                if (currentQuestionIndex > 0) currentQuestionIndex--
            },
            onSubmit = {
                if (userProfile?.coupleId != null) {
                    val response = QuizResponse(
                        quizId = quizId,
                        userId = userProfile.userId,
                        answers = quizAnswers
                    )
                    viewModel.submitQuiz(userProfile.coupleId, response)
                }
            },
            onDismiss = { 
                activeGame = null
                currentQuestionIndex = 0
                quizAnswers = emptyMap()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Play & Connect",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Serif
            )
        }

        // Weekly Spark Section
        Card(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Weekly Spark", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, letterSpacing = 1.sp, fontFamily = FontFamily.Serif)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "What is a dream you haven't shared with me yet?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Intimate Quizzes", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InteractiveCard(
                title = "Our Story",
                subtitle = "${OUR_STORY_QUESTIONS.size} Questions",
                modifier = Modifier.weight(1f),
                onClick = { activeGame = GameContent("Our Story", OUR_STORY_QUESTIONS); currentQuestionIndex = 0 }
            )
            InteractiveCard(
                title = "Love Languages",
                subtitle = "Discover More",
                modifier = Modifier.weight(1f),
                onClick = { activeGame = GameContent("Love Languages", LOVE_LANGUAGE_QUESTIONS); currentQuestionIndex = 0 }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Mini-Games", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        InteractiveCard(
            title = "Memory Match",
            subtitle = "Revisit our favourite moments in a classic game of match.",
            modifier = Modifier.fillMaxWidth().height(100.dp),
            onClick = { activeGame = GameContent("Memory Match", MEMORY_MATCH_PROMPTS); currentQuestionIndex = 0 }
        )

        Spacer(modifier = Modifier.height(16.dp))

        InteractiveCard(
            title = "Collaborative Art",
            subtitle = "Draw something together, one stroke at a time.",
            modifier = Modifier.fillMaxWidth().height(100.dp),
            onClick = { activeGame = GameContent("Collaborative Art", COLLAB_ART_PROMPTS); currentQuestionIndex = 0 }
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ── Interactive card ──────────────────────────────────────────────────────────

@Composable
fun InteractiveCard(title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Game dialog ───────────────────────────────────────────────────────────────

@Composable
fun GameDialog(
    game: GameContent,
    questionIndex: Int,
    answer: String,
    result: GameResult?,
    onAnswerChange: (String) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(game.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Serif)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Text(
                    "${questionIndex + 1} / ${game.questions.size}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = game.questions[questionIndex],
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = answer,
                    onValueChange = onAnswerChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Your answer...") },
                    shape = RoundedCornerShape(12.dp)
                )

                if (result != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    ComparisonResultView(result, questionIndex)
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (questionIndex == 0) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onPrev,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("← Prev")
                        }
                    }
                    
                    if (questionIndex < game.questions.size - 1) {
                        Button(
                            onClick = onNext,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Next →")
                        }
                    } else {
                        Button(
                            onClick = onSubmit,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Submit & Sync")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonResultView(result: GameResult, questionIndex: Int) {
    val myAnswer = result.user1Answers[questionIndex] ?: "..."
    val partnerAnswer = result.user2Answers[questionIndex] ?: "..."
    val isMatch = result.matches.contains(questionIndex)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isMatch) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Text(
            if (isMatch) "✨ It's a match!" else "⌛ Different vibes",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (isMatch) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Me:", fontSize = 10.sp, color = Color.Gray)
                Text(myAnswer, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("Partner:", fontSize = 10.sp, color = Color.Gray)
                Text(partnerAnswer, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { result.matchPercentage / 100f },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Text(
            "Overall Match: ${result.matchPercentage.toInt()}%",
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
