package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.Favorite
import com.eventexperts.eventandticketmanagementsystem.data.model.Review
import com.eventexperts.eventandticketmanagementsystem.util.Result
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReviewAndFavoritesRepository(
    private val firestore: FirebaseFirestore
) {
    fun observeUserFavorites(userId: String): Flow<List<Favorite>> = callbackFlow {
        val listener = firestore.collection("users").document(userId).collection("favorites")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val favorites = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Favorite::class.java)
                } ?: emptyList()
                trySend(favorites)
            }
        awaitClose { listener.remove() }
    }

    suspend fun toggleFavorite(userId: String, eventId: String): Result<Boolean> {
        return try {
            val favRef = firestore.collection("users").document(userId).collection("favorites").document(eventId)
            val doc = favRef.get().await()

            if (doc.exists()) {
                favRef.delete().await()
                Result.Success(false) // Removed
            } else {
                val fav = Favorite(eventId = eventId, savedAt = Timestamp.now(), reminderEnabled = true)
                favRef.set(fav).await()
                Result.Success(true) // Saved
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update favorites")
        }
    }

    suspend fun submitReview(
        userId: String,
        userName: String,
        eventId: String,
        rating: Int,
        comment: String?
    ): Result<Unit> {
        if (rating !in 1..5) return Result.Error("Rating must be between 1 and 5 stars")

        return try {
            // Check attendance verification doc: Only attendees who attended the event may write a review!
            val attendanceDoc = firestore.collection("attendance").document("${eventId}_${userId}").get().await()
            if (!attendanceDoc.exists()) {
                return Result.Error("Only attendees with checked-in tickets may write a review for this event.")
            }

            val reviewRef = firestore.collection("reviews").document("${eventId}_${userId}")
            val review = Review(
                reviewId = "${eventId}_${userId}",
                eventId = eventId,
                userId = userId,
                userName = userName,
                rating = rating,
                comment = comment?.trim(),
                createdAt = Timestamp.now()
            )

            // Update review and re-calculate event rating average atomically
            val eventRef = firestore.collection("events").document(eventId)

            firestore.runTransaction { transaction ->
                transaction.set(reviewRef, review)

                val reviewsSnap = firestore.collection("reviews")
                    .whereEqualTo("eventId", eventId)
                    .get()

                // Calculate updated average
                val eventSnap = transaction.get(eventRef)
                if (eventSnap.exists()) {
                    val currentAvg = eventSnap.getDouble("ratingAverage") ?: 0.0
                    val currentCount = eventSnap.getLong("ratingCount")?.toInt() ?: 0
                    val newCount = currentCount + 1
                    val newAvg = ((currentAvg * currentCount) + rating) / newCount

                    transaction.update(eventRef, mapOf(
                        "ratingAverage" to newAvg,
                        "ratingCount" to newCount
                    ))
                }
            }.await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to submit review")
        }
    }

    fun observeEventReviews(eventId: String): Flow<List<Review>> = callbackFlow {
        val listener = firestore.collection("reviews")
            .whereEqualTo("eventId", eventId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Review::class.java)
                } ?: emptyList()
                trySend(reviews)
            }
        awaitClose { listener.remove() }
    }
}
