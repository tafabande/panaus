package com.ourspace.app.ui.theme

import androidx.compose.ui.graphics.Color

object AuraColors {
    /**
     * Maps a color name string (e.g., from a Firestore profile) to a Compose Color object.
     * Default to Gray if the color is unrecognized.
     */
    fun fromName(name: String?): Color {
        return when (name?.trim()?.lowercase()) {
            "teal" -> Color(0xFF008080)
            "red" -> Color(0xFFFF4842)
            "blue" -> Color(0xFF1890FF)
            "green" -> Color(0xFF54D62C)
            "purple" -> Color(0xFF7635DC)
            "yellow" -> Color(0xFFFFC107)
            "pink" -> Color(0xFFFD36A4)
            "black" -> Color(0xFF212B36)
            "rose" -> Color(0xFFE91E63)
            "lavender" -> Color(0xFF9575CD)
            "amber" -> Color(0xFFFFB300)
            "sky" -> Color(0xFF03A9F4)
            "emerald" -> Color(0xFF2E7D32)
            "coral" -> Color(0xFFFF7043)
            "slate" -> Color(0xFF455A64)
            "gold" -> Color(0xFFFFD700)
            "violet" -> Color(0xFF8B00FF)
            else -> Color.Gray
        }
    }

    val palette = listOf(
        "Teal", "Red", "Blue", "Green", "Purple", "Yellow", "Pink", "Black",
        "Rose", "Lavender", "Amber", "Sky", "Emerald", "Coral", "Slate", "Gold", "Violet"
    )
}
