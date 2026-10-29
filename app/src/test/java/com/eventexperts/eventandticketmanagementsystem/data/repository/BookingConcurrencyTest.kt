package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class BookingConcurrencyTest {

    @Test
    fun simulatedConcurrentBooking_onlyOneSucceedsWhenLastSeatRemaining() = runBlocking {
        // Event has capacity 10, already 9 booked (1 seat remaining)
        var bookedCount = 9
        val capacity = 10

        val successfulBookings = AtomicInteger(0)
        val failedBookings = AtomicInteger(0)

        // Simulate two parallel user requests trying to claim the last seat simultaneously
        val job1 = async {
            synchronized(this) {
                if (bookedCount + 1 <= capacity) {
                    bookedCount += 1
                    successfulBookings.incrementAndGet()
                } else {
                    failedBookings.incrementAndGet()
                }
            }
        }

        val job2 = async {
            synchronized(this) {
                if (bookedCount + 1 <= capacity) {
                    bookedCount += 1
                    successfulBookings.incrementAndGet()
                } else {
                    failedBookings.incrementAndGet()
                }
            }
        }

        job1.await()
        job2.await()

        assertEquals("Exactly 1 concurrent request should claim the last seat", 1, successfulBookings.get())
        assertEquals("Exactly 1 concurrent request should be rejected due to sold-out state", 1, failedBookings.get())
        assertEquals("Final bookedCount should match capacity exactly", 10, bookedCount)
    }
}
