package com.eventexperts.eventandticketmanagementsystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Favorite(
    @DocumentId
    val eventId: String = "",
    val savedAt: Timestamp? = null,
    val reminderEnabled: Boolean = true
)
