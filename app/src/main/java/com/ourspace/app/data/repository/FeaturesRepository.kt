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
import android.util.Log

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

    suspend fun saveNote(coupleId: String, note: Note) = withContext(Dispatchers.IO) {
        val ref = if (note.id.isEmpty()) {
            db.collection("couples").document(coupleId).collection("notes").document()
        } else {
            db.collection("couples").document(coupleId).collection("notes").document(note.id)
        }
        val finalNote = if (note.id.isEmpty()) note.copy(id = ref.id) else note
        try {
            ref.set(finalNote, SetOptions.merge()).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteNote(coupleId: String, noteId: String) = withContext(Dispatchers.IO) {
        db.collection("couples").document(coupleId).collection("notes").document(noteId).delete().await()
    }

    // --- Mood & Wellness ---
    fun observePartnerMood(coupleId: String, partnerId: String) = callbackFlow {
        val listener = db.collection("users").document(partnerId)
            .addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(UserProfile::class.java)
                trySend(user?.mood)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateMood(coupleId: String, mood: Mood) = withContext(Dispatchers.IO) {
        // 1. Update historical record in couple's space
        if (coupleId.isNotEmpty()) {
            val historyRef = db.collection("couples").document(coupleId).collection("mood_history").document()
            val finalMood = mood.copy(id = historyRef.id)
            historyRef.set(finalMood, SetOptions.merge()).await()
        }
            
        // 2. Update the user's current mood in their profile for dashboard visibility
        db.collection("users").document(mood.userId)
            .update("mood", "${mood.emoji} ${mood.note}".trim()).await()
    }

    // --- Todos ---
    fun observeTodos(coupleId: String): Flow<List<TodoItem>> = callbackFlow {
        val listener = db.collection("couples").document(coupleId).collection("todos")
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

    suspend fun addTodo(coupleId: String, todo: TodoItem) = withContext(Dispatchers.IO) {
        val ref = db.collection("couples").document(coupleId).collection("todos").document()
        val finalTodo = todo.copy(id = ref.id)
        ref.set(finalTodo, SetOptions.merge()).await()
    }

    suspend fun toggleTodo(coupleId: String, id: String, isCompleted: Boolean, completedAt: Long?) = withContext(Dispatchers.IO) {
        db.collection("couples").document(coupleId).collection("todos").document(id).update(
            "isCompleted", isCompleted,
            "completedAt", completedAt
        ).await()
    }

    suspend fun deleteTodo(coupleId: String, id: String) = withContext(Dispatchers.IO) {
        db.collection("couples").document(coupleId).collection("todos").document(id).delete().await()
    }

    // --- Calendar Events ---
    fun observeEvents(coupleId: String): Flow<List<CalendarEvent>> = callbackFlow {
        val listener = db.collection("couples").document(coupleId).collection("events")
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

    suspend fun addEvent(coupleId: String, event: CalendarEvent) = withContext(Dispatchers.IO) {
        try {
            val ref = db.collection("couples").document(coupleId).collection("events").document()
            val finalEvent = event.copy(id = ref.id)
            ref.set(finalEvent, SetOptions.merge()).await()
        } catch (e: Exception) {
            throw e
        }
    }

    // --- Mood History ---
    fun observeMoods(coupleId: String): Flow<List<Mood>> = callbackFlow {
        val listener = db.collection("couples").document(coupleId).collection("mood_history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
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

    // Deprecated in favor of updateMood which handles both
    suspend fun addMood(coupleId: String, mood: Mood) = updateMood(coupleId, mood)

    // --- Asks ---
    fun observeAsks(coupleId: String): Flow<List<Ask>> = callbackFlow {
        val listener = db.collection("couples").document(coupleId).collection("asks")
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

    suspend fun addAsk(coupleId: String, ask: Ask) = withContext(Dispatchers.IO) {
        val ref = db.collection("couples").document(coupleId).collection("asks").document()
        val finalAsk = ask.copy(id = ref.id)
        ref.set(finalAsk, SetOptions.merge()).await()
    }

    suspend fun updateAskStatus(coupleId: String, id: String, status: String, respondedAt: Long?) = withContext(Dispatchers.IO) {
        db.collection("couples").document(coupleId).collection("asks").document(id).update(
            "status", status,
            "respondedAt", respondedAt
        ).await()
    }

    // --- Interactions (Pokes, Hugs) ---
    fun observeInteractions(coupleId: String): Flow<List<Interaction>> = callbackFlow {
        val listener = db.collection("couples").document(coupleId).collection("interactions")
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

    suspend fun sendInteraction(coupleId: String, interaction: Interaction) = withContext(Dispatchers.IO) {
        val ref = db.collection("couples").document(coupleId).collection("interactions").document()
        val finalInteraction = interaction.copy(id = ref.id)
        ref.set(finalInteraction, SetOptions.merge()).await()
    }

    suspend fun markInteractionAsRead(coupleId: String, interactionId: String) = withContext(Dispatchers.IO) {
        db.collection("couples").document(coupleId).collection("interactions").document(interactionId).update("status", "read").await()
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

    private val mediaRepository = MediaRepository()

    suspend fun uploadMemory(context: android.content.Context, coupleId: String, memory: Memory, localUri: android.net.Uri) = withContext(Dispatchers.IO) {
        try {
            val folder = if (coupleId.isNotBlank()) "memories/$coupleId" else "memories/${memory.userId}"
            val fileName = "${System.currentTimeMillis()}.jpg"
            
            // Upload using MediaRepository
            val uploadResult = mediaRepository.uploadMedia(context, folder, fileName, localUri)
            if (uploadResult.isFailure) {
                throw uploadResult.exceptionOrNull()!!
            }
            
            val downloadUrl = uploadResult.getOrNull()!!
            
            // Save to Firestore
            val finalMemory = memory.copy(imageUrl = downloadUrl, status = "UPLOADED")
            val targetCoupleId = if (coupleId.isNotBlank()) coupleId else "unknown"
            
            db.collection("couples").document(targetCoupleId)
                .collection("memories").document(finalMemory.id)
                .set(finalMemory, SetOptions.merge()).await()
        } catch (e: Exception) {
            throw e
        }
    }


    suspend fun deleteRelationshipEvent(coupleId: String, eventId: String) = withContext(Dispatchers.IO) {
        db.collection("couples").document(coupleId).collection("relationship_history").document(eventId).delete().await()
    }

    suspend fun submitQuizResponse(coupleId: String, response: QuizResponse) = withContext(Dispatchers.IO) {
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

    // --- Relationship Events ---
    fun observeRelationshipEvents(coupleId: String): Flow<List<RelationshipEvent>> = callbackFlow {
        if (coupleId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("couples").document(coupleId).collection("relationship_history")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FeaturesRepository", "Error observing relationship events", e)
                    close(e)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RelationshipEvent::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveRelationshipEvent(context: android.content.Context, coupleId: String, event: RelationshipEvent, localUri: android.net.Uri? = null) = withContext(Dispatchers.IO) {
        var finalEvent = event
        
        if (localUri != null) {
            val folder = "milestones/$coupleId"
            val fileName = "milestone_${System.currentTimeMillis()}.jpg"
            val uploadResult = mediaRepository.uploadMedia(context, folder, fileName, localUri)
            if (uploadResult.isSuccess) {
                finalEvent = finalEvent.copy(imageUrl = uploadResult.getOrNull()!!)
            } else {
                Log.e("FeaturesRepository", "Milestone image upload failed", uploadResult.exceptionOrNull())
            }
        }

        val docRef = if (finalEvent.id.isEmpty()) {
            db.collection("couples").document(coupleId).collection("relationship_history").document()
        } else {
            db.collection("couples").document(coupleId).collection("relationship_history").document(finalEvent.id)
        }
        finalEvent = finalEvent.copy(id = docRef.id)
        docRef.set(finalEvent, SetOptions.merge()).await()
    }
}
