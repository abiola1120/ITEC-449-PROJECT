package com.eventexperts.eventandticketmanagementsystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class BookingStatus(val value: String) {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    FAILED("failed"),
    CANCELLED("cancelled");

    companion object {
        fun fromValue(value: String?): BookingStatus {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

data class Booking(
    @DocumentId
    val bookingId: String = "",
    val bookingRef: String = "", // BK-XXXXXXXX
    val userId: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val ticketTypeId: String = "",
    val ticketTypeName: String = "",
    val quantity: Int = 1,
    val attendeeName: String = "",
    val attendeeEmail: String = "",
    val attendeePhone: String? = null,
    val promoCode: String? = null,
    val subtotal: Int = 0, // Cents
    val feeAmount: Int = 0, // Service fee cents
    val discountAmount: Int = 0, // Cents
    val totalAmount: Int = 0, // Cents
    val currency: String = "USD",
    val status: String = BookingStatus.CONFIRMED.value,
    val paymentStatus: String = "none",
    val idempotencyKey: String = "",
    val createdAt: Timestamp? = null,
    val cancelledAt: Timestamp? = null
) {
    val bookingStatus: BookingStatus get() = BookingStatus.fromValue(status)
}
