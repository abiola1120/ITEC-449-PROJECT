package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Booking
import com.eventexperts.eventandticketmanagementsystem.data.model.BookingStatus
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.Ticket
import com.eventexperts.eventandticketmanagementsystem.data.model.TicketStatus
import com.eventexperts.eventandticketmanagementsystem.data.model.TicketType
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.util.Result
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BookingRepository(
    private val firestore: FirebaseFirestore,
    private val activityLogRepository: ActivityLogRepository
) {
    fun observeUserBookings(userId: String): Flow<List<Booking>> = callbackFlow {
        val listener = firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)
                } ?: emptyList()
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    fun observeBookingTickets(bookingId: String): Flow<List<Ticket>> = callbackFlow {
        val listener = firestore.collection("tickets")
            .whereEqualTo("bookingId", bookingId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tickets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Ticket::class.java)
                } ?: emptyList()
                trySend(tickets)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getBookingById(bookingId: String): Booking? {
        return try {
            val doc = firestore.collection("bookings").document(bookingId).get().await()
            if (doc.exists()) doc.toObject(Booking::class.java) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun createBooking(
        eventId: String,
        ticketTypeId: String,
        quantity: Int,
        attendeeName: String,
        attendeeEmail: String,
        attendeePhone: String?,
        idempotencyKey: String,
        user: User
    ): Result<Booking> {
        if (quantity < 1 || quantity > 10) {
            return Result.Error("Booking quantity must be between 1 and 10 tickets")
        }
        if (attendeeName.isBlank() || attendeeEmail.isBlank()) {
            return Result.Error("Please enter attendee name and email")
        }

        try {
            // Check idempotency: If booking with this key exists, return it directly
            val existingSnapshot = firestore.collection("bookings")
                .whereEqualTo("idempotencyKey", idempotencyKey)
                .get()
                .await()

            if (!existingSnapshot.isEmpty) {
                val existingBooking = existingSnapshot.documents.first().toObject(Booking::class.java)
                if (existingBooking != null) {
                    return Result.Success(existingBooking)
                }
            }

            val bookingRefStr = "BK-" + UUID.randomUUID().toString().replace("-", "").take(8).uppercase()
            val bookingDocRef = firestore.collection("bookings").document()

            // Generate unique ticket references
            val ticketRefs = List(quantity) {
                "EVT-" + UUID.randomUUID().toString().replace("-", "").take(10).uppercase()
            }

            val eventRef = firestore.collection("events").document(eventId)
            val ticketTypeRef = firestore.collection("ticketTypes").document(ticketTypeId)

            var createdBooking: Booking? = null

            firestore.runTransaction { transaction ->
                val eventSnap = transaction.get(eventRef)
                if (!eventSnap.exists()) throw IllegalStateException("Event not found")
                val event = eventSnap.toObject(Event::class.java)!!

                if (!event.isPublished) throw IllegalStateException("Event is not published for sales")
                if (event.bookedCount + quantity > event.capacity) {
                    throw IllegalStateException("Event is sold out or does not have enough remaining seats ($quantity requested, ${event.capacity - event.bookedCount} available)")
                }

                val typeSnap = transaction.get(ticketTypeRef)
                if (!typeSnap.exists()) throw IllegalStateException("Ticket type not found")
                val ticketType = typeSnap.toObject(TicketType::class.java)!!

                if (!ticketType.active) throw IllegalStateException("Selected ticket type is inactive")
                if (ticketType.soldCount + quantity > ticketType.quantityLimit) {
                    throw IllegalStateException("Selected ticket tier is sold out (${ticketType.quantityLimit - ticketType.soldCount} available)")
                }

                val subtotal = ticketType.price * quantity
                val serviceFee = (subtotal * 0.05).toInt() // 5% fee rounded
                val totalAmount = subtotal + serviceFee

                val booking = Booking(
                    bookingId = bookingDocRef.id,
                    bookingRef = bookingRefStr,
                    userId = user.uid,
                    eventId = eventId,
                    eventTitle = event.title,
                    ticketTypeId = ticketTypeId,
                    ticketTypeName = ticketType.name,
                    quantity = quantity,
                    attendeeName = attendeeName.trim(),
                    attendeeEmail = attendeeEmail.trim(),
                    attendeePhone = attendeePhone?.trim(),
                    subtotal = subtotal,
                    feeAmount = serviceFee,
                    discountAmount = 0,
                    totalAmount = totalAmount,
                    status = BookingStatus.CONFIRMED.value,
                    idempotencyKey = idempotencyKey,
                    createdAt = Timestamp.now()
                )

                createdBooking = booking

                // Update event and ticket type counters atomically
                transaction.update(eventRef, "bookedCount", FieldValue.increment(quantity.toLong()))
                transaction.update(ticketTypeRef, "soldCount", FieldValue.increment(quantity.toLong()))

                // Write booking document
                transaction.set(bookingDocRef, booking)

                // Write ticket documents
                ticketRefs.forEach { tRef ->
                    val ticketDocRef = firestore.collection("tickets").document(tRef)
                    val ticket = Ticket(
                        ticketRef = tRef,
                        bookingId = bookingDocRef.id,
                        eventId = eventId,
                        userId = user.uid,
                        attendeeName = attendeeName.trim(),
                        ticketTypeId = ticketTypeId,
                        ticketTypeName = ticketType.name,
                        status = TicketStatus.VALID.value
                    )
                    transaction.set(ticketDocRef, ticket)
                }

                // Write in-app notification
                val notificationRef = firestore.collection("notifications").document()
                val notification = mapOf(
                    "id" to notificationRef.id,
                    "userId" to user.uid,
                    "type" to "booking_confirmed",
                    "title" to "Booking Confirmed!",
                    "body" to "You confirmed $quantity ticket(s) for '${event.title}'. Reference: $bookingRefStr",
                    "eventId" to eventId,
                    "bookingId" to bookingDocRef.id,
                    "read" to false,
                    "createdAt" to Timestamp.now()
                )
                transaction.set(notificationRef, notification)
            }.await()

            activityLogRepository.logAction(
                user = user,
                action = "booking_created",
                entityType = "booking",
                entityId = bookingDocRef.id,
                details = "Ref: $bookingRefStr, Quantity: $quantity"
            )

            return Result.Success(createdBooking!!)
        } catch (e: Exception) {
            return Result.Error(e.message ?: "Booking transaction failed")
        }
    }

    suspend fun cancelBooking(bookingId: String, user: User): Result<Unit> {
        return try {
            val booking = getBookingById(bookingId) ?: return Result.Error("Booking not found")
            if (booking.userId != user.uid && user.role != "admin") {
                return Result.Error("Unauthorized to cancel this booking")
            }
            if (booking.status == BookingStatus.CANCELLED.value) {
                return Result.Error("Booking is already cancelled")
            }

            val eventDoc = firestore.collection("events").document(booking.eventId).get().await()
            val event = eventDoc.toObject(Event::class.java)

            val nowSeconds = Timestamp.now().seconds
            val eventStartSeconds = event?.startAt?.seconds ?: (nowSeconds + 100000)
            val policySeconds = (event?.cancellationPolicyHours ?: 24) * 3600L

            if (nowSeconds + policySeconds > eventStartSeconds) {
                return Result.Error("Cancellation deadline passed (${event?.cancellationPolicyHours ?: 24}h before event start)")
            }

            val eventRef = firestore.collection("events").document(booking.eventId)
            val ticketTypeRef = firestore.collection("ticketTypes").document(booking.ticketTypeId)
            val bookingRef = firestore.collection("bookings").document(bookingId)

            val ticketsSnapshot = firestore.collection("tickets")
                .whereEqualTo("bookingId", bookingId)
                .get()
                .await()

            firestore.runTransaction { transaction ->
                // Mark booking cancelled
                transaction.update(bookingRef, mapOf(
                    "status" to BookingStatus.CANCELLED.value,
                    "cancelledAt" to Timestamp.now()
                ))

                // Mark tickets cancelled
                ticketsSnapshot.documents.forEach { doc ->
                    transaction.update(doc.reference, "status", TicketStatus.CANCELLED.value)
                }

                // Release capacity
                transaction.update(eventRef, "bookedCount", FieldValue.increment(-booking.quantity.toLong()))
                transaction.update(ticketTypeRef, "soldCount", FieldValue.increment(-booking.quantity.toLong()))

                // Write in-app notification
                val notificationRef = firestore.collection("notifications").document()
                val notification = mapOf(
                    "id" to notificationRef.id,
                    "userId" to user.uid,
                    "type" to "booking_cancelled",
                    "title" to "Booking Cancelled",
                    "body" to "Your booking (${booking.bookingRef}) for '${booking.eventTitle}' was cancelled and seats released.",
                    "eventId" to booking.eventId,
                    "bookingId" to bookingId,
                    "read" to false,
                    "createdAt" to Timestamp.now()
                )
                transaction.set(notificationRef, notification)
            }.await()

            activityLogRepository.logAction(
                user = user,
                action = "booking_cancelled",
                entityType = "booking",
                entityId = bookingId,
                details = "Ref: ${booking.bookingRef}, Released: ${booking.quantity} seats"
            )

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to cancel booking")
        }
    }
}
