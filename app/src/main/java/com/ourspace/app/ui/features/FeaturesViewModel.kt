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

    private var notesJob: Job? = null
    private var todosJob: Job? = null
    private var eventsJob: Job? = null
    private var moodsJob: Job? = null
    private var asksJob: Job? = null
    private var interactionsJob: Job? = null

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
    }

    fun sendNote(coupleId: String, senderId: String, receiverId: String?, content: String) {
        if (content.isBlank() || receiverId == null) return
        viewModelScope.launch {
            GlobalErrorHandler.runWithCatch {
                val note = Note(
                    coupleId = coupleId,
                    senderId = senderId,
                    receiverId = receiverId,
                    content = content.trim(),
                    createdAt = DateUtils.getCurrentIsoTime()
                )
                repository.sendNote(note)
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
                    createdAt = DateUtils.getCurrentIsoTime(),
                    completedAt = null
                )
                repository.addTodo(todo)
            }
        }
    }

    fun toggleTodo(todo: TodoItem) {
        viewModelScope.launch {
            GlobalErrorHandler.runWithCatch {
                val completedAt = if (!todo.isCompleted) DateUtils.getCurrentIsoTime() else null
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
                    createdAt = DateUtils.getCurrentIsoTime()
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
                    createdAt = DateUtils.getCurrentIsoTime()
                )
                repository.addMood(mood)
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
                    createdAt = DateUtils.getCurrentIsoTime(),
                    respondedAt = null
                )
                repository.addAsk(ask)
            }
        }
    }

    fun updateAskStatus(askId: String, status: String) {
        viewModelScope.launch {
            GlobalErrorHandler.runWithCatch {
                repository.updateAskStatus(askId, status, DateUtils.getCurrentIsoTime())
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
                    createdAt = DateUtils.getCurrentIsoTime()
                )
                repository.sendInteraction(interaction)
            }
        }
    }
}
