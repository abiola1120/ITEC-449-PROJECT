package com.eventexperts.eventandticketmanagementsystem.ui.navigation

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RESET_PASSWORD = "reset_password"
    const val SUSPENDED = "suspended"

    const val HOME = "home"
    const val SEARCH = "search"
    const val EVENT_DETAILS = "event/{eventId}"
    const val BOOKING = "book/{eventId}"
    const val MY_BOOKINGS = "bookings"
    const val TICKET_DETAILS = "ticket/{ticketId}"
    const val FAVORITES = "favorites"
    const val REVIEWS = "reviews/{eventId}"

    const val ORGANIZER_DASHBOARD = "organizer"
    const val ORGANIZER_EDIT_EVENT = "organizer/edit/{eventId}"
    const val ORGANIZER_CREATE_EVENT = "organizer/edit"
    const val ORGANIZER_TICKET_TYPES = "organizer/tickets/{eventId}"
    const val CHECKIN = "checkin/{eventId}"
    const val CHECKIN_ATTENDEES = "checkin/{eventId}/attendees"

    const val ADMIN_HOME = "admin"
    const val ADMIN_USERS = "admin/users"
    const val ADMIN_USER_DETAILS = "admin/users/{uid}"
    const val ADMIN_CATALOG = "admin/catalog"
    const val ADMIN_LOGS = "admin/logs"

    const val DASHBOARD = "dashboard"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"

    fun eventDetails(eventId: String) = "event/$eventId"
    fun booking(eventId: String) = "book/$eventId"
    fun ticketDetails(ticketId: String) = "ticket/$ticketId"
    fun reviews(eventId: String) = "reviews/$eventId"
    fun editEvent(eventId: String) = "organizer/edit/$eventId"
    fun ticketTypes(eventId: String) = "organizer/tickets/$eventId"
    fun checkIn(eventId: String) = "checkin/$eventId"
    fun checkInAttendees(eventId: String) = "checkin/$eventId/attendees"
    fun userDetails(uid: String) = "admin/users/$uid"
}
