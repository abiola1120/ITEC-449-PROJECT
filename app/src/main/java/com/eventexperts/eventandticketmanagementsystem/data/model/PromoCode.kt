package com.eventexperts.eventandticketmanagementsystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class DiscountType(val value: String) {
    PERCENT("percent"),
    FIXED("fixed");

    companion object {
        fun fromValue(value: String?): DiscountType {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: PERCENT
        }
    }
}

data class PromoCode(
    @DocumentId
    val codeId: String = "",
    val code: String = "", // Uppercase, e.g. "SAVE20"
    val eventId: String = "ALL",
    val discountType: String = DiscountType.PERCENT.value,
    val discountValue: Int = 0, // e.g. 20 for 20% or 500 for $5.00
    val usageLimit: Int = 100,
    val timesUsed: Int = 0,
    val expiresAt: Timestamp? = null,
    val active: Boolean = true
) {
    val isExpired: Boolean get() = expiresAt != null && Timestamp.now().seconds > expiresAt.seconds
    val isExhausted: Boolean get() = timesUsed >= usageLimit
    val isValid: Boolean get() = active && !isExpired && !isExhausted

    fun calculateDiscount(subtotalCents: Int): Int {
        if (!isValid || subtotalCents <= 0) return 0
        return when (DiscountType.fromValue(discountType)) {
            DiscountType.PERCENT -> ((subtotalCents * (discountValue.coerceIn(0, 100) / 100.0))).toInt()
            DiscountType.FIXED -> discountValue.coerceAtMost(subtotalCents)
        }
    }
}
