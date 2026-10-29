package com.eventexperts.eventandticketmanagementsystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Review(
    @DocumentId
    val reviewId: String = "", // doc ID: {eventId}_{userId}
    val eventId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 5, // 1 to 5
    val comment: String? = null,
    val createdAt: Timestamp? = null
)
