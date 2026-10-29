package com.eventexperts.eventandticketmanagementsystem.ui.home

import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeStateTest {

    @Test
    fun homeUiState_defaultValuesAreEmpty() {
        val state = HomeUiState()
        assertTrue(state.publishedEvents.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun homeUiState_updatesEventsCorrectly() {
        val events = listOf(Event(eventId = "1", title = "Tech Conference"))
        val state = HomeUiState(publishedEvents = events, isLoading = false)
        assertEquals(1, state.publishedEvents.size)
        assertEquals("Tech Conference", state.publishedEvents[0].title)
    }
}
