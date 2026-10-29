package com.eventexperts.eventandticketmanagementsystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class GalleryItem(
    @DocumentId
    val mediaId: String = "",
    val eventId: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val featured: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)
