package com.eventexperts.eventandticketmanagementsystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Venue(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val capacity: Int = 100,
    val eventCount: Int = 0,
    val createdAt: Timestamp? = null
)
