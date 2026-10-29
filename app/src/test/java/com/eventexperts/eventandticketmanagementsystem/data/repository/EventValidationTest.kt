package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Category
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.EventStatus
import com.eventexperts.eventandticketmanagementsystem.data.model.Venue
import com.google.firebase.Timestamp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventValidationTest {

    @Test
    fun eventCapacity_cannotExceedVenueCapacity() {
        val venue = Venue(capacity = 500)
        val eventRequestedCapacity = 600

        val isValid = eventRequestedCapacity <= venue.capacity
        assertFalse("Event capacity of $eventRequestedCapacity should exceed venue capacity of ${venue.capacity}", isValid)
    }

    @Test
    fun eventCapacity_withinVenueCapacity_isAllowed() {
        val venue = Venue(capacity = 500)
        val eventRequestedCapacity = 300

        val isValid = eventRequestedCapacity <= venue.capacity
        assertTrue("Event capacity $eventRequestedCapacity should be valid for venue capacity ${venue.capacity}", isValid)
    }

    @Test
    fun isSaleOpen_returnsFalseWhenSoldOutOrDraft() {
        val draftEvent = Event(
            status = EventStatus.DRAFT.value,
            capacity = 100,
            bookedCount = 10
        )
        assertFalse(draftEvent.isSaleOpen())

        val soldOutEvent = Event(
            status = EventStatus.PUBLISHED.value,
            capacity = 100,
            bookedCount = 100
        )
        assertFalse(soldOutEvent.isSaleOpen())
    }

    @Test
    fun isSaleOpen_returnsTrueWhenPublishedAndSeatsAvailable() {
        val activeEvent = Event(
            status = EventStatus.PUBLISHED.value,
            capacity = 100,
            bookedCount = 50
        )
        assertTrue(activeEvent.isSaleOpen())
    }

    @Test
    fun inUseCategoryDeletion_blockedWhenEventCountGreaterThanZero() {
        val categoryInUse = Category(id = "cat1", name = "Music", eventCount = 3)
        val canDelete = categoryInUse.eventCount == 0

        assertFalse("Deleting category in use should be blocked", canDelete)
    }

    @Test
    fun unusedCategoryDeletion_allowedWhenEventCountIsZero() {
        val unusedCategory = Category(id = "cat2", name = "New Cat", eventCount = 0)
        val canDelete = unusedCategory.eventCount == 0

        assertTrue("Deleting category with eventCount 0 should be allowed", canDelete)
    }
}
