package com.ourspace.app.data.model

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val partnerId: String? = null,
    val coupleId: String? = null,
    val createdAt: String = "",
    val isDiscoverable: Boolean = false,
    val hasCompletedSetup: Boolean = false,
    val birthday: String? = null,
    val anniversary: String? = null,
    val foodPreferences: String? = null,
    val favoriteColors: String? = null,
    val favoriteSongs: String? = null,
    val aestheticNote: String? = null,
    val mood: String? = null,
    val wellnessGoal: String? = null,
    val themePreference: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val partnerCode: String? = null,
    val gender: String? = null,
    val nickname: String? = null,
    val statusText: String? = null,
    val avatarUrl: String? = null,
    val themeColor: String? = null // Hex color for PFP ring
)
