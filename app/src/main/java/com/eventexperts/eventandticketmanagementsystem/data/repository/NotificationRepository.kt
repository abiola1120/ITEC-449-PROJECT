package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Booking
import com.eventexperts.eventandticketmanagementsystem.data.model.InAppNotification
import com.eventexperts.eventandticketmanagementsystem.util.Result
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository(
    private val firestore: FirebaseFirestore
) {
    fun observeUserNotifications(userId: String): Flow<List<InAppNotification>> = callbackFlow {
        val listener = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(InAppNotification::class.java)
                } ?: emptyList()
                trySend(notifications)
            }
        awaitClose { listener.remove() }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("notifications").document(notificationId).update("read", true).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to mark notification as read")
        }
    }

    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "read", true)
            }
            batch.commit().await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to mark all as read")
        }
    }

    suspend fun createNotification(
        userId: String,
        type: String,
        title: String,
        body: String,
        eventId: String? = null,
        bookingId: String? = null
    ): Result<Unit> {
        return try {
            val docRef = firestore.collection("notifications").document()
            val notification = InAppNotification(
                id = docRef.id,
                userId = userId,
                type = type,
                title = title,
                body = body,
                eventId = eventId,
                bookingId = bookingId,
                read = false,
                createdAt = Timestamp.now()
            )
            docRef.set(notification).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create notification")
        }
    }

    suspend fun fanOutEventNotification(
        eventId: String,
        title: String,
        body: String,
        type: String = "event_updated"
    ): Result<Int> {
        return try {
            // Find all unique ticket holders for this event
            val bookingsSnapshot = firestore.collection("bookings")
                .whereEqualTo("eventId", eventId)
                .get()
                .await()

            val ticketHolderUserIds = bookingsSnapshot.documents
                .mapNotNull { it.toObject(Booking::class.java)?.userId }
                .distinct()

            val batch = firestore.batch()
            ticketHolderUserIds.forEach { targetUserId ->
                val docRef = firestore.collection("notifications").document()
                val notification = InAppNotification(
                    id = docRef.id,
                    userId = targetUserId,
                    type = type,
                    title = title,
                    body = body,
                    eventId = eventId,
                    read = false,
                    createdAt = Timestamp.now()
                )
                batch.set(docRef, notification)
            }

            if (ticketHolderUserIds.isNotEmpty()) {
                batch.commit().await()
            }

            Result.Success(ticketHolderUserIds.size)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fan-out notifications")
        }
    }
}
