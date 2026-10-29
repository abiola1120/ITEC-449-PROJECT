package com.eventexperts.eventandticketmanagementsystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ActivityLog(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val role: String = "",
    val action: String = "", // "login", "logout", "event_created", etc.
    val entityType: String = "",
    val entityId: String = "",
    val details: String? = null,
    val createdAt: Timestamp? = Timestamp.now()
)
