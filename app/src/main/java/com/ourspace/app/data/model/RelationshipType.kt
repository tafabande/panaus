package com.ourspace.app.data.model

import androidx.annotation.Keep
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

@Keep
enum class RelationshipType(val displayName: String) {
    ROMANTIC("Romantic Partner"),
    PARENT_CHILD("Parent / Child"),
    FLATMATE("Flatmate"),
    FRIEND("Friend");

    fun getIcon(): ImageVector {
        return when (this) {
            ROMANTIC -> Icons.Default.Favorite
            PARENT_CHILD -> Icons.Default.Person
            FLATMATE -> Icons.Default.Home
            FRIEND -> Icons.Default.People
        }
    }
}
