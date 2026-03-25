package com.ourspace.app.data.model

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val partnerId: String? = null,
    val coupleId: String? = null,
    val createdAt: String = "",
    val isDiscoverable: Boolean = false
)
