package com.ourspace.app.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val username: String? = null,
    val email: String = "",

    val partnerId: String? = null,
    val coupleId: String? = null,
    val createdAt: String = "",
    
    @get:PropertyName("discoverable")
    @set:PropertyName("discoverable")
    var discoverable: Boolean = false,

    @get:PropertyName("isSetupComplete")
    @set:PropertyName("isSetupComplete")
    var isSetupComplete: Boolean = false,
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
    val firstDateLocation: String? = null,
    val firstKissDate: String? = null,
    val howWeMet: String? = null,
    @get:PropertyName("profileTheme") @set:PropertyName("profileTheme")
    var profileTheme: String? = null, // Theme color name (e.g., "Teal")
    val themeColor: String? = null, // Hex color override
    val relationshipType: String? = null, // "ROMANTIC", "PARENT_CHILD", "FLATMATE", "FRIEND"
    val statusEmoji: String? = null,
    val statusNote: String? = null
)
