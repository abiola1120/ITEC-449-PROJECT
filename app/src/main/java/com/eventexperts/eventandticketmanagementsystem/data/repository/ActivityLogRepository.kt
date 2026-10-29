package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.ActivityLog
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ActivityLogRepository(
    private val firestore: FirebaseFirestore
) {
    fun observeActivityLogs(): Flow<List<ActivityLog>> = callbackFlow {
        val listener = firestore.collection("activityLogs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ActivityLog::class.java)
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun logAction(
        user: User?,
        action: String,
        entityType: String = "user",
        entityId: String = user?.uid ?: "",
        details: String? = null
    ) {
        try {
            val log = ActivityLog(
                userId = user?.uid ?: "",
                userName = user?.name.takeIf { !it.isNullOrBlank() } ?: user?.email ?: "Anonymous",
                role = user?.role ?: "guest",
                action = action,
                entityType = entityType,
                entityId = entityId,
                details = details,
                createdAt = Timestamp.now()
            )
            firestore.collection("activityLogs").add(log).await()
        } catch (_: Exception) {
            // Ignore failure to prevent breaking main user flow if log write fails
        }
    }
}
