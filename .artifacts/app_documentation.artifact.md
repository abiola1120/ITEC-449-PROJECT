# Event and Ticket Management System - Comprehensive Technical Documentation

> [!NOTE]
> This document provides complete architectural, technological, and functional documentation for the **Event and Ticket Management System** Android application. It is designed to prepare you for any technical questions or defense regarding the codebase, architecture, tools, and implementation details.

---

## 1. Executive Summary & App Overview

The **Event and Ticket Management System** is a modern, native Android application built for end-to-end event discovery, ticket purchasing, event organization, and administrative oversight.

The application supports three distinct user roles with tailored dashboards and permission-based routing:
1. **Attendee**: Browse events, search & filter by category/city/price, view event details, book tickets, view digital tickets with generated QR codes, manage favorites & reminders, and view notifications.
2. **Organizer**: Manage owned events, create/edit events, configure ticket types (pricing and quantity limits), and perform attendee check-ins (manual entry or QR scanning).
3. **Admin**: Oversee the entire system through an Admin Portal, manage users (promote/suspend/change roles), manage the category and venue catalog, and inspect real-time activity logs and analytics dashboards.

---

## 2. Tech Stack & Tools Used

### Core Technologies
- **Programming Language**: **Kotlin** (100% Kotlin codebase, taking advantage of null safety, extension functions, coroutines, and modern language features).
- **UI Framework**: **Jetpack Compose** (Declarative UI toolkit leveraging Material 3 design system, Lazy columns/grids, and animations).
- **Architecture Pattern**: **MVVM (Model-View-ViewModel)** with Unidirectional Data Flow (UDF).
- **Navigation**: **Jetpack Navigation Compose** (`NavHost`, type-safe argument passing, dynamic start destination based on authentication state and user role).

### Backend & Cloud Services (Google Firebase)
- **Firebase Authentication**: Handles secure user sign-up, login, password reset, session initialization, and role persistence.
- **Cloud Firestore**: NoSQL real-time cloud database storing collections for:
  - `users` (profiles, roles, status)
  - `events` (published, draft, cancelled events)
  - `categories` & `venues` (catalog management)
  - `ticket_types` & `bookings` & `tickets` (ticketing and purchase records)
  - `activity_logs` (audit trail of admin/organizer actions)
  - `notifications` (in-app alerts and reminders)

### Concurrency & Asynchronous Programming
- **Kotlin Coroutines**: Managing asynchronous threads and background tasks safely.
- **StateFlow & SharedFlow**: Reactive state management between repositories and ViewModels (`uiState` exposed as immutable `StateFlow`, UI one-off events exposed via `SharedFlow`).
- **callbackFlow**: Converting real-time Firestore snapshot listeners into reactive Kotlin Flows with robust `.catch { ... }` error handling operators to prevent crashes on network errors or permission restrictions.

### Dependency Injection & Utilities
- **Manual Dependency Injection**: Implemented via a custom `AppContainer` initialized in the Custom Application class (`EventApp`), providing repositories and dependencies without heavy reflection frameworks.
- **Libraries**:
  - `com.google.zxing:core`: QR code bitmap generation for digital tickets.
  - `io.coil-kt:coil-compose`: Asynchronous image loading.
  - `desugar_jdk_libs`: Java 8+ API desugaring support for older Android SDKs.

---

## 3. Project Architecture & Directory Structure

The project follows clean architecture principles, grouped into logical packages under `com.eventexperts.eventandticketmanagementsystem`:

```text
com.eventexperts.eventandticketmanagementsystem/
├── data/
│   ├── model/         # Data classes (User, Event, Booking, Ticket, Category, Venue, etc.)
│   └── repository/    # Firestore repositories (Auth, Event, Booking, Ticket, Catalog, etc.)
├── ui/
│   ├── activitylog/   # Activity logs screen & ViewModel
│   ├── admin/         # Admin portal, User management & details screens
│   ├── auth/          # Login, Register, Welcome, Reset password & Suspended screens
│   ├── booking/       # Booking checkout & My Bookings screens
│   ├── catalog/       # Admin catalog management
│   ├── checkin/       # Organizer check-in scanner & attendee list
│   ├── components/    # Reusable composables (Top bar, bottom nav, loading views)
│   ├── dashboard/     # Analytics & role dashboards
│   ├── events/        # Event details, search, and reviews screens
│   ├── home/          # Home screen & ViewModel
│   ├── navigation/    # AppNavHost and Routes definition
│   ├── notifications/ # Notification center
│   ├── organizer/     # Organizer dashboard, event editor, ticket types
│   ├── profile/       # User profile & settings
│   └── tickets/       # Digital ticket screen with QR code
└── util/              # Seed data tool, Result wrapper, QR generator
```

---

## 4. How Everything Works (Key Workflows)

### A. App Startup & Navigation Flow (`AppNavHost.kt`)
1. **Application Initialization**: `EventApp` instantiates `AppContainer`, which wires up Firebase instances and repositories.
2. **Session Check**: `AuthViewModel` initializes the session. While initializing, `LoadingView` is displayed.
3. **Role-Based Routing**: Once initialized:
   - If user is logged out $\rightarrow$ `Routes.WELCOME`
   - If user is suspended $\rightarrow$ `Routes.SUSPENDED`
   - If user is logged in $\rightarrow$ Routed based on role (`ORGANIZER_DASHBOARD`, `ADMIN_HOME`, or `HOME`).
4. **Bottom & Top Bars**: Dynamically shown or hidden depending on the current route (hidden on auth/welcome/suspended screens).

### B. Real-Time Data Observation (`callbackFlow` & `.catch`)
- Repositories expose real-time Firestore queries using `callbackFlow` with `addSnapshotListener`.
- ViewModels collect these flows within `viewModelScope.launch`.
- **Crash Prevention**: Every `.collect` is preceded by `.catch { _uiState.value = _uiState.value.copy(isLoading = false) }` to gracefully handle permission denials (e.g. non-admin accessing admin logs) without crashing the app.

### C. Ticketing & Booking Workflow
1. User views event details (`EventDetailsScreen`).
2. Taps "Book Now" $\rightarrow$ selects ticket type and quantity in `BookingScreen`.
3. `BookingRepository` executes transaction/creation, generating unique tickets with reference codes and idempotency keys.
4. User views digital ticket (`DigitalTicketScreen`), which renders a QR code generated via `QrCodeGenerator` (`zxing`).
5. Organizers scan or manually check-in attendees at the event via `CheckInScreen`.

---

## 5. Potential Defense / Interview Questions & Answers

### Q1: Why did you choose Manual DI (`AppContainer`) over Hilt or Dagger?
* **Answer**: Manual DI via `AppContainer` keeps the dependency graph explicit, lightweight, and easy to understand without boilerplate annotations or compilation overhead. It gives absolute control over object instantiation and mock repositories for unit testing.

### Q2: How does the app handle Firestore real-time updates and errors (like PERMISSION_DENIED)?
* **Answer**: Repositories use `callbackFlow` wrapping Firestore snapshot listeners. In ViewModels, we use Kotlin Flow's `.catch { ... }` operator right before `.collect { }`. This catches any upstream exceptions (such as Firestore permission errors when an attendee attempts to read admin logs), updates the UI state safely (`isLoading = false`), and prevents app crashes.

### Q3: How is role-based access control (RBAC) enforced?
* **Answer**: User roles (`ATTENDEE`, `ORGANIZER`, `ADMIN`) are stored in Firestore and loaded into the `User` model upon authentication. `AppNavHost` and UI components inspect `currentUser.role` to render appropriate navigation bars, dashboards, and restrict access to admin-only ViewModels and screens.
