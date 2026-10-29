package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.TicketType
import com.eventexperts.eventandticketmanagementsystem.util.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TicketTypeRepository(
    private val firestore: FirebaseFirestore
) {
    fun observeTicketTypesForEvent(eventId: String): Flow<List<TicketType>> = callbackFlow {
        val listener = firestore.collection("ticketTypes")
            .whereEqualTo("eventId", eventId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val types = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(TicketType::class.java)
                } ?: emptyList()
                trySend(types)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getTicketTypesForEvent(eventId: String): List<TicketType> {
        return try {
            val snapshot = firestore.collection("ticketTypes")
                .whereEqualTo("eventId", eventId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(TicketType::class.java) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveTicketType(
        existingTypeId: String?,
        eventId: String,
        name: String,
        priceCents: Int,
        quantityLimit: Int,
        active: Boolean = true
    ): Result<TicketType> {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return Result.Error("Ticket type name cannot be blank")
        if (priceCents < 0) return Result.Error("Price cannot be negative")
        if (quantityLimit < 1) return Result.Error("Quantity limit must be at least 1")

        return try {
            // Fetch event to validate total capacity
            val eventDoc = firestore.collection("events").document(eventId).get().await()
            if (!eventDoc.exists()) return Result.Error("Event not found")
            val event = eventDoc.toObject(Event::class.java)!!

            val existingTypes = getTicketTypesForEvent(eventId)
            val currentType = existingTypes.find { it.typeId == existingTypeId }

            // Rules when editing tickets that already have sales
            if (currentType != null && currentType.soldCount > 0) {
                if (priceCents != currentType.price) {
                    return Result.Error("Cannot change price of a ticket type after tickets have already been sold")
                }
                if (quantityLimit < currentType.soldCount) {
                    return Result.Error("Limit cannot be set lower than sold tickets count (${currentType.soldCount})")
                }
            }

            // Calculate total quantity limit across all ticket types for this event
            val otherTypesLimit = existingTypes.filter { it.typeId != existingTypeId && it.active }.sumOf { it.quantityLimit }
            val newTotalLimit = otherTypesLimit + quantityLimit

            if (newTotalLimit > event.capacity) {
                return Result.Error("Sum of ticket type limits ($newTotalLimit) exceeds event total capacity (${event.capacity})")
            }

            val docRef = if (existingTypeId.isNullOrBlank()) {
                firestore.collection("ticketTypes").document()
            } else {
                firestore.collection("ticketTypes").document(existingTypeId)
            }

            val ticketType = TicketType(
                typeId = docRef.id,
                eventId = eventId,
                name = cleanName,
                price = priceCents,
                quantityLimit = quantityLimit,
                soldCount = currentType?.soldCount ?: 0,
                active = active
            )

            docRef.set(ticketType).await()
            Result.Success(ticketType)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save ticket type")
        }
    }

    suspend fun toggleTicketTypeActive(typeId: String, active: Boolean): Result<Unit> {
        return try {
            firestore.collection("ticketTypes").document(typeId).update("active", active).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to toggle ticket type status")
        }
    }
}
