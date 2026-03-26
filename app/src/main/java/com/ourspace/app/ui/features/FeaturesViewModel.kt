package com.ourspace.app.ui.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ourspace.app.data.model.*
import com.ourspace.app.data.repository.FeaturesRepository
import com.ourspace.app.data.util.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import com.ourspace.app.util.GlobalErrorHandler

class FeaturesViewModel(private val repository: FeaturesRepository = FeaturesRepository()) : ViewModel() {
    
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

    private var notesJob: Job? = null
    private var todosJob: Job? = null
    private var eventsJob: Job? = null
    private var moodsJob: Job? = null
    private var asksJob: Job? = null
    private var interactionsJob: Job? = null
    private var memoriesJob: Job? = null
    private var partnerMoodJob: Job? = null

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

        // Observe partner mood
        val partnerId = userProfile.partnerId ?: ""
        if (partnerId.isNotEmpty()) {
            partnerMoodJob?.cancel()
            partnerMoodJob = viewModelScope.launch {
                repository.observePartnerMood(coupleId, partnerId)
                    .catch { e -> GlobalErrorHandler.recordException(e) }
                    .collect { _partnerMood.value = it }
            }
        }
    }

    fun uploadMemory(coupleId: String, userId: String, localUri: android.net.Uri) {
        val tempId = java.util.UUID.randomUUID().toString()
        val optimisticMemory = Memory(
            id = tempId,
            userId = userId,
            imageUrl = localUri.toString(),
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
        viewModelScope.launch {
            GlobalErrorHandler.runWithCatch {
                val interaction = Interaction(
                    coupleId = coupleId,
                    senderId = senderId,
                    type = type,
                    timestamp = System.currentTimeMillis()
                )
                repository.sendInteraction(interaction)
            }
        }
    }
}
