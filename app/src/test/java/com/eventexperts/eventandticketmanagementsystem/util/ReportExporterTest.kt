package com.eventexperts.eventandticketmanagementsystem.util

import com.eventexperts.eventandticketmanagementsystem.data.model.Booking
import com.eventexperts.eventandticketmanagementsystem.data.model.Ticket
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportExporterTest {

    @Test
    fun attendanceCsvGeneration_formatsHeaderAndRowsCorrectly() {
        val tickets = listOf(
            Ticket(ticketRef = "EVT-101", attendeeName = "Jane Doe", ticketTypeName = "VIP", status = "used")
        )

        val csv = ReportExporter.generateAttendanceCsv(tickets)
        assertTrue(csv.contains("TicketRef,AttendeeName,TicketType,Status,CheckedInAt"))
        assertTrue(csv.contains("EVT-101,\"Jane Doe\",\"VIP\",used"))
    }

    @Test
    fun salesCsvGeneration_formatsHeaderAndRowsCorrectly() {
        val bookings = listOf(
            Booking(bookingRef = "BK-202", attendeeName = "John Smith", eventTitle = "Music Fest", quantity = 2, totalAmount = 5000, status = "confirmed")
        )

        val csv = ReportExporter.generateSalesCsv(bookings)
        assertTrue(csv.contains("BookingRef,AttendeeName,EventTitle,Quantity,TotalAmount,Status,CreatedAt"))
        assertTrue(csv.contains("BK-202,\"John Smith\",\"Music Fest\",2"))
    }
}
