package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.data.model.UserRole
import com.eventexperts.eventandticketmanagementsystem.data.model.UserStatus
import com.eventexperts.eventandticketmanagementsystem.util.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore,
    private val activityLogRepository: ActivityLogRepository
) {
    fun observeAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getUserById(uid: String): User? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) doc.toObject(User::class.java) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun updateUserRole(targetUid: String, newRole: UserRole, adminUser: User): Result<Unit> {
        if (adminUser.role != UserRole.ADMIN.value) {
            return Result.Error("Only Administrators can modify user roles")
        }

        return try {
            val targetDoc = firestore.collection("users").document(targetUid).get().await()
            if (!targetDoc.exists()) return Result.Error("User not found")
            val targetUser = targetDoc.toObject(User::class.java)!!

            // Last-admin protection check
            if (targetUser.role == UserRole.ADMIN.value && newRole != UserRole.ADMIN) {
                val adminSnapshot = firestore.collection("users")
                    .whereEqualTo("role", UserRole.ADMIN.value)
                    .whereEqualTo("status", UserStatus.ACTIVE.value)
                    .get()
                    .await()

                val activeAdmins = adminSnapshot.documents.mapNotNull { it.toObject(User::class.java) }
                if (activeAdmins.size <= 1) {
                    return Result.Error("Cannot demote the last remaining active Administrator")
                }
            }

            firestore.collection("users").document(targetUid).update("role", newRole.value).await()

            activityLogRepository.logAction(
                user = adminUser,
                action = "user_role_changed",
                entityType = "user",
                entityId = targetUid,
                details = "Changed role of ${targetUser.email} to ${newRole.value}"
            )

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update user role")
        }
    }

    suspend fun updateUserStatus(targetUid: String, newStatus: UserStatus, adminUser: User): Result<Unit> {
        if (adminUser.role != UserRole.ADMIN.value) {
            return Result.Error("Only Administrators can modify account status")
        }

        return try {
            val targetDoc = firestore.collection("users").document(targetUid).get().await()
            if (!targetDoc.exists()) return Result.Error("User not found")
            val targetUser = targetDoc.toObject(User::class.java)!!

            // Last-admin protection check
            if (targetUser.role == UserRole.ADMIN.value && newStatus == UserStatus.SUSPENDED) {
                val adminSnapshot = firestore.collection("users")
                    .whereEqualTo("role", UserRole.ADMIN.value)
                    .whereEqualTo("status", UserStatus.ACTIVE.value)
                    .get()
                    .await()

                val activeAdmins = adminSnapshot.documents.mapNotNull { it.toObject(User::class.java) }
                if (activeAdmins.size <= 1) {
                    return Result.Error("Cannot suspend the last remaining active Administrator")
                }
            }

            firestore.collection("users").document(targetUid).update("status", newStatus.value).await()

            activityLogRepository.logAction(
                user = adminUser,
                action = if (newStatus == UserStatus.SUSPENDED) "user_suspended" else "user_activated",
                entityType = "user",
                entityId = targetUid,
                details = "Updated status of ${targetUser.email} to ${newStatus.value}"
            )

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update account status")
        }
    }
}
