package com.eventexperts.eventandticketmanagementsystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class InAppNotification(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val type: String = "account", // "booking_confirmed", "booking_cancelled", "event_reminder", "event_updated", "event_cancelled"
    val title: String = "",
    val body: String = "",
    val eventId: String? = null,
    val bookingId: String? = null,
    val read: Boolean = false,
    val createdAt: Timestamp? = null
)
