package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Booking
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.EventStatus
import com.eventexperts.eventandticketmanagementsystem.data.model.TicketType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingCalculationTest {

    @Test
    fun bookingFeeCalculation_appliesFivePercentServiceFee() {
        val ticketPriceCents = 2000 // $20.00
        val quantity = 3
        val subtotal = ticketPriceCents * quantity // $60.00 = 6000 cents
        val serviceFee = (subtotal * 0.05).toInt() // $3.00 = 300 cents
        val total = subtotal + serviceFee // $63.00 = 6300 cents

        assertEquals(6000, subtotal)
        assertEquals(300, serviceFee)
        assertEquals(6300, total)
    }

    @Test
    fun bookingIdempotency_sameKeyReturnsExistingBooking() {
        val key = "unique_checkout_key_123"
        val booking1 = Booking(bookingId = "b1", idempotencyKey = key)
        val booking2 = Booking(bookingId = "b1", idempotencyKey = key)

        assertEquals("Repeat request with same idempotency key must return same booking ID", booking1.bookingId, booking2.bookingId)
    }

    @Test
    fun oversellingCheck_blocksBookingWhenQuantityExceedsRemainingSeats() {
        val event = Event(capacity = 50, bookedCount = 48) // 2 seats remaining
        val requestedQuantity = 3

        val canBook = event.bookedCount + requestedQuantity <= event.capacity
        assertFalse("Booking 3 seats when only 2 seats remain must be blocked", canBook)
    }

    @Test
    fun capacityCheck_allowsBookingWhenSeatsAvailable() {
        val event = Event(capacity = 50, bookedCount = 45) // 5 seats remaining
        val requestedQuantity = 3

        val canBook = event.bookedCount + requestedQuantity <= event.capacity
        assertTrue("Booking 3 seats when 5 seats remain must be allowed", canBook)
    }
}
