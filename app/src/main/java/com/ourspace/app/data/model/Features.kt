package com.ourspace.app.data.model

data class Note(
    var id: String = "",
    val coupleId: String = "",
    val senderId: String = "",
    val title: String = "",
    val content: String = "",
    val color: String = "#FFFFFF",
    val timestamp: Long = System.currentTimeMillis()
)

data class TodoItem(
    var id: String = "",
    val coupleId: String = "",
    val title: String = "",
    val assignedTo: String = "",
    val category: String = "General",
    val isCompleted: Boolean = false,
    val createdBy: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

data class CalendarEvent(
    var id: String = "",
    val coupleId: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val category: String = "",
    val createdBy: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Mood(
    var id: String = "",
    val userId: String = "",
    val coupleId: String = "",
    val moodValue: Int = 0,
    val emoji: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Ask(
    var id: String = "",
    val coupleId: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val requestText: String = "",
    val requestType: String = "",
    val status: String = "",
    val responseText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null
)

data class Interaction(
    var id: String = "",
    val coupleId: String = "",
    val senderId: String = "",
    val type: String = "poke", // poke, hug, kiss
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "unread" // unread, read
)

data class Memory(
    var id: String = "",
    val userId: String = "",
    val coupleId: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "UPLOADED" // UPLOADED, SENDING, FAILED
)

data class RelationshipEvent(
    var id: String = "",
    val coupleId: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "", // yyyy-MM-dd
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "Milestone" // Milestone, Anniversary, Journey, Memory
)

data class QuizResponse(
    var id: String = "",
    val quizId: String = "", // e.g., "OUR_STORY"
    val userId: String = "",
    val answers: Map<Int, String> = emptyMap(), // questionIndex -> answer
    val timestamp: Long = System.currentTimeMillis()
)

data class GameResult(
    val quizId: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val user1Answers: Map<Int, String> = emptyMap(),
    val user2Answers: Map<Int, String> = emptyMap(),
    val matches: List<Int> = emptyList(), // questionIndices where answers match
    val matchPercentage: Float = 0f
)

object MoodConstants {
    val allMoods = listOf(
        "Happy", "Sad", "Energetic", "Anxious", "Loved", 
        "Exhausted", "Cozy", "Grumpy", "Creative", "Calm", 
        "Excited", "Stressed", "Lonely"
    )
}
