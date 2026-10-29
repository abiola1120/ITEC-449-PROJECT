package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Faq
import com.eventexperts.eventandticketmanagementsystem.data.model.GalleryItem
import com.eventexperts.eventandticketmanagementsystem.util.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SupportGalleryRepository(
    private val firestore: FirebaseFirestore
) {
    fun observeFaqs(): Flow<List<Faq>> = callbackFlow {
        val listener = firestore.collection("faqs")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val faqs = snapshot?.documents?.mapNotNull { it.toObject(Faq::class.java) } ?: emptyList()
                trySend(faqs.sortedBy { it.orderIndex })
            }
        awaitClose { listener.remove() }
    }

    fun observeGallery(): Flow<List<GalleryItem>> = callbackFlow {
        val listener = firestore.collection("gallery")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { it.toObject(GalleryItem::class.java) } ?: emptyList()
                trySend(items.sortedByDescending { it.createdAt })
            }
        awaitClose { listener.remove() }
    }

    suspend fun submitInquiry(name: String, email: String, message: String): Result<Unit> {
        if (name.isBlank() || email.isBlank() || message.isBlank()) {
            return Result.Error("Please fill in all fields")
        }
        return try {
            val inquiry = mapOf(
                "name" to name.trim(),
                "email" to email.trim(),
                "message" to message.trim(),
                "status" to "pending",
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            firestore.collection("inquiries").add(inquiry).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to submit inquiry")
        }
    }
}
