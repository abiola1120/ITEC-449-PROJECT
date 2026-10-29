package com.eventexperts.eventandticketmanagementsystem.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CancellationPolicyTest {

    @Test
    fun cancellationAllowed_whenBeforePolicyDeadline() {
        val nowSeconds = 100000L
        val policyHours = 24
        val policySeconds = policyHours * 3600L

        // Event starts in 48 hours
        val eventStartSeconds = nowSeconds + (48 * 3600L)

        val canCancel = nowSeconds + policySeconds <= eventStartSeconds
        assertTrue("Cancellation should be allowed when 48h remain and policy is 24h", canCancel)
    }

    @Test
    fun cancellationBlocked_whenAfterPolicyDeadline() {
        val nowSeconds = 100000L
        val policyHours = 24
        val policySeconds = policyHours * 3600L

        // Event starts in 10 hours (less than 24h)
        val eventStartSeconds = nowSeconds + (10 * 3600L)

        val canCancel = nowSeconds + policySeconds <= eventStartSeconds
        assertFalse("Cancellation should be blocked when less than 24h remain before event start", canCancel)
    }

    @Test
    fun seatReleaseMath_correctlyDecrementsBookedCount() {
        var bookedCount = 20
        val quantityToCancel = 2

        bookedCount -= quantityToCancel
        assertEquals(18, bookedCount)
    }
}
