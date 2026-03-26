package com.ourspace.app.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class TimelineEvent(
    val id: String = "",
    val name: String = "",
    val date: String = "", // ISO format: yyyy-MM-dd
    val description: String = "",
    val type: String = "MANUAL", // "MANUAL", "SYSTEM"
    val category: String = "MILESTONE",
    val coupleId: String = ""
)
