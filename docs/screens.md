# Event Management App — Screens & Navigation Specification

This document details all required screens, routes, state fields, and navigation rules for the Event and Ticket Management System based on Section 1A of `CLAUDE.md`.

---

## 1. Authentication & Onboarding

### 1.1 Welcome Screen
- **Route:** `welcome`
- **Role:** Unauthenticated users
- **UiState:** `WelcomeUiState(isLoading: Boolean)`
- **Key Interactions:**
  - Navigates to `login` ("Sign In")
  - Navigates to `register` ("Create Account")
- **Navigation:**
  - `welcome` → `login`
  - `welcome` → `register`

### 1.2 Login Screen
- **Route:** `login`
- **Role:** Unauthenticated users
- **UiState:** `LoginUiState(emailOrUsernameInput: String, passwordInput: String, isLoading: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - Login with email or username + password
  - Toggle password visibility
  - Navigate to `reset_password`
  - Navigate to `register`
- **Navigation:**
  - `login` → `home` (or `organizer`, `admin` depending on role)
  - `login` → `suspended` (if user account is suspended)
  - `login` → `reset_password`
  - `login` → `register`

### 1.3 Register Screen
- **Route:** `register`
- **Role:** Unauthenticated users
- **UiState:** `RegisterUiState(nameInput: String, usernameInput: String, emailInput: String, passwordInput: String, roleInput: UserRole, isLoading: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - Fill registration details (Name, unique Username, Email, Password ≥ 8 chars)
  - Select Role (`attendee` or `organizer`)
  - Register account (creates Firestore `users/{uid}` and `usernames/{username}`)
- **Navigation:**
  - `register` → `home` / `organizer`
  - `register` → `login`

### 1.4 Reset Password Screen
- **Route:** `reset_password`
- **Role:** Unauthenticated users
- **UiState:** `ResetPasswordUiState(emailInput: String, isLoading: Boolean, isSuccess: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - Enter email to receive reset link
- **Navigation:**
  - `reset_password` → `login`

### 1.5 Account Suspended Screen
- **Route:** `suspended`
- **Role:** Suspended users (`status == "suspended"`)
- **UiState:** `SuspendedUiState(userName: String, email: String)`
- **Key Interactions:**
  - Sign out
- **Navigation:**
  - `suspended` → `welcome` / `login`

---

## 2. Event Discovery & Attendee Experience

### 2.1 Home Screen
- **Route:** `home`
- **Role:** All authenticated users
- **UiState:** `HomeUiState(featuredEvents: List<Event>, upcomingEvents: List<Event>, recommendedEvents: List<Event>, isLoading: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - View published upcoming events
  - Filter by quick tags
  - Toggle favorite status on event card
  - Tap event card to open details
- **Navigation:**
  - `home` → `event/{eventId}`
  - `home` → `search`
  - `home` → `notifications`

### 2.2 Search & Filter Screen
- **Route:** `search`
- **Role:** All authenticated users
- **UiState:** `SearchUiState(query: String, selectedCategory: Category?, selectedDateRange: DateRange?, selectedCity: String?, minPrice: Int?, maxPrice: Int?, searchResults: List<Event>, categories: List<Category>, isLoading: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - Live search by title or description
  - Filter by date, category, location/city, price range
  - Clear filters
- **Navigation:**
  - `search` → `event/{eventId}`

### 2.3 Event Details Screen
- **Route:** `event/{eventId}`
- **Role:** All authenticated users
- **UiState:** `EventDetailsUiState(event: Event?, ticketTypes: List<TicketType>, organizer: User?, userReview: Review?, isFavorite: Boolean, isLoading: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - View event info, schedule, venue, capacity, ticket prices
  - Favorite/unfavorite
  - Tap "Book Now" (or "Join Waitlist" if full/sold out)
  - "Add to Calendar" intent
  - "Share" intent
- **Navigation:**
  - `event/{eventId}` → `book/{eventId}`
  - `event/{eventId}` → `reviews/{eventId}`

### 2.4 Ticket Booking & Checkout Screen
- **Route:** `book/{eventId}`
- **Role:** Attendee
- **UiState:** `BookingUiState(event: Event?, selectedTicketType: TicketType?, quantity: Int, promoCodeInput: String, appliedDiscount: Int, subtotal: Int, serviceFee: Int, totalAmount: Int, attendeeName: String, attendeeEmail: String, attendeePhone: String, isSubmitting: Boolean, bookingResult: BookingResult?, errorMessage: String?)`
- **Key Interactions:**
  - Select ticket type and quantity
  - Apply promo code
  - Review fee breakdown (Subtotal, 5% Service Fee, Discount, Total)
  - Enter attendee contact details
  - Confirm booking (idempotent submission using transaction)
- **Navigation:**
  - `book/{eventId}` → `ticket/{ticketId}` (on success)
  - `book/{eventId}` → `bookings`

### 2.5 My Bookings & History Screen
- **Route:** `bookings`
- **Role:** Attendee
- **UiState:** `MyBookingsUiState(selectedTab: BookingTab, upcomingBookings: List<BookingWithTickets>, pastBookings: List<BookingWithTickets>, cancelledBookings: List<BookingWithTickets>, isLoading: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - Switch tabs (Upcoming, Previous, Cancelled)
  - View booking reference and status
  - Open e-ticket
  - Cancel booking (subject to event cancellation policy window)
- **Navigation:**
  - `bookings` → `ticket/{ticketId}`

### 2.6 Digital Ticket Screen
- **Route:** `ticket/{ticketId}`
- **Role:** Attendee
- **UiState:** `TicketUiState(ticket: Ticket?, event: Event?, isLoading: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - Display unique ticket reference (e.g., `EVT-XXXXXXXXXX`)
  - Display QR code
  - Show status (Valid, Used, Cancelled, Refunded)
  - Save ticket image / share
- **Navigation:**
  - `ticket/{ticketId}` → `bookings`

### 2.7 Favorites & Reminders Screen
- **Route:** `favorites`
- **Role:** Attendee
- **UiState:** `FavoritesUiState(favoriteEvents: List<Event>, isLoading: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - Toggle reminder local notification (24h before)
  - Remove from favorites
- **Navigation:**
  - `favorites` → `event/{eventId}`

---

## 3. Organizer Screens

### 3.1 Organizer Dashboard / My Events
- **Route:** `organizer`
- **Role:** Organizer
- **UiState:** `OrganizerDashboardUiState(myEvents: List<Event>, totalSales: Int, totalTicketsSold: Int, isLoading: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - List created events (Draft, Published, Cancelled)
  - Filter events by status
  - Navigate to create event
  - Navigate to event check-in / attendee list
- **Navigation:**
  - `organizer` → `organizer/edit/{eventId}`
  - `organizer` → `organizer/tickets/{eventId}`
  - `organizer` → `checkin/{eventId}`

### 3.2 Create / Edit Event Screen
- **Route:** `organizer/edit/{eventId?}`
- **Role:** Organizer
- **UiState:** `EventEditUiState(eventId: String?, title: String, description: String, selectedCategoryId: String, selectedVenueId: String, startAt: Long, endAt: Long, capacity: Int, saleStartAt: Long, saleEndAt: Long, registrationDeadline: Long, cancellationPolicyHours: Int, status: EventStatus, imageInputs: List<String>, categories: List<Category>, venues: List<Venue>, isSaving: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - Fill title, description, category, venue
  - Validate capacity ≤ venue capacity
  - Set sale start/end dates and registration deadline
  - Set cancellation policy hours
  - Add poster images (compressed Base64 / URL)
  - Change visibility (Draft / Published / Cancelled)
- **Navigation:**
  - `organizer/edit` → `organizer`

### 3.3 Ticket Types & Inventory Management Screen
- **Route:** `organizer/tickets/{eventId}`
- **Role:** Organizer
- **UiState:** `TicketTypesUiState(eventId: String, eventCapacity: Int, ticketTypes: List<TicketType>, isLoading: Boolean, isSaving: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - Add/edit ticket types (General Admission, VIP, Student)
  - Set price, quantity limit, and availability period
  - Deactivate/activate ticket type
  - Enforce sum of type limits ≤ event capacity
- **Navigation:**
  - `organizer/tickets/{eventId}` → `organizer`

### 3.4 Check-in & Scanner Screen
- **Route:** `checkin/{eventId}`
- **Role:** Organizer
- **UiState:** `CheckInUiState(eventId: String, manualInput: String, scanResult: CheckInResult?, isLoading: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - Manual reference entry
  - QR barcode scanner (CameraX)
  - Display atomic check-in result: ✅ Valid / ⚠️ Already Used / ❌ Wrong Event / ❌ Cancelled
- **Navigation:**
  - `checkin/{eventId}` → `checkin/{eventId}/attendees`

### 3.5 Attendee List & Check-in Totals Screen
- **Route:** `checkin/{eventId}/attendees`
- **Role:** Organizer
- **UiState:** `AttendeeListUiState(eventId: String, checkedInCount: Int, totalTicketsCount: Int, attendees: List<TicketAttendeeInfo>, searchQuery: String, statusFilter: CheckInFilter, isLoading: Boolean)`
- **Key Interactions:**
  - Search attendee list by name or reference
  - Filter by Checked In / Not Checked In
  - Manual check-in toggle
- **Navigation:**
  - `checkin/{eventId}/attendees` → `checkin/{eventId}`

---

## 4. Admin Screens

### 4.1 Admin Portal Home
- **Route:** `admin`
- **Role:** Admin
- **UiState:** `AdminHomeUiState(totalUsers: Int, totalEvents: Int, pendingModerationCount: Int, isLoading: Boolean)`
- **Key Interactions:**
  - Navigate to User Management
  - Navigate to Catalog Management
  - Navigate to Activity Logs
- **Navigation:**
  - `admin` → `admin/users`
  - `admin` → `admin/catalog`
  - `admin` → `admin/logs`

### 4.2 User Management Screen
- **Route:** `admin/users`
- **Role:** Admin
- **UiState:** `UserManagementUiState(users: List<User>, searchQuery: String, roleFilter: UserRole?, statusFilter: UserStatus?, isLoading: Boolean)`
- **Key Interactions:**
  - List all registered users
  - Search by name, username, or email
  - Tap user to manage role or activation status
- **Navigation:**
  - `admin/users` → `admin/users/{uid}`

### 4.3 User Details & Role/Status Management Screen
- **Route:** `admin/users/{uid}`
- **Role:** Admin
- **UiState:** `UserDetailsUiState(user: User?, isSaving: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - Assign or update role (Attendee / Organizer / Admin)
  - Activate or suspend user account
  - Enforce last-admin protection (cannot demote/suspend last admin)
- **Navigation:**
  - `admin/users/{uid}` → `admin/users`

### 4.4 Category & Venue Management Screen
- **Route:** `admin/catalog`
- **Role:** Admin
- **UiState:** `CatalogUiState(categories: List<Category>, venues: List<Venue>, selectedTab: CatalogTab, isAddingCategory: Boolean, isAddingVenue: Boolean, errorMessage: String?)`
- **Key Interactions:**
  - Add, edit, or delete categories
  - Add, edit, or delete venues (name, address, city, capacity)
  - Enforce in-use protection (cannot delete category or venue if `eventCount > 0`)
- **Navigation:**
  - `admin/catalog` → `admin`

### 4.5 Activity Log Viewer Screen
- **Route:** `admin/logs`
- **Role:** Admin
- **UiState:** `ActivityLogsUiState(logs: List<ActivityLog>, searchQuery: String, actionFilter: ActionType?, dateRange: DateRange?, isLoading: Boolean)`
- **Key Interactions:**
  - View system activity logs (login, booking, event edit, check-in, user status change)
  - Filter by user, date, or action type
- **Navigation:**
  - `admin/logs` → `admin`

---

## 5. Cross-Role Shared Screens

### 5.1 Role-Specific Dashboards & Reports Screen
- **Route:** `dashboard`
- **Role:** All authenticated users (content tailored to role)
- **UiState:** `DashboardUiState(userRole: UserRole, adminStats: AdminDashboardStats?, organizerStats: OrganizerDashboardStats?, attendeeStats: AttendeeDashboardStats?, selectedDateRange: DateRange?, selectedEventId: String?, isLoading: Boolean)`
- **Key Interactions:**
  - Filter stats by date range and event
  - Admin view: Total users, events, bookings, tickets sold, revenue
  - Organizer view: Own events, ticket sales, occupancy %, check-in stats
  - Attendee view: Upcoming events and recent bookings
- **Navigation:**
  - Top app bar / bottom nav bar item

### 5.2 Notification Center Screen
- **Route:** `notifications`
- **Role:** All authenticated users
- **UiState:** `NotificationCenterUiState(notifications: List<InAppNotification>, unreadCount: Int, isLoading: Boolean)`
- **Key Interactions:**
  - View booking confirmations, event cancellation/update alerts, and reminders
  - Mark notification as read / mark all as read
- **Navigation:**
  - `notifications` → event or booking route (if attached to notification)

### 5.3 Profile & Settings Screen
- **Route:** `profile`
- **Role:** All authenticated users
- **UiState:** `ProfileUiState(user: User?, darkThemeEnabled: Boolean, isLoading: Boolean)`
- **Key Interactions:**
  - View profile info (Name, Email, Username, Role)
  - Toggle dark theme
  - Sign out
- **Navigation:**
  - `profile` → `welcome` / `login` (on sign out)
