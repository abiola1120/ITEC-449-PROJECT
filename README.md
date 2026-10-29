# Native Android Event & Ticket Management System

This repository contains the official Kotlin & Jetpack Compose native Android implementation for the **ITEC499 & CMPR205 Lab Project: Event & Ticket Management System**.

---

## 📱 Features & Highlights

### 1. Mandatory Core Features (100% Implemented)
* **Role-Based Access Control:** Separate workflows and navigation graphs for **Attendee**, **Organizer**, and **Administrator**.
* **Authentication & Username Sign-In:** Sign in using either an **email address or unique username**. Register as an Attendee or Organizer.
* **Event Management:** Create, edit, and publish events with capacity limits, ticket sale windows, registration deadlines, and Draft/Published/Cancelled visibility.
* **Ticket Tiers & Inventory Control:** Create General Admission, VIP, and Student ticket types. Enforces that the sum of tier limits cannot exceed total event capacity.
* **Discovery & Multi-Filter Search:** Search events by keyword; filter by Category, City, and Max Price.
* **Atomic Capacity-Safe Booking:** Transactional ticket booking preventing overselling. Generates unique booking references (`BK-XXXXXXXX`) and ticket references (`EVT-XXXXXXXXXX`). Prevents duplicate confirmations via idempotency keys. Includes a 5% service fee calculation.
* **Digital E-Tickets & QR Codes:** View e-tickets with rendered QR code graphics (ZXing) and status badges (`Valid`, `Used`, `Cancelled`, `Refunded`). Cancel bookings according to event cancellation policy windows.
* **Organizer Check-In & Anti-Reuse:** Organizer ticket check-in by reference. Atomic anti-reuse validation prevents double check-ins. Real-time attendee check-in progress stats.
* **User Management & Last-Admin Protection:** Admins manage user roles and account activation/suspension. Enforces **Last-Admin Protection** (prevents demoting or suspending the final active Admin).
* **Category & Venue Catalog with In-Use Protection:** Admins manage categories and venues. Prevents deleting categories or venues currently assigned to active events (`eventCount > 0`).
* **Role-Tailored Dashboards:**
  * **Admin:** Total Users, Events, Bookings, Tickets Sold, Gross Revenue (excluding cancelled/refunded bookings).
  * **Organizer:** Own Events, Tickets Sold, Occupancy %, Revenue $, per-event breakdown table.
  * **Attendee:** Confirmed bookings and tickets summary.
* **In-App Notification Center:** Real-time notifications for booking confirmations, cancellations, and event updates (fan-out to all ticket holders).
* **Append-Only Activity Audit Log:** Immutable audit trail logging sign-ins, logouts, bookings, event edits, check-ins, and admin actions.

### 2. Lab Extras & Optional Features
* **Promo Codes:** Percentage (% off) or Fixed ($ off) promo code validation and checkout discount calculation.
* **Favorites & Local Reminders:** Save events and toggle local notification reminders.
* **Attendance-Verified Reviews & Ratings:** Attendees can rate (1-5 stars) and review events only after attendance is verified at check-in (`attendance/{eventId}_{userId}`).
* **Calendar & Share Integration:** Android System Intents to add events to calendar or share event details.
* **CSV Report Exporter:** Export attendance and sales reports to CSV string format.

---

## 🏗️ Architecture & Technology Stack

* **Language:** 100% Kotlin (No Java source code)
* **UI Framework:** Jetpack Compose + Material 3 Design System
* **Architecture:** MVVM + Repository pattern with manual Dependency Injection (`AppContainer`)
* **State Management:** `StateFlow`, `SharedFlow`, and Compose `collectAsStateWithLifecycle`
* **Navigation:** Navigation Compose (`AppNavHost`) with dynamic start destination based on role and account status
* **Database & Auth:** Firebase Authentication & Cloud Firestore (Security Mode B: Atomic Firestore Transactions + Rules)
* **QR Codes:** ZXing Core library
* **Asynchronous Operations:** Kotlin Coroutines & `kotlinx-coroutines-play-services`
* **Testing:** JUnit, MockK, Turbine (`app:testDebugUnitTest`)

---

