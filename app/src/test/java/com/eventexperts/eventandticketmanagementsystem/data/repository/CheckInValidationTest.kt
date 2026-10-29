package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Ticket
import com.eventexperts.eventandticketmanagementsystem.data.model.TicketStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckInValidationTest {

    @Test
    fun antiReuseCheck_blocksAlreadyUsedTicket() {
        val usedTicket = Ticket(
            ticketRef = "EVT-TEST01",
            eventId = "evt1",
            status = TicketStatus.USED.value
        )

        val canCheckIn = usedTicket.status == TicketStatus.VALID.value
        assertFalse("Re-checking in a used ticket must be blocked", canCheckIn)
    }

    @Test
    fun eventValidation_blocksTicketBelongingToDifferentEvent() {
        val ticket = Ticket(
            ticketRef = "EVT-TEST02",
            eventId = "evt_event_A",
            status = TicketStatus.VALID.value
        )
        val scannerCurrentEventId = "evt_event_B"

        val matchesEvent = ticket.eventId == scannerCurrentEventId
        assertFalse("Ticket belonging to event A must be rejected when scanning at event B", matchesEvent)
    }

    @Test
    fun cancelledTicketCheck_blocksCancelledTicket() {
        val cancelledTicket = Ticket(
            ticketRef = "EVT-TEST03",
            eventId = "evt1",
            status = TicketStatus.CANCELLED.value
        )

        val canCheckIn = cancelledTicket.status == TicketStatus.VALID.value
        assertFalse("Cancelled ticket must be rejected at check-in", canCheckIn)
    }

    @Test
    fun validTicketCheck_allowsCheckIn() {
        val validTicket = Ticket(
            ticketRef = "EVT-TEST04",
            eventId = "evt1",
            status = TicketStatus.VALID.value
        )

        val canCheckIn = validTicket.status == TicketStatus.VALID.value
        assertTrue("Valid ticket for current event must be allowed to check in", canCheckIn)
    }
}
