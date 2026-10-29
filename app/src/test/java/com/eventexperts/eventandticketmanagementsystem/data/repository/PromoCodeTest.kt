package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.DiscountType
import com.eventexperts.eventandticketmanagementsystem.data.model.PromoCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromoCodeTest {

    @Test
    fun percentDiscountCalculation_correctlyDeductsPercentage() {
        val promo = PromoCode(
            code = "SAVE20",
            discountType = DiscountType.PERCENT.value,
            discountValue = 20, // 20%
            active = true
        )

        val subtotalCents = 10000 // $100.00
        val discount = promo.calculateDiscount(subtotalCents)

        assertEquals(2000, discount) // $20.00 discount
    }

    @Test
    fun fixedDiscountCalculation_correctlyDeductsFixedAmount() {
        val promo = PromoCode(
            code = "TAKE15",
            discountType = DiscountType.FIXED.value,
            discountValue = 1500, // $15.00
            active = true
        )

        val subtotalCents = 8000 // $80.00
        val discount = promo.calculateDiscount(subtotalCents)

        assertEquals(1500, discount) // $15.00 discount
    }

    @Test
    fun exhaustedPromoCode_isNotValid() {
        val exhaustedPromo = PromoCode(
            code = "LIMIT10",
            usageLimit = 10,
            timesUsed = 10,
            active = true
        )

        assertTrue(exhaustedPromo.isExhausted)
        assertFalse("Exhausted promo code must be invalid", exhaustedPromo.isValid)
    }
}
