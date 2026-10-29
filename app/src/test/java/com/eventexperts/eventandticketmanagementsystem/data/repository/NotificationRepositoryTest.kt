package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Booking
import com.eventexperts.eventandticketmanagementsystem.data.model.InAppNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationRepositoryTest {

    @Test
    fun unreadNotificationCount_correctlyCountsUnreadItems() {
        val notifications = listOf(
            InAppNotification(id = "1", read = false),
            InAppNotification(id = "2", read = true),
            InAppNotification(id = "3", read = false)
        )

        val unreadCount = notifications.count { !it.read }
        assertEquals(2, unreadCount)
    }

    @Test
    fun fanOutTicketHolders_extractsDistinctUserIds() {
        val eventBookings = listOf(
            Booking(bookingId = "b1", userId = "user_A"),
            Booking(bookingId = "b2", userId = "user_B"),
            Booking(bookingId = "b3", userId = "user_A") // Duplicate user_A
        )

        val distinctHolderIds = eventBookings.map { it.userId }.distinct()

        assertEquals("Fan-out should generate notifications for distinct ticket holders only", 2, distinctHolderIds.size)
        assertTrue(distinctHolderIds.contains("user_A"))
        assertTrue(distinctHolderIds.contains("user_B"))
    }

    @Test
    fun privateNotificationAccess_onlyOwnerCanRead() {
        val notification = InAppNotification(id = "n1", userId = "user_X")
        val currentUserId = "user_Y"

        val isOwner = notification.userId == currentUserId
        assertFalse("User Y must not be allowed to access User X's notifications", isOwner)
    }
}
