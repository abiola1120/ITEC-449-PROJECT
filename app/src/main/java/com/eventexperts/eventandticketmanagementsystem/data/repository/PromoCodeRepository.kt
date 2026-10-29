package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.DiscountType
import com.eventexperts.eventandticketmanagementsystem.data.model.PromoCode
import com.eventexperts.eventandticketmanagementsystem.util.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PromoCodeRepository(
    private val firestore: FirebaseFirestore
) {
    suspend fun validateAndCalculatePromo(
        codeStr: String,
        eventId: String,
        subtotalCents: Int
    ): Result<Pair<PromoCode, Int>> {
        val cleanCode = codeStr.trim().uppercase()
        if (cleanCode.isBlank()) return Result.Error("Please enter a promo code")

        return try {
            val snapshot = firestore.collection("promoCodes")
                .whereEqualTo("code", cleanCode)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.Error("Invalid promo code")
            }

            val promo = snapshot.documents.first().toObject(PromoCode::class.java)
                ?: return Result.Error("Promo code error")

            if (!promo.active) return Result.Error("Promo code is no longer active")
            if (promo.isExpired) return Result.Error("Promo code has expired")
            if (promo.isExhausted) return Result.Error("Promo code usage limit has been reached")
            if (promo.eventId != "ALL" && promo.eventId != eventId) {
                return Result.Error("Promo code is not applicable to this event")
            }

            val discountCents = promo.calculateDiscount(subtotalCents)
            Result.Success(Pair(promo, discountCents))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to validate promo code")
        }
    }

    suspend fun createPromoCode(
        code: String,
        eventId: String,
        discountType: DiscountType,
        discountValue: Int,
        usageLimit: Int
    ): Result<PromoCode> {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) return Result.Error("Code cannot be blank")
        if (discountValue <= 0) return Result.Error("Discount value must be greater than 0")

        return try {
            val docRef = firestore.collection("promoCodes").document()
            val promo = PromoCode(
                codeId = docRef.id,
                code = cleanCode,
                eventId = eventId,
                discountType = discountType.value,
                discountValue = discountValue,
                usageLimit = usageLimit,
                timesUsed = 0,
                active = true
            )
            docRef.set(promo).await()
            Result.Success(promo)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create promo code")
        }
    }
}
