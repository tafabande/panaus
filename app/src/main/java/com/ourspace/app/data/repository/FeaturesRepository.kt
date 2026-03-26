package com.ourspace.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.ourspace.app.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class FeaturesRepository {
    private val db = FirebaseFirestore.getInstance()

    // --- Notes ---
    fun observeNotes(coupleId: String): Flow<List<Note>> = callbackFlow {
        val listener = db.collection("couples").document(coupleId).collection("notes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    trySend(it.toObjects(Note::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveNote(coupleId: String, note: Note) {
        val ref = if (note.id.isEmpty()) {
            db.collection("couples").document(coupleId).collection("notes").document()
        } else {
            db.collection("couples").document(coupleId).collection("notes").document(note.id)
        }
        val finalNote = if (note.id.isEmpty()) note.copy(id = ref.id) else note
        ref.set(finalNote, SetOptions.merge()).await()
    }

    suspend fun deleteNote(coupleId: String, noteId: String) {
        db.collection("couples").document(coupleId).collection("notes").document(noteId).delete().await()
    }

    // --- Mood & Wellness ---
    fun observePartnerMood(coupleId: String, partnerId: String) = callbackFlow {
        val listener = db.collection("moods")
            .whereEqualTo("coupleId", coupleId)
            .whereEqualTo("userId", partnerId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documents?.firstOrNull()?.let {
                    trySend(it.toObject(Mood::class.java))
                } ?: trySend(null)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateMood(mood: Mood) {
        db.collection("moods").document(mood.userId)
            .set(mood, SetOptions.merge()).await()
    }

    // --- Todos ---
    fun observeTodos(coupleId: String): Flow<List<TodoItem>> = callbackFlow {
        val listener = db.collection("todos")
            .whereEqualTo("coupleId", coupleId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
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

    suspend fun addTodo(todo: TodoItem) = withContext(Dispatchers.IO) {
        val ref = db.collection("todos").document()
        val finalTodo = todo.copy().apply { id = ref.id }
        ref.set(finalTodo, SetOptions.merge()).await()
    }

    suspend fun toggleTodo(id: String, isCompleted: Boolean, completedAt: Long?) = withContext(Dispatchers.IO) {
        db.collection("todos").document(id).update(
            "isCompleted", isCompleted,
            "completedAt", completedAt
        ).await()
    }

    suspend fun deleteTodo(id: String) = withContext(Dispatchers.IO) {
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

    suspend fun addEvent(event: CalendarEvent) = withContext(Dispatchers.IO) {
        val ref = db.collection("events").document()
        val finalEvent = event.copy().apply { id = ref.id }
        ref.set(finalEvent, SetOptions.merge()).await()
    }

    // --- Moods ---
    fun observeMoods(coupleId: String): Flow<List<Mood>> = callbackFlow {
        val listener = db.collection("moods")
            .whereEqualTo("coupleId", coupleId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
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

    suspend fun addMood(mood: Mood) = withContext(Dispatchers.IO) {
        val ref = db.collection("moods").document()
        val finalMood = mood.copy().apply { id = ref.id }
        ref.set(finalMood, SetOptions.merge()).await()
    }

    // --- Asks ---
    fun observeAsks(coupleId: String): Flow<List<Ask>> = callbackFlow {
        val listener = db.collection("asks")
            .whereEqualTo("coupleId", coupleId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
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

    suspend fun addAsk(ask: Ask) = withContext(Dispatchers.IO) {
        val ref = db.collection("asks").document()
        val finalAsk = ask.copy().apply { id = ref.id }
        ref.set(finalAsk, SetOptions.merge()).await()
    }

    suspend fun updateAskStatus(id: String, status: String, respondedAt: Long?) = withContext(Dispatchers.IO) {
        db.collection("asks").document(id).update(
            "status", status,
            "respondedAt", respondedAt
        ).await()
    }

    // --- Interactions (Pokes, Hugs) ---
    fun observeInteractions(coupleId: String): Flow<List<Interaction>> = callbackFlow {
        val listener = db.collection("interactions")
            .whereEqualTo("coupleId", coupleId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val interactions = snapshot?.documents?.mapNotNull { doc ->
                    val interaction = doc.toObject(Interaction::class.java)
                    interaction?.id = doc.id
                    interaction
                } ?: emptyList()
                trySend(interactions)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendInteraction(interaction: Interaction) = withContext(Dispatchers.IO) {
        val ref = db.collection("interactions").document()
        val finalInteraction = interaction.copy().apply { id = ref.id }
        ref.set(finalInteraction, SetOptions.merge()).await()
    }

    suspend fun markInteractionAsRead(interactionId: String) = withContext(Dispatchers.IO) {
        db.collection("interactions").document(interactionId).update("status", "read").await()
    }

    // --- Memories ---
    fun observeMemories(coupleId: String): Flow<List<Memory>> = callbackFlow {
        val listener = db.collection("couples").document(coupleId).collection("memories")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val memories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Memory::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(memories)
            }
        awaitClose { listener.remove() }
    }

    suspend fun uploadMemory(coupleId: String, memory: Memory, localUri: android.net.Uri) = withContext(Dispatchers.IO) {
        try {
            val storage = com.google.firebase.storage.FirebaseStorage.getInstance()
            val fileName = "memories/$coupleId/${System.currentTimeMillis()}.jpg"
            val fileRef = storage.reference.child(fileName)
            
            // Upload to Storage
            fileRef.putFile(localUri).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            
            // Save to Firestore
            val finalMemory = memory.copy(imageUrl = downloadUrl, status = "UPLOADED")
            db.collection("couples").document(coupleId)
                .collection("memories").document(finalMemory.id)
                .set(finalMemory, SetOptions.merge()).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun submitQuizResponse(coupleId: String, response: QuizResponse) {
        val docId = "${response.quizId}_${response.userId}"
        db.collection("couples").document(coupleId)
            .collection("quizzes").document(docId)
            .set(response, SetOptions.merge()).await()
}

    fun observeQuizResults(coupleId: String, quizId: String): Flow<GameResult?> = callbackFlow {
        val listener = db.collection("couples").document(coupleId)
            .collection("quizzes")
            .whereEqualTo("quizId", quizId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val responses = snapshot?.toObjects(QuizResponse::class.java) ?: emptyList()
                if (responses.size >= 2) {
                    // Assuming only 2 partners in a coupleId
                    val user1 = responses[0]
                    val user2 = responses[1]
                    
                    val commonQuestions = user1.answers.keys.intersect(user2.answers.keys)
                    val matches = commonQuestions.filter { qIdx ->
                        user1.answers[qIdx]?.trim()?.lowercase() == user2.answers[qIdx]?.trim()?.lowercase()
                    }
                    
                    val matchPercentage = if (commonQuestions.isNotEmpty()) {
                        (matches.size.toFloat() / commonQuestions.size.toFloat()) * 100
                    } else 0f
                    
                    trySend(GameResult(
                        quizId = quizId,
                        user1Id = user1.userId,
                        user2Id = user2.userId,
                        user1Answers = user1.answers,
                        user2Answers = user2.answers,
                        matches = matches.toList(),
                        matchPercentage = matchPercentage
                    ))
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }
}
