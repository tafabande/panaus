package com.ourspace.app.ui.features

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ourspace.app.data.model.*
import com.ourspace.app.data.repository.FeaturesRepository
import com.ourspace.app.data.util.DateUtils
import kotlinx.coroutines.Job
import com.ourspace.app.util.GlobalErrorHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeaturesViewModel(
    private val repository: FeaturesRepository,
    private val musicRepository: com.ourspace.app.data.repository.MusicRepository
) : ViewModel() {
    
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _todos = MutableStateFlow<List<TodoItem>>(emptyList())
    val todos: StateFlow<List<TodoItem>> = _todos.asStateFlow()

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    private val _moods = MutableStateFlow<List<Mood>>(emptyList())
    val moods: StateFlow<List<Mood>> = _moods.asStateFlow()

    private val _asks = MutableStateFlow<List<Ask>>(emptyList())
    val asks: StateFlow<List<Ask>> = _asks.asStateFlow()

    private val _interactions = MutableStateFlow<List<Interaction>>(emptyList())
    val interactions: StateFlow<List<Interaction>> = _interactions.asStateFlow()

    private val _memories = MutableStateFlow<List<Memory>>(emptyList())
    val memories: StateFlow<List<Memory>> = _memories.asStateFlow()

    private val _optimisticMemories = MutableStateFlow<List<Memory>>(emptyList())
    val optimisticMemories: StateFlow<List<Memory>> = _optimisticMemories.asStateFlow()

    private val _quizResults = MutableStateFlow<Map<String, GameResult?>>(emptyMap())
    val quizResults: StateFlow<Map<String, GameResult?>> = _quizResults.asStateFlow()

    private val _partnerMood = MutableStateFlow<Mood?>(null)
    val partnerMood: StateFlow<Mood?> = _partnerMood.asStateFlow()

    private val _relationshipEvents = MutableStateFlow<List<RelationshipEvent>>(emptyList())
    val relationshipEvents: StateFlow<List<RelationshipEvent>> = _relationshipEvents.asStateFlow()

    val combinedTimeline: StateFlow<List<TimelineItem>> = combine(
        memories,
        relationshipEvents,
        events
    ) { memories, relEvents, calEvents ->
        val items = mutableListOf<TimelineItem>()
        
        // Add Memories
        memories.forEach { items.add(TimelineItem.Photo(it)) }
        
        // Add Relationship Events
        relEvents.forEach { items.add(TimelineItem.Relationship(it)) }
        
        // Add Anniversaries from Calendar
        calEvents.filter { it.category.contains("Anniversary", ignoreCase = true) || it.title.contains("Anniversary", ignoreCase = true) }
            .forEach { items.add(TimelineItem.Anniversary(it)) }
            
        items.sortedByDescending { it.sortDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _songSuggestions = MutableStateFlow<List<com.ourspace.app.data.api.SongResult>>(emptyList())
    val songSuggestions: StateFlow<List<com.ourspace.app.data.api.SongResult>> = _songSuggestions.asStateFlow()

    private var notesJob: Job? = null
    private var todosJob: Job? = null
    private var eventsJob: Job? = null
    private var moodsJob: Job? = null
    private var asksJob: Job? = null
    private var interactionsJob: Job? = null
    private var memoriesJob: Job? = null
    private var partnerMoodJob: Job? = null
    
    // Throttling for interactions
    private val lastInteractionTimes = mutableMapOf<String, Long>()
    private val INTERACTION_COOLDOWN = 3000L // 3 seconds



    // Load Data based on UserProfile
    fun startObserving(userProfile: UserProfile) {
        val coupleId = userProfile.coupleId ?: return

        notesJob?.cancel()
        notesJob = viewModelScope.launch {
            repository.observeNotes(coupleId)
                .catch { e -> GlobalErrorHandler.recordException(e) }
                .collect { _notes.value = it }
        }

        todosJob?.cancel()
        todosJob = viewModelScope.launch {
            repository.observeTodos(coupleId)
                .catch { e -> GlobalErrorHandler.recordException(e) }
                .collect { _todos.value = it }
        }

        eventsJob?.cancel()
        eventsJob = viewModelScope.launch {
            repository.observeEvents(coupleId)
                .catch { e -> GlobalErrorHandler.recordException(e) }
                .collect { _events.value = it }
        }

        moodsJob?.cancel()
        moodsJob = viewModelScope.launch {
            repository.observeMoods(coupleId)
                .catch { e -> GlobalErrorHandler.recordException(e) }
                .collect { _moods.value = it }
        }

        asksJob?.cancel()
        asksJob = viewModelScope.launch {
            repository.observeAsks(coupleId)
                .catch { e -> GlobalErrorHandler.recordException(e) }
                .collect { _asks.value = it }
        }

        interactionsJob?.cancel()
        interactionsJob = viewModelScope.launch {
            repository.observeInteractions(coupleId)
                .catch { e -> GlobalErrorHandler.recordException(e) }
                .collect { _interactions.value = it }
        }

        memoriesJob?.cancel()
        memoriesJob = viewModelScope.launch {
            repository.observeMemories(coupleId)
                .catch { e -> GlobalErrorHandler.recordException(e) }
                .collect { _memories.value = it }
        }

        viewModelScope.launch {
            repository.getRelationshipEvents(coupleId)
                .catch { e -> GlobalErrorHandler.recordException(e) }
                .collect { _relationshipEvents.value = it }
        }
    }

    fun uploadMemory(coupleId: String, userId: String, localUri: android.net.Uri, caption: String = "") {
        val tempId = java.util.UUID.randomUUID().toString()
        val optimisticMemory = Memory(
            id = tempId,
            userId = userId,
            coupleId = coupleId,
            imageUrl = localUri.toString(),
            caption = caption,
            timestamp = System.currentTimeMillis(),
            status = "SENDING"
        )
        
        // Add to optimistic list
        _optimisticMemories.value = listOf(optimisticMemory) + _optimisticMemories.value
        
        viewModelScope.launch {
            try {
                repository.uploadMemory(coupleId, optimisticMemory, localUri)
                // Success: remove from optimistic (Firestore listener will pick it up)
                _optimisticMemories.value = _optimisticMemories.value.filter { it.id != tempId }
            } catch (e: Exception) {
                GlobalErrorHandler.recordException(e)
                // Failed: update status for UI
                _optimisticMemories.value = _optimisticMemories.value.map {
                    if (it.id == tempId) it.copy(status = "FAILED") else it
                }
            }
        }
    }

    fun submitQuiz(coupleId: String, response: QuizResponse) {
        viewModelScope.launch {
            try {
                repository.submitQuizResponse(coupleId, response)
            } catch (e: Exception) {
                GlobalErrorHandler.recordException(e)
            }
        }
    }

    fun observeQuiz(coupleId: String, quizId: String) {
        viewModelScope.launch {
            repository.observeQuizResults(coupleId, quizId)
                .catch { e -> GlobalErrorHandler.recordException(e) }
                .collect { result ->
                    _quizResults.value = _quizResults.value + (quizId to result)
                }
        }
    }

    fun saveNote(coupleId: String, note: Note) {
        viewModelScope.launch {
            try {
                repository.saveNote(coupleId, note)
            } catch (e: Exception) {
                GlobalErrorHandler.recordException(e)
            }
        }
    }

    fun deleteNote(coupleId: String, noteId: String) {
        viewModelScope.launch {
            try {
                repository.deleteNote(coupleId, noteId)
            } catch (e: Exception) {
                GlobalErrorHandler.recordException(e)
            }
        }
    }

    fun addTodo(coupleId: String, creatorId: String, title: String, assignedTo: String, category: String = "General") {
        if (title.isBlank()) return
        viewModelScope.launch {
            GlobalErrorHandler.runWithCatch {
                val todo = TodoItem(
                    coupleId = coupleId,
                    title = title.trim(),
                    assignedTo = assignedTo,
                    category = category,
                    isCompleted = false,
                    createdBy = creatorId,
                    timestamp = System.currentTimeMillis(),
                    completedAt = null
                )
                repository.addTodo(todo)
            }
        }
    }

    fun toggleTodo(todo: TodoItem) {
        viewModelScope.launch {
            GlobalErrorHandler.runWithCatch {
                val completedAt = if (!todo.isCompleted) System.currentTimeMillis() else null
                repository.toggleTodo(todo.id, !todo.isCompleted, completedAt)
            }
        }
    }

    fun deleteTodo(todoId: String) {
        viewModelScope.launch {
            GlobalErrorHandler.runWithCatch {
                repository.deleteTodo(todoId)
            }
        }
    }

    fun addEvent(coupleId: String, creatorId: String, title: String, date: String, time: String, category: String) {
        if (title.isBlank() || date.isBlank()) return
        viewModelScope.launch {
            GlobalErrorHandler.runWithCatch {
                val event = CalendarEvent(
                    coupleId = coupleId,
                    title = title.trim(),
                    date = date,
                    time = time,
                    category = category,
                    createdBy = creatorId,
                    timestamp = System.currentTimeMillis()
                )
                repository.addEvent(event)
            }
        }
    }

    fun addMood(userId: String, coupleId: String, moodValue: Int, emoji: String, note: String) {
        viewModelScope.launch {
            GlobalErrorHandler.runWithCatch {
                val mood = Mood(
                    userId = userId,
                    coupleId = coupleId,
                    moodValue = moodValue,
                    emoji = emoji,
                    note = note.trim(),
                    timestamp = System.currentTimeMillis()
                )
                repository.updateMood(mood)
            }
        }
    }

    fun addAsk(coupleId: String, fromUserId: String, toUserId: String, text: String, type: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            GlobalErrorHandler.runWithCatch {
                val ask = Ask(
                    coupleId = coupleId,
                    fromUserId = fromUserId,
                    toUserId = toUserId,
                    requestText = text.trim(),
                    requestType = type,
                    status = "pending",
                    responseText = "",
                    timestamp = System.currentTimeMillis(),
                    respondedAt = null
                )
                repository.addAsk(ask)
            }
        }
    }

    fun updateAskStatus(askId: String, status: String) {
        viewModelScope.launch {
            GlobalErrorHandler.runWithCatch {
                repository.updateAskStatus(askId, status, System.currentTimeMillis())
            }
        }
    }

    fun sendInteraction(coupleId: String, senderId: String, type: String) {
        val currentTime = System.currentTimeMillis()
        val lastTime = lastInteractionTimes[type] ?: 0L
        
        if (currentTime - lastTime < INTERACTION_COOLDOWN) {
            Log.d("FeaturesViewModel", "Throttling interaction: $type")
            return
        }
        
        lastInteractionTimes[type] = currentTime
        
        viewModelScope.launch {
            GlobalErrorHandler.runWithCatch {
                val interaction = Interaction(
                    coupleId = coupleId,
                    senderId = senderId,
                    type = type,
                    timestamp = System.currentTimeMillis(),
                    status = "unread"
                )
                repository.sendInteraction(interaction)
            }
        }
    }

    fun markInteractionAsRead(interactionId: String) {
        viewModelScope.launch {
            GlobalErrorHandler.runWithCatch {
                repository.markInteractionAsRead(interactionId)
            }
        }
    }

    fun saveRelationshipEvent(event: RelationshipEvent) {
        viewModelScope.launch {
            try {
                repository.saveRelationshipEvent(event)
            } catch (e: Exception) {
                GlobalErrorHandler.recordException(e)
            }
        }
    }

    fun deleteRelationshipEvent(coupleId: String, eventId: String) {
        viewModelScope.launch {
            try {
                repository.deleteRelationshipEvent(coupleId, eventId)
            } catch (e: Exception) {
                GlobalErrorHandler.recordException(e)
            }
        }
    }
}

sealed class TimelineItem {
    data class Relationship(val event: RelationshipEvent) : TimelineItem()
    data class Photo(val memory: Memory) : TimelineItem()
    data class Anniversary(val event: CalendarEvent) : TimelineItem()
    
    val sortDate: String get() = when(this) {
        is Relationship -> event.date
        is Photo -> {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.format(Date(memory.timestamp))
        }
        is Anniversary -> event.date
    }

    val displayDate: String get() = when(this) {
        is Relationship -> event.date
        is Photo -> DateUtils.formatDateTime(memory.timestamp)
        is Anniversary -> event.date
    }
}
