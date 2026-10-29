package com.eventexperts.eventandticketmanagementsystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class UserRole(val value: String) {
    ATTENDEE("attendee"),
    ORGANIZER("organizer"),
    ADMIN("admin");

    companion object {
        fun fromValue(value: String?): UserRole {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: ATTENDEE
        }
    }
}

enum class UserStatus(val value: String) {
    ACTIVE("active"),
    SUSPENDED("suspended");

    companion object {
        fun fromValue(value: String?): UserStatus {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: ACTIVE
        }
    }
}

data class User(
    @DocumentId
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String? = null,
    val role: String = UserRole.ATTENDEE.value,
    val status: String = UserStatus.ACTIVE.value,
    val createdAt: Timestamp? = null,
    val photoUrl: String? = null
) {
    val userRole: UserRole get() = UserRole.fromValue(role)
    val userStatus: UserStatus get() = UserStatus.fromValue(status)
    val isSuspended: Boolean get() = userStatus == UserStatus.SUSPENDED
}

data class UsernameDoc(
    val uid: String = "",
    val email: String = ""
)
