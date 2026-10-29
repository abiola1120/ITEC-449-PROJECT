package com.eventexperts.eventandticketmanagementsystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class TicketType(
    @DocumentId
    val typeId: String = "",
    val eventId: String = "",
    val name: String = "", // e.g. "General Admission", "VIP", "Student"
    val price: Int = 0, // Minor units (cents)
    val quantityLimit: Int = 1,
    val soldCount: Int = 0,
    val availableFrom: Timestamp? = null,
    val availableTo: Timestamp? = null,
    val active: Boolean = true
) {
    val availableQuantity: Int get() = (quantityLimit - soldCount).coerceAtLeast(0)
    val isSoldOut: Boolean get() = soldCount >= quantityLimit
    val isAvailable: Boolean get() = active && !isSoldOut
}
