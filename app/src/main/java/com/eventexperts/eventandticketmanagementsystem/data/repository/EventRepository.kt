package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Category
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.EventStatus
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.data.model.Venue
import com.eventexperts.eventandticketmanagementsystem.util.Result
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EventRepository(
    private val firestore: FirebaseFirestore,
    private val activityLogRepository: ActivityLogRepository
) {
    fun observeOrganizerEvents(organizerId: String): Flow<List<Event>> = callbackFlow {
        val listener = firestore.collection("events")
            .whereEqualTo("organizerId", organizerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)
                } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    fun observePublishedEvents(): Flow<List<Event>> = callbackFlow {
        val listener = firestore.collection("events")
            .whereEqualTo("status", EventStatus.PUBLISHED.value)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)
                } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getEventById(eventId: String): Event? {
        return try {
            val doc = firestore.collection("events").document(eventId).get().await()
            if (doc.exists()) doc.toObject(Event::class.java) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveEvent(
        existingEventId: String?,
        title: String,
        description: String,
        categoryId: String,
        venueId: String,
        capacity: Int,
        basePrice: Int,
        startAt: Timestamp,
        endAt: Timestamp?,
        saleStartAt: Timestamp?,
        saleEndAt: Timestamp?,
        registrationDeadline: Timestamp?,
        cancellationPolicyHours: Int,
        status: EventStatus,
        imageUrls: List<String>,
        videoUrl: String = "",
        user: User
    ): Result<Event> {
        val cleanTitle = title.trim()
        val cleanDescription = description.trim()

        if (cleanTitle.isBlank()) return Result.Error("Event title cannot be blank")
        if (cleanDescription.isBlank()) return Result.Error("Event description cannot be blank")
        if (categoryId.isBlank()) return Result.Error("Please select a category")
        if (venueId.isBlank()) return Result.Error("Please select a venue")
        if (capacity < 1) return Result.Error("Capacity must be at least 1")

        // Fetch selected category and venue to validate capacity and get names
        val categoryDoc = firestore.collection("categories").document(categoryId).get().await()
        if (!categoryDoc.exists()) return Result.Error("Selected category does not exist")
        val category = categoryDoc.toObject(Category::class.java)!!

        val venueDoc = firestore.collection("venues").document(venueId).get().await()
        if (!venueDoc.exists()) return Result.Error("Selected venue does not exist")
        val venue = venueDoc.toObject(Venue::class.java)!!

        if (capacity > venue.capacity) {
            return Result.Error("Event capacity ($capacity) cannot exceed venue capacity (${venue.capacity})")
        }

        if (saleStartAt != null && saleEndAt != null && saleStartAt.seconds >= saleEndAt.seconds) {
            return Result.Error("Ticket sale start date must be before sale end date")
        }

        if (registrationDeadline != null && registrationDeadline.seconds > startAt.seconds) {
            return Result.Error("Registration deadline cannot be after the event start time")
        }

        return try {
            val isNew = existingEventId.isNullOrBlank()
            val eventRef = if (isNew) {
                firestore.collection("events").document()
            } else {
                firestore.collection("events").document(existingEventId!!)
            }

            val existingEvent = if (!isNew) getEventById(existingEventId!!) else null

            val event = Event(
                eventId = eventRef.id,
                title = cleanTitle,
                description = cleanDescription,
                categoryId = category.id,
                categoryName = category.name,
                organizerId = user.uid,
                organizerName = user.name.ifBlank { user.email },
                startAt = startAt,
                endAt = endAt,
                venueId = venue.id,
                venueName = venue.name,
                address = venue.address,
                city = venue.city,
                latitude = venue.latitude,
                longitude = venue.longitude,
                imageUrls = imageUrls,
                videoUrl = videoUrl,
                capacity = capacity,
                bookedCount = existingEvent?.bookedCount ?: 0,
                basePrice = basePrice,
                saleStartAt = saleStartAt,
                saleEndAt = saleEndAt,
                registrationDeadline = registrationDeadline,
                cancellationPolicyHours = cancellationPolicyHours,
                status = status.value,
                createdAt = existingEvent?.createdAt ?: Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            firestore.runTransaction { transaction ->
                transaction.set(eventRef, event)

                if (isNew) {
                    transaction.update(firestore.collection("categories").document(category.id), "eventCount", FieldValue.increment(1))
                    transaction.update(firestore.collection("venues").document(venue.id), "eventCount", FieldValue.increment(1))
                } else if (existingEvent != null) {
                    if (existingEvent.categoryId != category.id) {
                        transaction.update(firestore.collection("categories").document(existingEvent.categoryId), "eventCount", FieldValue.increment(-1))
                        transaction.update(firestore.collection("categories").document(category.id), "eventCount", FieldValue.increment(1))
                    }
                    if (existingEvent.venueId != venue.id) {
                        transaction.update(firestore.collection("venues").document(existingEvent.venueId), "eventCount", FieldValue.increment(-1))
                        transaction.update(firestore.collection("venues").document(venue.id), "eventCount", FieldValue.increment(1))
                    }
                }
            }.await()

            activityLogRepository.logAction(
                user = user,
                action = if (isNew) "event_created" else "event_updated",
                entityType = "event",
                entityId = event.eventId,
                details = "Title: ${event.title}, Status: ${event.status}"
            )

            Result.Success(event)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save event")
        }
    }

    suspend fun cancelEvent(eventId: String, user: User): Result<Unit> {
        return try {
            val event = getEventById(eventId) ?: return Result.Error("Event not found")
            if (event.organizerId != user.uid && user.role != "admin") {
                return Result.Error("Unauthorized to cancel this event")
            }

            firestore.collection("events").document(eventId).update(
                mapOf(
                    "status" to EventStatus.CANCELLED.value,
                    "updatedAt" to Timestamp.now()
                )
            ).await()

            activityLogRepository.logAction(
                user = user,
                action = "event_cancelled",
                entityType = "event",
                entityId = eventId,
                details = "Event cancelled: ${event.title}"
            )

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to cancel event")
        }
    }
}
