package com.ourspace.app.ui.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ourspace.app.data.model.*
import com.ourspace.app.data.repository.FeaturesRepository
import com.ourspace.app.data.util.DateUtils
import kotlinx.coroutines.Job

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

    private var notesJob: Job? = null
    private var todosJob: Job? = null
    private var eventsJob: Job? = null
    private var moodsJob: Job? = null
    private var asksJob: Job? = null

    // Load Data based on UserProfile
    fun startObserving(userProfile: UserProfile) {
        val coupleId = userProfile.coupleId ?: return

        notesJob?.cancel()
        notesJob = viewModelScope.launch {
            repository.observeNotes(coupleId).collect { _notes.value = it }
        }

        todosJob?.cancel()
        todosJob = viewModelScope.launch {
            repository.observeTodos(coupleId).collect { _todos.value = it }
        }

        eventsJob?.cancel()
        eventsJob = viewModelScope.launch {
            repository.observeEvents(coupleId).collect { _events.value = it }
        }

        moodsJob?.cancel()
        moodsJob = viewModelScope.launch {
            repository.observeMoods(coupleId).collect { _moods.value = it }
        }

        asksJob?.cancel()
        asksJob = viewModelScope.launch {
            repository.observeAsks(coupleId).collect { _asks.value = it }
        }
    }

    fun sendNote(coupleId: String, senderId: String, receiverId: String?, content: String) {
        if (content.isBlank() || receiverId == null) return
        viewModelScope.launch {
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

    fun addTodo(coupleId: String, creatorId: String, title: String, assignedTo: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val todo = TodoItem(
                coupleId = coupleId,
                title = title.trim(),
                assignedTo = assignedTo,
                isCompleted = false,
                createdBy = creatorId,
                createdAt = DateUtils.getCurrentIsoTime(),
                completedAt = null
            )
            repository.addTodo(todo)
        }
    }

    fun toggleTodo(todo: TodoItem) {
        viewModelScope.launch {
            val completedAt = if (!todo.isCompleted) DateUtils.getCurrentIsoTime() else null
            repository.toggleTodo(todo.id, !todo.isCompleted, completedAt)
        }
    }

    fun deleteTodo(todoId: String) {
        viewModelScope.launch {
            repository.deleteTodo(todoId)
        }
    }

    fun addEvent(coupleId: String, creatorId: String, title: String, date: String, time: String, category: String) {
        if (title.isBlank() || date.isBlank()) return
        viewModelScope.launch {
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

    fun addMood(userId: String, coupleId: String, moodValue: Int, emoji: String, note: String) {
        viewModelScope.launch {
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

    fun addAsk(coupleId: String, fromUserId: String, toUserId: String, text: String, type: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
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

    fun updateAskStatus(askId: String, status: String) {
        viewModelScope.launch {
            repository.updateAskStatus(askId, status, DateUtils.getCurrentIsoTime())
        }
    }

    // Removed local getCurrentIsoTime in favor of DateUtils
}
