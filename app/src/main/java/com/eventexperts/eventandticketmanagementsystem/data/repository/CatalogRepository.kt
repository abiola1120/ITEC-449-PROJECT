package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Category
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.data.model.Venue
import com.eventexperts.eventandticketmanagementsystem.util.Result
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CatalogRepository(
    private val firestore: FirebaseFirestore,
    private val activityLogRepository: ActivityLogRepository
) {
    fun observeCategories(): Flow<List<Category>> = callbackFlow {
        val listener = firestore.collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val categories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Category::class.java)
                } ?: emptyList()
                trySend(categories)
            }
        awaitClose { listener.remove() }
    }

    fun observeVenues(): Flow<List<Venue>> = callbackFlow {
        val listener = firestore.collection("venues")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val venues = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Venue::class.java)
                } ?: emptyList()
                trySend(venues)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addCategory(name: String, user: User?): Result<Category> {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return Result.Error("Category name cannot be blank")

        return try {
            val docRef = firestore.collection("categories").document()
            val category = Category(
                id = docRef.id,
                name = cleanName,
                eventCount = 0,
                createdAt = Timestamp.now()
            )
            docRef.set(category).await()
            activityLogRepository.logAction(user, "category_created", "category", docRef.id, cleanName)
            Result.Success(category)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add category")
        }
    }

    suspend fun editCategory(id: String, newName: String, user: User?): Result<Unit> {
        val cleanName = newName.trim()
        if (cleanName.isBlank()) return Result.Error("Category name cannot be blank")

        return try {
            firestore.collection("categories").document(id).update("name", cleanName).await()
            activityLogRepository.logAction(user, "category_updated", "category", id, cleanName)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to edit category")
        }
    }

    suspend fun deleteCategory(id: String, user: User?): Result<Unit> {
        return try {
            val doc = firestore.collection("categories").document(id).get().await()
            if (!doc.exists()) return Result.Error("Category not found")

            val category = doc.toObject(Category::class.java)
            if (category != null && category.eventCount > 0) {
                return Result.Error("Cannot delete category '${category.name}' because ${category.eventCount} event(s) are using it.")
            }

            firestore.collection("categories").document(id).delete().await()
            activityLogRepository.logAction(user, "category_deleted", "category", id, category?.name)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete category")
        }
    }

    suspend fun addVenue(
        name: String,
        address: String,
        city: String,
        capacity: Int,
        latitude: Double? = null,
        longitude: Double? = null,
        user: User?
    ): Result<Venue> {
        val cleanName = name.trim()
        val cleanAddress = address.trim()
        val cleanCity = city.trim()

        if (cleanName.isBlank()) return Result.Error("Venue name cannot be blank")
        if (cleanAddress.isBlank()) return Result.Error("Venue address cannot be blank")
        if (cleanCity.isBlank()) return Result.Error("Venue city cannot be blank")
        if (capacity < 1) return Result.Error("Venue capacity must be at least 1")

        return try {
            val docRef = firestore.collection("venues").document()
            val venue = Venue(
                id = docRef.id,
                name = cleanName,
                address = cleanAddress,
                city = cleanCity,
                latitude = latitude,
                longitude = longitude,
                capacity = capacity,
                eventCount = 0,
                createdAt = Timestamp.now()
            )
            docRef.set(venue).await()
            activityLogRepository.logAction(user, "venue_created", "venue", docRef.id, cleanName)
            Result.Success(venue)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add venue")
        }
    }

    suspend fun editVenue(
        id: String,
        name: String,
        address: String,
        city: String,
        capacity: Int,
        latitude: Double? = null,
        longitude: Double? = null,
        user: User?
    ): Result<Unit> {
        val cleanName = name.trim()
        val cleanAddress = address.trim()
        val cleanCity = city.trim()

        if (cleanName.isBlank()) return Result.Error("Venue name cannot be blank")
        if (cleanAddress.isBlank()) return Result.Error("Venue address cannot be blank")
        if (cleanCity.isBlank()) return Result.Error("Venue city cannot be blank")
        if (capacity < 1) return Result.Error("Venue capacity must be at least 1")

        return try {
            val updates = mapOf(
                "name" to cleanName,
                "address" to cleanAddress,
                "city" to cleanCity,
                "capacity" to capacity,
                "latitude" to latitude,
                "longitude" to longitude
            )
            firestore.collection("venues").document(id).update(updates).await()
            activityLogRepository.logAction(user, "venue_updated", "venue", id, cleanName)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to edit venue")
        }
    }

    suspend fun deleteVenue(id: String, user: User?): Result<Unit> {
        return try {
            val doc = firestore.collection("venues").document(id).get().await()
            if (!doc.exists()) return Result.Error("Venue not found")

            val venue = doc.toObject(Venue::class.java)
            if (venue != null && venue.eventCount > 0) {
                return Result.Error("Cannot delete venue '${venue.name}' because ${venue.eventCount} event(s) are scheduled there.")
            }

            firestore.collection("venues").document(id).delete().await()
            activityLogRepository.logAction(user, "venue_deleted", "venue", id, venue?.name)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete venue")
        }
    }
}
