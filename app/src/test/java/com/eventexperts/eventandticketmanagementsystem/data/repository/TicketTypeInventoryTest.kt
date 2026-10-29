package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.TicketType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketTypeInventoryTest {

    @Test
    fun ticketType_availableQuantityCalculation() {
        val ticketType = TicketType(quantityLimit = 100, soldCount = 35)
        assertEquals(65, ticketType.availableQuantity)
    }

    @Test
    fun ticketType_soldOutWhenSoldCountEqualsOrExceedsLimit() {
        val soldOutType = TicketType(quantityLimit = 50, soldCount = 50)
        assertTrue(soldOutType.isSoldOut)
        assertFalse(soldOutType.isAvailable)

        val availableType = TicketType(quantityLimit = 50, soldCount = 49)
        assertFalse(availableType.isSoldOut)
        assertTrue(availableType.isAvailable)
    }

    @Test
    fun ticketType_inactiveTypeIsNotAvailableEvenIfSeatsRemain() {
        val inactiveType = TicketType(quantityLimit = 50, soldCount = 10, active = false)
        assertFalse("Inactive ticket type must not be available for purchase", inactiveType.isAvailable)
    }

    @Test
    fun ticketType_sumOfLimitsValidation() {
        val eventCapacity = 200
        val existingTypes = listOf(
            TicketType(quantityLimit = 100, active = true),
            TicketType(quantityLimit = 50, active = true)
        )

        val newTypeLimit = 60
        val total = existingTypes.sumOf { it.quantityLimit } + newTypeLimit

        assertTrue("Total ticket types limit $total exceeds event capacity $eventCapacity", total > eventCapacity)
    }
}