## 🚀 Prerequisites & Firebase Setup

### Prerequisites
* **Android Studio:** Android Studio Ladybug / Koala or newer
* **JDK:** Java 11 or 17
* **Android SDK:** `compileSdk 37`, `targetSdk 37`, `minSdk 24` (with Core Library Desugaring enabled)

### Firebase Setup
1. Place your `google-services.json` in the `app/` directory:
   ```text
   Eventandticketmanagementsystem/app/google-services.json
   ```
2. Enable **Authentication** in Firebase Console -> **Build -> Authentication -> Email/Password**.
3. Create a **Cloud Firestore Database** in **Production Mode**.

---

## 🛠️ Building & Running

### Build Debug APK
```bash
./gradlew assembleDebug
```
*Output APK Location:* `app/build/outputs/apk/debug/app-debug.apk`

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

---

## 🧪 Sample Data & Demo Script

The app includes an automated **`SeedDataTool`** that seeds default categories, venues, and sample events on initial startup if the Firestore database is empty.

### Demo Accounts
1. **Admin Account:**
   * **Username:** `admin_demo` | **Email:** `admin@eventapp.com`
   * **Capabilities:** Manage users, assign roles, activate/suspend accounts, manage catalog, view activity audit logs, view global revenue metrics.
2. **Organizer Account:**
   * **Username:** `organizer_demo` | **Email:** `organizer@eventapp.com`
   * **Capabilities:** Create & edit events, configure ticket tiers & limits, check in attendees, view organizer sales dashboard.
3. **Attendee Account:**
   * **Username:** `attendee_demo` | **Email:** `attendee@eventapp.com`
   * **Capabilities:** Discover events, search with filters, book tickets, view e-tickets & QR codes, cancel bookings within policy window, write attendance-verified reviews.

### Demo Script (Main Workflows)
1. **Sign Up & Sign In:** Register a new user account as an Attendee or Organizer. Test sign-in using username or email.
2. **Event Creation (Organizer):** Sign in as Organizer -> Navigate to "My Events" -> Tap '+' -> Fill event title, description, category, venue -> Save & Publish -> Manage ticket types & price limits.
3. **Event Discovery & Search (Attendee):** Sign in as Attendee -> Browse upcoming events on Home screen -> Go to Search tab -> Filter by category, location, or max price slider -> Select event to view details.
4. **Checkout & Ticket Generation:** Tap "Book Now" -> Choose ticket quantity -> Review 5% service fee breakdown -> Confirm booking -> Receive unique booking reference (`BK-XXXXXXXX`) -> Open Digital E-Ticket to view QR code (`EVT-XXXXXXXXXX`).
5. **Check-In Validation (Organizer):** Sign in as Organizer -> Go to event check-in -> Enter ticket reference (`EVT-XXXXXXXXXX`) -> See ✅ **VALID TICKET** result. Re-enter same reference -> See ⚠️ **ALREADY USED** anti-reuse alert.
6. **User Management & Last-Admin Protection (Admin):** Sign in as Admin -> Open User Management -> Edit user role or suspend account -> Attempt to demote/suspend the last Admin to verify **Last-Admin Protection** rejection.
7. **Audit Trail (Admin):** Open Activity Logs tab -> View real-time immutable audit trail for logins, bookings, event edits, and check-ins.

---

## 🔒 Security & Policy Compliance

* **Server-Side & Transactional Validation:** Capacity limits, inventory limits, and check-ins are verified atomically inside Firestore transactions (`runTransaction`).
* **Last-Admin Protection:** Administrative role demotion or account suspension validates remaining active admin counts before executing.
* **In-Use Catalog Protection:** Categories and venues cannot be deleted while assigned to active events (`eventCount > 0`).
* **Review Verification:** Reviews require verified attendance records (`attendance/{eventId}_{userId}`) generated upon check-in.
* **Secrets Safety:** `.gitignore` excludes sensitive files (`google-services.json` secrets, keystores, `.env`).
#   I T E C - 4 4 9 - P R O J E C T  
 #   I T E C - 4 4 9 - P R O J E C T  
 # ITEC-449-PROJECT
# ITEC-449-PROJECT
