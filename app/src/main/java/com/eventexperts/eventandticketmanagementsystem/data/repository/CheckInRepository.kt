package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.Ticket
import com.eventexperts.eventandticketmanagementsystem.data.model.TicketStatus
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.util.Result
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

sealed class CheckInResult {
    data class Success(val ticket: Ticket, val attendeeName: String, val ticketTypeName: String) : CheckInResult()
    data class AlreadyUsed(val ticketRef: String, val checkedInAt: Timestamp?) : CheckInResult()
    object WrongEvent : CheckInResult()
    object CancelledTicket : CheckInResult()
    object NotFound : CheckInResult()
    data class Error(val message: String) : CheckInResult()
}

data class CheckInStats(
    val totalTickets: Int = 0,
    val checkedInCount: Int = 0
) {
    val checkInPercentage: Int get() = if (totalTickets == 0) 0 else ((checkedInCount.toDouble() / totalTickets) * 100).toInt()
}

class CheckInRepository(
    private val firestore: FirebaseFirestore,
    private val activityLogRepository: ActivityLogRepository
) {
    fun observeEventTickets(eventId: String): Flow<List<Ticket>> = callbackFlow {
        val listener = firestore.collection("tickets")
            .whereEqualTo("eventId", eventId)
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

    suspend fun checkInTicket(ticketRefInput: String, eventId: String, user: User): CheckInResult {
        val cleanRef = ticketRefInput.trim().uppercase()
        if (cleanRef.isBlank()) return CheckInResult.Error("Please enter a ticket reference")

        return try {
            val ticketDocRef = firestore.collection("tickets").document(cleanRef)
            var result: CheckInResult = CheckInResult.NotFound

            firestore.runTransaction { transaction ->
                val ticketSnap = transaction.get(ticketDocRef)
                if (!ticketSnap.exists()) {
                    result = CheckInResult.NotFound
                    return@runTransaction
                }

                val ticket = ticketSnap.toObject(Ticket::class.java)!!

                if (ticket.eventId != eventId) {
                    result = CheckInResult.WrongEvent
                    return@runTransaction
                }

                when (ticket.status) {
                    TicketStatus.USED.value -> {
                        result = CheckInResult.AlreadyUsed(ticket.ticketRef, ticket.checkedInAt)
                    }
                    TicketStatus.CANCELLED.value, TicketStatus.REFUNDED.value -> {
                        result = CheckInResult.CancelledTicket
                    }
                    TicketStatus.VALID.value -> {
                        val now = Timestamp.now()
                        transaction.update(ticketDocRef, mapOf(
                            "status" to TicketStatus.USED.value,
                            "checkedInAt" to now,
                            "checkedInBy" to user.uid
                        ))

                        // Write attendance proof doc for event reviews
                        val attendanceDocRef = firestore.collection("attendance").document("${eventId}_${ticket.userId}")
                        val attendanceRecord = mapOf(
                            "eventId" to eventId,
                            "userId" to ticket.userId,
                            "checkedInAt" to now
                        )
                        transaction.set(attendanceDocRef, attendanceRecord)

                        result = CheckInResult.Success(
                            ticket = ticket.copy(status = TicketStatus.USED.value, checkedInAt = now),
                            attendeeName = ticket.attendeeName,
                            ticketTypeName = ticket.ticketTypeName
                        )
                    }
                }
            }.await()

            if (result is CheckInResult.Success) {
                activityLogRepository.logAction(
                    user = user,
                    action = "ticket_checked_in",
                    entityType = "ticket",
                    entityId = cleanRef,
                    details = "Checked in ticket $cleanRef for event $eventId"
                )
            }

            result
        } catch (e: Exception) {
            CheckInResult.Error(e.message ?: "Check-in transaction failed")
        }
    }
}
