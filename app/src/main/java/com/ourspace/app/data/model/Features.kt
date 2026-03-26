package com.ourspace.app.data.model

data class Note(
    var id: String = "",
    val coupleId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val createdAt: String = ""
)

data class TodoItem(
    var id: String = "",
    val coupleId: String = "",
    val title: String = "",
    val assignedTo: String = "",
    val category: String = "General",
    val isCompleted: Boolean = false,
    val createdBy: String = "",
    val createdAt: String = "",
    val completedAt: String? = null
)

data class CalendarEvent(
    var id: String = "",
    val coupleId: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val category: String = "",
    val createdBy: String = "",
    val createdAt: String = ""
)

data class Mood(
    var id: String = "",
    val userId: String = "",
    val coupleId: String = "",
    val moodValue: Int = 0,
    val emoji: String = "",
    val note: String = "",
    val createdAt: String = ""
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
    val createdAt: String = "",
    val respondedAt: String? = null
)
