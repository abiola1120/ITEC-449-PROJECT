package com.eventexperts.eventandticketmanagementsystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class TicketStatus(val value: String) {
    VALID("valid"),
    USED("used"),
    CANCELLED("cancelled"),
    REFUNDED("refunded");

    companion object {
        fun fromValue(value: String?): TicketStatus {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: VALID
        }
    }
}

data class Ticket(
    @DocumentId
    val ticketRef: String = "", // EVT-XXXXXXXXXX
    val bookingId: String = "",
    val eventId: String = "",
    val userId: String = "",
    val attendeeName: String = "",
    val ticketTypeId: String = "",
    val ticketTypeName: String = "",
    val status: String = TicketStatus.VALID.value,
    val checkedInAt: Timestamp? = null,
    val checkedInBy: String? = null
) {
    val ticketStatus: TicketStatus get() = TicketStatus.fromValue(status)
}
