package com.eventexperts.eventandticketmanagementsystem.util

import com.eventexperts.eventandticketmanagementsystem.data.model.Booking
import com.eventexperts.eventandticketmanagementsystem.data.model.Ticket

object ReportExporter {

    fun generateAttendanceCsv(tickets: List<Ticket>): String {
        val sb = StringBuilder()
        sb.append("TicketRef,AttendeeName,TicketType,Status,CheckedInAt\n")
        tickets.forEach { ticket ->
            sb.append("${ticket.ticketRef},\"${ticket.attendeeName}\",\"${ticket.ticketTypeName}\",${ticket.status},${ticket.checkedInAt?.toDate() ?: "N/A"}\n")
        }
        return sb.toString()
    }

    fun generateSalesCsv(bookings: List<Booking>): String {
        val sb = StringBuilder()
        sb.append("BookingRef,AttendeeName,EventTitle,Quantity,TotalAmount,Status,CreatedAt\n")
        bookings.forEach { b ->
            val totalDollars = String.format("%.2f", b.totalAmount / 100.0)
            sb.append("${b.bookingRef},\"${b.attendeeName}\",\"${b.eventTitle}\",${b.quantity},$totalDollars,${b.status},${b.createdAt?.toDate() ?: "N/A"}\n")
        }
        return sb.toString()
    }
}
