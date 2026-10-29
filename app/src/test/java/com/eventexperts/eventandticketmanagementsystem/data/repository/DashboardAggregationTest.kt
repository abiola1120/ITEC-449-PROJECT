package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Booking
import com.eventexperts.eventandticketmanagementsystem.data.model.BookingStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardAggregationTest {

    @Test
    fun revenueAggregation_excludesCancelledAndFailedBookings() {
        val bookings = listOf(
            Booking(bookingId = "b1", totalAmount = 5000, status = BookingStatus.CONFIRMED.value),
            Booking(bookingId = "b2", totalAmount = 3000, status = BookingStatus.CANCELLED.value), // Excluded
            Booking(bookingId = "b3", totalAmount = 2500, status = BookingStatus.FAILED.value),    // Excluded
            Booking(bookingId = "b4", totalAmount = 7000, status = BookingStatus.CONFIRMED.value)
        )

        val confirmedBookings = bookings.filter { it.status == BookingStatus.CONFIRMED.value }
        val grossRevenueCents = confirmedBookings.sumOf { it.totalAmount }

        assertEquals(2, confirmedBookings.size)
        assertEquals(12000, grossRevenueCents) // $120.00
    }

    @Test
    fun occupancyRate_calculatesCorrectPercentage() {
        val totalCapacity = 200
        val totalBooked = 150

        val rate = if (totalCapacity == 0) 0 else ((totalBooked.toDouble() / totalCapacity) * 100).toInt()
        assertEquals(75, rate)
    }
}
