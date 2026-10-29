package com.eventexperts.eventandticketmanagementsystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class EventStatus(val value: String) {
    DRAFT("draft"),
    PUBLISHED("published"),
    CANCELLED("cancelled"),
    SUSPENDED("suspended");

    companion object {
        fun fromValue(value: String?): EventStatus {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: DRAFT
        }
    }
}

data class Event(
    @DocumentId
    val eventId: String = "",
    val title: String = "",
    val description: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val startAt: Timestamp? = null,
    val endAt: Timestamp? = null,
    val venueId: String = "",
    val venueName: String = "",
    val address: String = "",
    val city: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val geohash: String? = null,
    val imageUrls: List<String> = emptyList(),
    val videoUrl: String = "",
    val capacity: Int = 1,
    val bookedCount: Int = 0,
    val basePrice: Int = 0, // In minor units (cents)
    val currency: String = "USD",
    val saleStartAt: Timestamp? = null,
    val saleEndAt: Timestamp? = null,
    val registrationDeadline: Timestamp? = null,
    val cancellationPolicyHours: Int = 24,
    val status: String = EventStatus.DRAFT.value,
    val ratingAverage: Double = 0.0,
    val ratingCount: Int = 0,
    val isSample: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val eventStatus: EventStatus get() = EventStatus.fromValue(status)
    val seatsRemaining: Int get() = (capacity - bookedCount).coerceAtLeast(0)
    val isSoldOut: Boolean get() = bookedCount >= capacity
    val isPublished: Boolean get() = eventStatus == EventStatus.PUBLISHED
    val isCancelled: Boolean get() = eventStatus == EventStatus.CANCELLED

    fun isSaleOpen(now: Timestamp = Timestamp.now()): Boolean {
        if (!isPublished || isSoldOut) return false
        val saleStart = saleStartAt
        val saleEnd = saleEndAt
        val deadline = registrationDeadline

        if (saleStart != null && now.seconds < saleStart.seconds) return false
        if (saleEnd != null && now.seconds > saleEnd.seconds) return false
        if (deadline != null && now.seconds > deadline.seconds) return false
        return true
    }
}
