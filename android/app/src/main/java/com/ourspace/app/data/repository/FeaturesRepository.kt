package com.ourspace.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ourspace.app.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FeaturesRepository {
    private val db = FirebaseFirestore.getInstance()

    // --- Notes ---
    fun observeNotes(coupleId: String): Flow<List<Note>> = callbackFlow {
        val listener = db.collection("notes")
            .whereEqualTo("coupleId", coupleId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Note::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(notes)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendNote(note: Note) {
        db.collection("notes").add(note).await()
    }

    // --- Todos ---
    fun observeTodos(coupleId: String): Flow<List<TodoItem>> = callbackFlow {
        val listener = db.collection("todos")
            .whereEqualTo("coupleId", coupleId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val todos = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(TodoItem::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(todos)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addTodo(todo: TodoItem) {
        db.collection("todos").add(todo).await()
    }

    suspend fun toggleTodo(id: String, isCompleted: Boolean, completedAt: String?) {
        db.collection("todos").document(id).update(
            "isCompleted", isCompleted,
            "completedAt", completedAt
        ).await()
    }

    suspend fun deleteTodo(id: String) {
        db.collection("todos").document(id).delete().await()
    }

    // --- Calendar Events ---
    fun observeEvents(coupleId: String): Flow<List<CalendarEvent>> = callbackFlow {
        val listener = db.collection("events")
            .whereEqualTo("coupleId", coupleId)
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CalendarEvent::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addEvent(event: CalendarEvent) {
        db.collection("events").add(event).await()
    }

    // --- Moods ---
    fun observeMoods(coupleId: String): Flow<List<Mood>> = callbackFlow {
        val listener = db.collection("moods")
            .whereEqualTo("coupleId", coupleId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val moods = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Mood::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(moods)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addMood(mood: Mood) {
        db.collection("moods").add(mood).await()
    }

    // --- Asks ---
    fun observeAsks(coupleId: String): Flow<List<Ask>> = callbackFlow {
        val listener = db.collection("asks")
            .whereEqualTo("coupleId", coupleId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val asks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Ask::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(asks)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addAsk(ask: Ask) {
        db.collection("asks").add(ask).await()
    }

    suspend fun updateAskStatus(id: String, status: String, respondedAt: String) {
        db.collection("asks").document(id).update(
            "status", status,
            "respondedAt", respondedAt
        ).await()
    }
}
