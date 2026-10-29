# CLAUDE.md — Event Management App (Kotlin / Jetpack Compose / Firebase)

You are helping a group of four beginner students build a **native Android Event Management App**. The Android Studio project already exists in this folder. Your job is to write the code, step by step, following this document. The students will run the app in Android Studio (emulator/device); you build and test with Gradle from the terminal.

**Course context:** this is the **ITEC499 & CMPR205 lab project: Event & Ticket Management System**. The required screens and rules in **Section 1A** come from the official project requirements and are **mandatory**. **Do not use Java:** write everything in Kotlin (the lab forbids Java). The lab also requires a mobile app with role-based authorization, input validation, consistent capacity/inventory/ticket status, **sample data**, and a demonstration of the main workflows.

---

## 0. How you must work (read first)

1. **Work phase by phase** (Section 12). Finish and verify one phase before starting the next. Do not build everything at once.
2. **Before writing code, inspect the project**: read `settings.gradle(.kts)`, `build.gradle(.kts)` files, `gradle/libs.versions.toml` (if present), `AndroidManifest.xml`, and `applicationId`. Use the **existing package name and applicationId**; do not rename them. Wherever this document says `com.example.eventapp`, substitute the real one.
3. **After each phase run** `./gradlew assembleDebug` (use `gradlew.bat` on Windows) and `./gradlew testDebugUnitTest`. Fix errors before moving on. Report results briefly.
4. **Some steps only a human can do** (Firebase console, `google-services.json`, API keys, running the emulator). When you reach one, **stop and give the student a short numbered checklist**, then wait for confirmation. Never invent keys or fake config files.
5. **Never commit secrets.** No private keys, service-account JSON, payment secrets, or API keys in code or Git. `google-services.json` is fine for a private student repo, but add `local.properties`, `*.jks`, `*.keystore`, `serviceAccount*.json`, and `.env` to `.gitignore`.
6. **Beginner-friendly code**: small files, clear names, short comments explaining *why*. Prefer simple, standard Android patterns over clever ones.
7. **Keep `main` buildable.** Suggest a feature branch name per phase (for example `feature/auth`) and small commits with clear messages. Do not run `git push` or force-push unless asked.
8. **Do not add libraries not listed here** without telling the student why. Use the latest stable versions compatible with the project's Kotlin/AGP; check the Compose BOM instead of pinning individual Compose versions.
9. **Security is part of the grade.** Never trust the client for capacity, payments, check-in, promo codes, or roles. See Section 7.
10. When a requirement is ambiguous, **ask one short question**, then continue.
11. Keep a running **`PROGRESS.md`** in the project root: phases done, decisions made, manual steps still pending, known limitations. Update it at the end of every phase. The students will use it for their final report.
12. **Section 1A is mandatory.** Build every required lab feature before any extra feature. Extras never replace required features.
13. **Kotlin only.** Never create `.java` files or add Java source. Gradle Kotlin DSL and Kotlin for Cloud Functions' Android client code; Cloud Functions themselves are TypeScript (backend, not part of the mobile app).

---

## 1. Project overview

Users discover events, book tickets, and manage events, with **role-based authorization** for three roles.

| Role | Capabilities |
|---|---|
| **Attendee** | Browse upcoming published events; search by title/keyword; filter by date, category, location, price; view event details; book tickets (checkout with fees and attendee info); view booking history and e-tickets; cancel per the event policy; attendee dashboard; in-app notifications; extras: waitlist, favorites and reminders, reviews, nearby and recommended events, Wallet pass |
| **Organizer** | Create/edit events (Draft, Published, Cancelled); set sale dates and registration deadlines; manage ticket types and inventory; check in attendees (QR or ticket ID) and see attendee lists; organizer dashboard; extras: promo codes, analytics |
| **Administrator** | Manage users (roles, activate/suspend); manage categories and venues; admin dashboard and reports; activity log; moderate events. The **first** admin is set manually in the Firebase console; after that, admins assign roles inside the app |

Project priorities:
1. **Required lab features** (Section 1A), starting with the **MVP** (Section 11, Milestone 1) then the remaining required features (Milestone 2). 2. **Extra features** (Section 10) only after all required features work. 3. **Optional features** (payments and refund tracking, seat selection, report export, maps, offline, chat, push notifications) last.

### 1A. Required lab features (official requirements)

| # | Screen | Required behavior | Who |
|---|---|---|---|
| 1 | Login & Registration | Access control for Admin / Organizer / Attendee; secure sign-in with **email or username** + password; register as Organizer or Attendee; reset forgotten password; sign out securely | All |
| 2 | Event Management | Create event (title, description, date/time, venue, category, images, capacity); edit; visibility **Draft / Published / Cancelled**; list and manage own events; set **ticket sale start/end dates** and **registration deadlines**; **block new sales** for cancelled or sold-out events | Organizer |
| 3 | Ticket Type & Inventory | Create ticket types (General Admission, VIP, Student, ...); price, quantity limit, availability period per type; edit or deactivate before sales when permitted; show available and sold-out quantities; **never sell beyond capacity** | Organizer |
| 4 | Event Discovery & Search | Browse upcoming and published events; search by title or keyword; filter by **date, category, location, price**; details with venue, schedule, available tickets, organizer info; clear availability and prices | Attendee |
| 5 | Ticket Booking & Checkout | Select event, ticket type, quantity; review details, total cost, and **fees**; enter or confirm attendee information; confirm and receive a **unique booking reference**; prevent duplicate confirmations and overselling; show result **Confirmed / Pending / Failed / Cancelled** | Attendee |
| 6 | My Tickets & Booking History | Upcoming and previous bookings; ticket with event, type, date, **unique QR code or ticket identifier**; status **Valid / Used / Cancelled / Refunded**; cancel per the event cancellation policy; open an e-ticket without making a new booking | Attendee |
| 7 | Ticket Validation & Check-in | Scan a QR code or enter a ticket identifier; validate against the event; mark as **Checked In / Used**; reject invalid, cancelled, or already used tickets; **check-in totals and attendee lists** | Organizer |
| 8 | User Management | View users and roles; edit user info; **activate/suspend**; assign or update Admin / Organizer / Attendee roles; view a user's events or bookings where authorized; **restrict suspended users** from protected operations | Admin |
| 9 | Category & Venue Management | Add, edit, delete categories (Music, Sports, Conferences, ...); manage venues (name, address, location, capacity); assign an existing category and venue to an event; **prevent deleting** categories or venues in use | Admin (organizers choose from the lists) |
| 10 | Dashboard & Reports | **Admin:** total users, events, bookings, tickets sold, revenue. **Organizer:** own events, ticket sales, occupancy, check-in stats. **Attendee:** upcoming events and recent bookings. Filter by date range and event; charts or tables | All |
| 11 | Notifications | In-app notifications when a booking is confirmed or cancelled; reminders before an upcoming event; notify ticket holders when an event is updated or cancelled; **read/unread status** | All |
| 12 | Activity Log | Record sign-in, event creation/editing, booking, cancellation, check-in; admins filter by user, date, or action type; records protected from unauthorized modification | Admin |
| 13 | Lab-optional | Payment processing and refund tracking; event ratings and reviews; seat selection for assigned seating; export ticket and attendance reports | Optional |

**Development requirements:** mobile app with an appropriate UI; **Kotlin only (no Java)**; role-based authorization and basic input validation; keep event capacity, ticket inventory, and ticket status **consistent**; provide **sample data** and demonstrate the main workflows.

---

## 2. Tech stack

| Technology | Use | Priority |
|---|---|---|
| Kotlin | Language | Core |
| Android Studio + Gradle (Kotlin DSL, version catalog) | Build | Core |
| Jetpack Compose + Material 3 | UI | Core |
| Navigation Compose | Navigation | Core |
| ViewModel + Kotlin Coroutines + StateFlow | State | Core |
| MVVM + Repository pattern | Architecture | Core |
| Hilt **or** manual DI (see note) | Dependency injection | Core |
| Firebase Authentication (email/password) | Accounts | Core backend |
| Cloud Firestore | Data | Core backend |
| Firebase Cloud Functions (TypeScript) | Trusted server logic | Security (see Section 7 modes) |
| Firebase Storage | Event images (required by the lab; see the fallback in Section 6) | Core backend |
| Coil (`coil-compose`) | Loading images | Optional |
| ZXing (`com.journeyapps:zxing-android-embedded` or `com.google.zxing:core`) | Generate QR codes | Extra |
| CameraX + ML Kit Barcode Scanning | Scan QR codes | Extra |
| Charting library (Vico or MPAndroidChart) | Dashboard and report charts (required) | Core |
| WorkManager | Local event reminders (no server needed) | Extra |
| Play Services Location (`play-services-location`) | Device location for nearby events | Extra |
| GeoFire utils (`com.firebase:geofire-android-common`) | Geohash bounds for nearby queries in Firestore | Extra |
| Google Wallet API (`com.google.android.gms:play-services-pay`) | "Add to Google Wallet" ticket pass | Stretch |
| Firebase Cloud Messaging | Notifications | Optional |
| Room | Offline cache | Optional |
| Maps Compose + Google Maps SDK | Venue maps | Optional |
| JUnit, MockK (or Mockito), Turbine, Compose UI test | Testing | Core |

**DI note:** Students are beginners. Use **manual DI** (an `AppContainer` created in the `Application` class, passed to ViewModel factories) unless the project already uses Hilt. Keep it simple and consistent.

**SDK:** Use the project's existing `compileSdk`/`targetSdk`. Set `minSdk` to **26** unless the project already differs.

---

## 3. Architecture and package structure

Single `app` module. MVVM + Repository.

```
com.example.eventapp/
  EventApp.kt                  // Application class, creates AppContainer
  MainActivity.kt              // sets content, hosts NavHost
  di/AppContainer.kt
  data/
    model/                     // User, Event, TicketType, Booking, Ticket, PromoCode, WaitlistEntry, enums
    remote/                    // FirestoreDataSource, AuthDataSource, FunctionsDataSource
    local/                     // Room (optional, later)
    repository/                // AuthRepository, EventRepository, BookingRepository, TicketRepository, ...
  domain/                      // small use cases only where they add clarity (BookTicketUseCase, ...)
  ui/
    theme/                     // Material 3 theme (light + dark), typography
    components/                // shared composables: EventCard, LoadingView, ErrorView, EmptyView, PrimaryButton, ...
    navigation/                // Routes.kt, AppNavHost.kt
    auth/                      // Welcome, Login, Register, ResetPassword
    home/                      // Home / discovery
    events/                    // Search+filters, EventDetails
    booking/                   // Booking flow, confirmation, MyBookings
    tickets/                   // Digital ticket + QR
    waitlist/
    favorites/                 // Saved events + reminders
    reviews/                   // Ratings and reviews
    nearby/                    // Nearby events (location)
    recommendations/           // RecommendationEngine + UI
    organizer/                 // Dashboard, Create/Edit event
    checkin/                   // Scanner + manual check-in
    dashboard/                 // Admin / Organizer / Attendee dashboards and reports (charts + tables)
    analytics/                 // Per-event analytics (extra)
    admin/                     // User management
    catalog/                   // Category and venue management
    notifications/             // In-app notification center
    activitylog/               // Admin activity log viewer
    profile/                   // Profile + settings
  util/                        // Result wrapper, date formatting, validators
```

**Rules**
- Composables never touch Firebase directly. Flow: `Screen → ViewModel → Repository → DataSource`.
- Each screen has a `UiState` data class (loading / error / data) exposed as `StateFlow`; one-off events (navigation, snackbars) via `SharedFlow`/`Channel`.
- Repositories return a small `Result<T>` wrapper (success / error with a user-friendly message). Never expose raw Firebase exceptions to the UI.
- Use `Flow` + Firestore snapshot listeners for live data (events list, my bookings); `suspend` functions for one-shot writes. Use `kotlinx-coroutines-play-services` for `.await()`.
- Handle **loading, empty, error, and permission-denied** states on every screen.
- Validate all input in the ViewModel (email format, password length ≥ 8, required fields, price ≥ 0, capacity ≥ 1, date in the future).
- Dates: store as Firestore `Timestamp`; format with `java.time` (core library desugaring if minSdk < 26).
- Money: store amounts as **integer minor units** (cents) plus a `currency` string (for example `"USD"`). Never use floating point for money.

---

## 4. Navigation and screens

Routes (define as sealed class/object in `Routes.kt`):

| Route | Screen | Who |
|---|---|---|
| `welcome` | Welcome | Signed out |
| `login`, `register`, `reset_password` | Auth | Signed out |
| `home` | Event discovery (list + featured) | All signed-in |
| `search` | Keyword search + filters (date, category, location/city, price) | All signed-in |
| `event/{eventId}` | Event details (+ add-to-calendar, share, join waitlist) | All signed-in |
| `book/{eventId}` | Booking (ticket type, quantity, promo code) → confirmation | Attendee |
| `bookings` | My bookings (upcoming / previous / cancelled) | Attendee |
| `ticket/{ticketId}` | Digital ticket (reference + QR) | Attendee |
| `favorites` | Saved events (+ reminder toggle) | Attendee |
| `nearby` | Nearby events (radius chips, distance on cards) | All signed-in |
| `reviews/{eventId}` | Read reviews / write a review (after check-in) | All signed-in |
| `profile` | Profile and settings (logout, theme) | All signed-in |
| `organizer` | Organizer dashboard (my events) | Organizer |
| `organizer/edit/{eventId?}` | Create / edit event, ticket types, promo codes | Organizer |
| `organizer/analytics/{eventId}` | Analytics | Organizer |
| `checkin/{eventId}` | QR scanner + manual reference entry | Organizer |
| `organizer/tickets/{eventId}` | Ticket types and inventory | Organizer |
| `checkin/{eventId}/attendees` | Attendee list and check-in totals | Organizer |
| `dashboard` | Role-specific dashboard (Admin / Organizer / Attendee) with date-range and event filters, charts and tables | All signed-in |
| `notifications` | Notification center (read/unread) | All signed-in |
| `admin` | Admin home (links to users, catalog, logs, moderation) | Admin |
| `admin/users`, `admin/users/{uid}` | User management (list, edit, activate/suspend, change role, view activity) | Admin |
| `admin/catalog` | Categories and venues (add/edit/delete, in-use protection) | Admin |
| `admin/logs` | Activity log (filter by user, date, action) | Admin |
| `suspended` | Account suspended screen (sign out only) | Suspended users |

- **Start destination** is decided by auth state + role (restore session after app restart; no flicker of the login screen).
- Bottom navigation bar: Attendee → Home, Search, My Bookings, Favorites, Profile. Organizer → Dashboard, My Events, Home, Profile. Admin → Dashboard, Users, Catalog, Logs (Home and Profile in a More menu). A notifications bell with an unread badge sits in the top app bar for every role.
- Guard routes by role in the UI **and** in Firestore rules/backend (the UI check is only for UX).
- Support dark mode (Material 3 color scheme), and add `contentDescription` to icons/images for accessibility.

---

## 5. Data model (Cloud Firestore)

Use these collections exactly (field names are camelCase). Create matching Kotlin data classes with default values (Firestore needs a no-arg constructor) and an `@DocumentId` id field.

**`users/{uid}`**
`uid, name, username (unique, lowercase), email, phone?, role ("attendee" | "organizer" | "admin"), status ("active" | "suspended"), createdAt (Timestamp), photoUrl?`
- **Username sign-in:** Firebase Auth signs in with email, so also store `usernames/{username}` = `{uid, email}`. Login accepts an email **or** a username: if the input has no `@`, look up `usernames/{username}` to get the email, then sign in with email + password. Mode A: do the lookup in a callable `resolveUsername` so emails are not exposed. Mode B: allow `get` on a single `usernames` doc but never `list`, and note the limitation in `PROGRESS.md`. Uniqueness is enforced by creating the `usernames` doc and the profile in one transaction.
- **Suspended users** (`status == "suspended"`) cannot book, create or edit events, check in, review, or write anything except signing out. Enforce this in rules/backend and show the `suspended` screen.
- Only an **Admin** can change another user's `role` or `status`; users can never change their own.

**`events/{eventId}`**
`title, description, categoryId, categoryName, organizerId, organizerName, startAt (Timestamp), endAt?, venueId, venueName, address, city, latitude?, longitude?, geohash?, imageUrls (list; first = poster; or small Base64 strings in the no-Storage fallback), capacity (Int; must not exceed venue capacity), bookedCount (Int), basePrice (Int, lowest ticket price, minor units), currency, saleStartAt?, saleEndAt?, registrationDeadline?, cancellationPolicyHours (Int, default 24; 0 = non-refundable), status ("draft" | "published" | "cancelled" | "suspended"), ratingAverage (Double, default 0), ratingCount (Int, default 0), createdAt, updatedAt`
- Subcollection or top-level `ticketTypes` (see below). Keep `capacity`/`bookedCount` as the overall limit.
- Seats remaining = `capacity - bookedCount`. An event is **sold out** when `bookedCount >= capacity`.
- Sales are **blocked** when `status != published`, the event is sold out, the sale window (`saleStartAt`/`saleEndAt`) is closed, the `registrationDeadline` has passed, or the ticket type is inactive.
- Deleting or changing an event's category/venue updates the `eventCount` counters (see `categories`, `venues`).

**`ticketTypes/{typeId}`**
`eventId, name ("General Admission", "VIP", "Student", "Early Bird"), price (Int, minor units), quantityLimit (Int), soldCount (Int), availableFrom?, availableTo?, active (Boolean)`
- Available = `quantityLimit - soldCount`; show "Sold out" at 0. The sum of type limits must not exceed the event `capacity`.
- An organizer may edit price/limit or deactivate a type while `soldCount == 0`; once sales started, only increase the limit or deactivate (never lower it below `soldCount`, never change the price of sold tickets).

**`bookings/{bookingId}`**
`bookingRef (unique, e.g. BK-XXXXXXXX), userId, eventId, ticketTypeId, quantity, attendeeName, attendeeEmail, attendeePhone?, promoCode?, subtotal (Int), feeAmount (Int), discountAmount (Int), totalAmount (Int), currency, status ("pending" | "confirmed" | "failed" | "cancelled"), paymentStatus ("none" | "pending" | "paid" | "failed" | "refunded"), idempotencyKey, createdAt, cancelledAt?`
- **Service fee:** a simple documented rule (for example 5% rounded, or a fixed fee per ticket) kept as one constant and shown at checkout.
- **Duplicate confirmations** are prevented with `idempotencyKey` (one booking per key; a repeat returns the existing booking).

**`tickets/{ticketRef}`** (document ID **is** the unique ticket reference)
`ticketRef, bookingId, eventId, userId, attendeeName, ticketTypeId, status ("valid" | "used" | "cancelled" | "refunded"), checkedInAt?, checkedInBy?`
- The ticket reference must be unique and hard to guess: for example `EVT-` + 10 random characters from an unambiguous alphabet (no `0/O/1/I`). Create it with a "create-only-if-absent" write/transaction so collisions are impossible. The **QR code encodes only the ticket reference** (no personal data).

**`promoCodes/{codeId}`**
`code (uppercase), eventId (or "ALL"), discountType ("percent" | "fixed"), discountValue, usageLimit, timesUsed, expiresAt, active`

**`waitlist/{entryId}`**
`eventId, userId, ticketTypeId, quantity, joinedAt, status ("waiting" | "offered" | "expired" | "booked" | "left"), offerExpiresAt?`

**`users/{uid}/favorites/{eventId}`** (subcollection, private to the user)
`eventId, savedAt, reminderEnabled (Boolean)`

**`reviews/{eventId}_{uid}`** (document ID enforces one review per user per event)
`eventId, userId, userName, rating (Int 1-5), comment (max 500 chars, optional), createdAt, updatedAt`

**`attendance/{eventId}_{uid}`** (written only by the check-in logic; proves the user attended; used to allow reviews)
`eventId, userId, checkedInAt`

**`usernames/{username}`** `uid, email` (lowercase username as document ID; enables sign-in by username)

**`categories/{categoryId}`** `name, eventCount (Int, number of events using it), createdAt`

**`venues/{venueId}`** `name, address, city, latitude, longitude, capacity (Int), eventCount (Int), createdAt`
- Deleting a category or venue is allowed only when `eventCount == 0`. Event create/edit/delete operations update the counters in the same transaction.

**`notifications/{id}`** `userId, type ("booking_confirmed" | "booking_cancelled" | "event_reminder" | "event_updated" | "event_cancelled" | "account"), title, body, eventId?, bookingId?, read (Boolean), createdAt`
- In-app notification center with read/unread. A user can read and mark as read only their own.

**`activityLogs/{id}`** `userId, userName, role, action ("login" | "logout" | "event_created" | "event_updated" | "event_cancelled" | "booking_created" | "booking_cancelled" | "ticket_checked_in" | "user_role_changed" | "user_suspended" | "user_activated" | "category_changed" | "venue_changed"), entityType, entityId, details?, createdAt`
- **Append-only:** create allowed, update/delete denied for everyone, read only for admins.

**`events/{eventId}/seats/{seatId}`** (lab-optional seat selection) `label, section, row, number, status ("free" | "held" | "booked"), bookingId?`, plus the event field `seatingType ("general" | "assigned")`.

**`paymentRecords/{id}`** (optional payments)
`bookingId, provider, providerReference, amount, currency, verificationStatus ("pending" | "verified" | "failed" | "refunded"), createdAt` — **never** store card numbers, CVV, or full card data.

**`deviceTokens/{tokenId}`** (optional notifications)
`userId, token, platform, updatedAt`

**`chats/{chatId}/messages/{messageId}`** (optional) — only if the students ask for chat.

Create composite indexes when Firestore asks for them (it prints a link in the error); list any created indexes in `PROGRESS.md` and `firestore.indexes.json`.

---

## 6. Firebase setup (HUMAN steps — stop and guide the student)

When Phase 2 starts, give the student this checklist and wait:

1. In the Firebase console, create a project (Analytics optional).
2. **Add an Android app** using the exact `applicationId` from `app/build.gradle(.kts)`.
3. Download `google-services.json` and place it in the `app/` folder.
4. Enable **Authentication → Sign-in method → Email/Password**.
5. Create a **Firestore database** (start in production mode; we will deploy rules).
6. Enable **Storage** for event images (images are required). New Firebase projects may require the **Blaze plan** to use Storage. If the group stays on the free plan, use the documented fallback: resize images to about 800 px, compress as JPEG, and store them as small Base64 strings in the event document (keep each document well under 1 MB), or use image URLs. Record the choice in `PROGRESS.md`.
7. For Cloud Functions you need the **Blaze (pay-as-you-go) plan**. Ask the student which Security Mode (Section 7) they want, and record the answer in `PROGRESS.md`.
8. Install Node.js (LTS) and the Firebase CLI: `npm install -g firebase-tools`, then `firebase login` and `firebase init` (choose Firestore, and Functions if Mode A) inside the project root. Offer to write the config files.
9. Create three test accounts after registration works: one attendee, one organizer, and set one account's `role` to `admin` manually in the Firestore console.

Then you (Claude) add to Gradle: the Google services plugin, the Firebase BoM, and dependencies for `firebase-auth-ktx`, `firebase-firestore-ktx`, `firebase-functions-ktx` (Mode A), `firebase-storage-ktx`, and `kotlinx-coroutines-play-services`. Use the Firebase BoM so versions stay aligned.

---

## 7. Security (non-negotiable)

Never rely on UI visibility as access control.

Required rules:
- Attendees cannot edit other users' events or bookings.
- Organizers can manage **only their own** events (and those events' ticket types, promo codes, analytics, and check-in).
- Ticket references are unique; **check-in cannot reuse a ticket** (atomic check: status must be `valid`, then set `used`).
- **Capacity is validated securely** so seats cannot be oversold, including simultaneous bookings.
- Promo discounts are calculated and applied **server-side/transactionally**, never trusted from the client.
- A booking is only marked paid after payment is verified by a trusted backend. Never store card numbers or CVVs.
- Users cannot change their own `role` or `status`. Only an Admin can change another user's role or activate/suspend accounts. The **first** admin is set manually in the Firestore console; after that, admins assign roles in the app.
- **Suspended users** are blocked from all protected operations (booking, event creation/editing, check-in, reviews, writes) by rules/backend, not only by the UI.
- Sales are rejected for non-published, cancelled, or sold-out events, closed sale windows, passed registration deadlines, and inactive ticket types.
- Notifications are readable only by their owner; the owner may only flip `read`.
- **Activity logs are append-only:** update/delete denied for everyone; read only for Admins.
- Categories and venues are managed only by Admins; deleting one that is in use (`eventCount > 0`) is denied.

### Security Mode A — Cloud Functions (preferred, needs Blaze plan)
Implement these **callable HTTPS functions** (TypeScript, Firebase Admin SDK, Firestore transactions). The Android app calls them through `firebase-functions-ktx`; clients **cannot** write `bookings`, `tickets`, `promoCodes.timesUsed`, `ticketTypes.soldCount`, or `events.bookedCount` directly.
- `createBooking({eventId, ticketTypeId, quantity, promoCode?})` — in one transaction: check the user is not suspended, the event is `published`, the sale window and registration deadline are open, the ticket type is `active` and within its availability period, and seats/type availability exist, enforce the `idempotencyKey` (a repeated key returns the existing booking), validate promo (active, not expired, under limit, matches event), compute total in the server, increment counters, create the booking (unique `bookingRef`, fee, attendee info) + one ticket per seat with unique refs, write the notification and the activity log. Returns booking ID and result status.
- `cancelBooking({bookingId})` — owner only, according to the event's `cancellationPolicyHours`; mark the booking cancelled and tickets `cancelled` (or `refunded` when a refund is recorded), release seats, write the notification and activity log, then trigger waitlist processing.
- `checkInTicket({ticketRef, eventId})` — organizer of that event only; transaction: ticket exists, belongs to event, `status == valid`, then set `used`. Returns a clear result code (`OK`, `ALREADY_USED`, `WRONG_EVENT`, `CANCELLED`, `NOT_FOUND`). On success it also creates `attendance/{eventId}_{userId}` (this is what allows the attendee to review).
- `joinWaitlist` / `leaveWaitlist` / `processWaitlist` (offers freed seats to the next person with an expiry time).
- `submitReview` is optional in Mode A: it may update `events.ratingAverage/ratingCount` after a review is written (or use a Firestore trigger).
- `createWalletPassJwt({ticketRef})` — (Wallet stretch feature) owner of the ticket only; signs the Google Wallet pass JWT with the issuer service account key stored in Secret Manager; never put that key in the app.
- `getEventAnalytics({eventId})` — organizer/admin only; returns sold by type, revenue, check-in rate, waitlist size.
- `getDashboardStats({from?, to?, eventId?})` — role-aware aggregates: Admin (total users, events, bookings, tickets sold, revenue), Organizer (own events, sales, occupancy, check-in stats), Attendee (upcoming events, recent bookings).
- `resolveUsername({username})` — returns the email for username sign-in without exposing the `usernames` collection.
- `setUserRole({uid, role})` / `setUserStatus({uid, status})` — admin only; write the activity log; prevent removing the last admin; optionally set custom claims and revoke tokens for suspended users.
- Notification triggers: on booking confirmed/cancelled and on event update/cancel (fan-out to all ticket holders) create `notifications`; a scheduled function creates event reminders (24 hours before).
- `logActivity` helper used by every function above, and `deleteCategory` / `deleteVenue` (reject when in use).
- (Optional) `createPaymentSession`, `paymentWebhook` (verify provider signature), notification triggers.
- Put function code in `/functions`. Keep secrets in Firebase config/Secret Manager, never in Git.

### Security Mode B — No billing (Firestore transactions + strict rules)
Use only if the group cannot enable Blaze. The app performs a **Firestore transaction (`runTransaction`)** that atomically reads the event/ticket type, checks capacity, writes the booking and tickets, and increments `bookedCount`/`soldCount`. Security rules must restrict these writes (for example, attendees may update an event only if `affectedKeys().hasOnly(['bookedCount'])` and `bookedCount <= capacity`; ticket and booking creation must have `userId == request.auth.uid`, `status == 'confirmed'`, valid quantity). Check-in is also a transaction allowed only for the event's organizer; it also writes the `attendance` marker. In this mode compute `ratingAverage` on the client from a bounded reviews query (for example latest 100). The Google Wallet feature needs a signing backend, so it is **not possible safely in Mode B** — use the fallback in Section 10, item 11. **Document the limitation** in `PROGRESS.md` ("promo codes and capacity enforced by rules + transactions, not a dedicated backend") and recommend Mode A for production. In Mode B the app also writes notifications, activity logs, `eventCount` counters, and `usernames` entries inside transactions/batches under tight rules (organizers may change only `eventCount` on categories/venues); aggregate dashboards on the client from bounded queries. List each of these limitations in `PROGRESS.md`.

### Starter `firestore.rules` (adapt to the chosen mode; test with the Emulator Suite)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    function signedIn() { return request.auth != null; }
    function userDoc() { return get(/databases/$(database)/documents/users/$(request.auth.uid)).data; }
    function isAdmin() { return signedIn() && userDoc().role == 'admin'; }
    function isActive() { return signedIn() && userDoc().status == 'active'; } // add to every write rule so suspended users are blocked
    function isOrganizer() { return signedIn() && userDoc().role == 'organizer' && userDoc().status == 'active'; }
    function ownsEvent(eventId) {
      return signedIn() &&
        get(/databases/$(database)/documents/events/$(eventId)).data.organizerId == request.auth.uid;
    }

    match /users/{uid} {
      allow read: if signedIn() && (request.auth.uid == uid || isAdmin());
      allow create: if signedIn() && request.auth.uid == uid
                    && request.resource.data.role in ['attendee', 'organizer']
                    && request.resource.data.status == 'active';
      allow update: if signedIn() && ((request.auth.uid == uid
                    && request.resource.data.role == resource.data.role
                    && request.resource.data.status == resource.data.status)
                    || isAdmin());
      // Admins can edit info, change role, and activate/suspend; users cannot change their own role/status.
    }

    match /events/{eventId} {
      allow read: if signedIn() && (resource.data.status == 'published'
                    || resource.data.organizerId == request.auth.uid || isAdmin());
      allow create: if isOrganizer() && request.resource.data.organizerId == request.auth.uid
                    && request.resource.data.capacity >= 1;
      allow update, delete: if signedIn() &&
        (resource.data.organizerId == request.auth.uid || isAdmin());
      // Mode A: bookedCount is changed only by Cloud Functions (Admin SDK bypasses rules).
    }

    match /ticketTypes/{typeId} {
      allow read: if signedIn();
      allow create, update, delete: if ownsEvent(request.resource.data.eventId) || isAdmin();
    }

    match /bookings/{bookingId} {
      allow read: if signedIn() && (resource.data.userId == request.auth.uid
                    || ownsEvent(resource.data.eventId) || isAdmin());
      allow write: if false; // Mode A: only Cloud Functions write bookings
    }

    match /tickets/{ref} {
      allow read: if signedIn() && (resource.data.userId == request.auth.uid
                    || ownsEvent(resource.data.eventId) || isAdmin());
      allow write: if false; // Mode A
    }

    match /promoCodes/{id} {
      allow read: if signedIn() && (ownsEvent(resource.data.eventId) || isAdmin());
      allow create: if signedIn() && (ownsEvent(request.resource.data.eventId) || isAdmin());
      allow update, delete: if signedIn() && (ownsEvent(resource.data.eventId) || isAdmin());
      // Attendees never read promo documents; the backend validates codes.
    }

    match /waitlist/{id} {
      allow read: if signedIn() && (resource.data.userId == request.auth.uid
                    || ownsEvent(resource.data.eventId));
      allow write: if false; // Mode A
    }

    match /users/{uid}/favorites/{eventId} {
      allow read, write: if signedIn() && request.auth.uid == uid;
    }

    match /attendance/{id} {
      allow read: if signedIn() && resource.data.userId == request.auth.uid;
      allow write: if false; // Mode A: Cloud Function (check-in). Mode B: only the event organizer inside the check-in transaction.
    }

    match /reviews/{id} {
      allow read: if signedIn();
      allow create: if signedIn()
        && request.resource.data.userId == request.auth.uid
        && id == request.resource.data.eventId + '_' + request.auth.uid
        && request.resource.data.rating is int
        && request.resource.data.rating >= 1 && request.resource.data.rating <= 5
        && exists(/databases/$(database)/documents/attendance/$(id));
      allow update: if signedIn() && resource.data.userId == request.auth.uid
        && request.resource.data.rating is int
        && request.resource.data.rating >= 1 && request.resource.data.rating <= 5;
      allow delete: if signedIn() && (resource.data.userId == request.auth.uid || isAdmin());
    }

    match /usernames/{username} {
      allow get: if true;   // Mode B only: single lookup for username sign-in; never allow list
      allow list: if false;
      allow create: if signedIn() && request.resource.data.uid == request.auth.uid;
      allow update, delete: if false;
    }

    match /categories/{id} {
      allow read: if signedIn();
      allow create: if isAdmin();
      allow update: if isAdmin() || (isOrganizer()
        && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['eventCount']));
      allow delete: if isAdmin() && resource.data.eventCount == 0;
    }

    match /venues/{id} {
      allow read: if signedIn();
      allow create: if isAdmin();
      allow update: if isAdmin() || (isOrganizer()
        && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['eventCount']));
      allow delete: if isAdmin() && resource.data.eventCount == 0;
    }

    match /notifications/{id} {
      allow read: if signedIn() && resource.data.userId == request.auth.uid;
      allow update: if signedIn() && resource.data.userId == request.auth.uid
        && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['read']);
      allow create, delete: if false; // Mode A: backend writes. Mode B: add narrowly scoped creates.
    }

    match /activityLogs/{id} {
      allow read: if isAdmin();
      allow create: if signedIn() && request.resource.data.userId == request.auth.uid;
      allow update, delete: if false; // append-only
    }

    match /paymentRecords/{id} { allow read, write: if false; } // backend only

    match /deviceTokens/{id} {
      allow read, write: if signedIn() && request.resource.data.userId == request.auth.uid;
    }
  }
}
```
Notes for you: in Mode B, replace the `write: if false` lines with the narrowly scoped rules described above. Test every rule with the Emulator Suite using attendee, organizer, admin, and unauthenticated users, including attempted attacks (editing another user's booking, changing own role, overselling, reusing a ticket).

---

## 8. Feature requirements (details)

**Auth and roles.** Welcome, Register (name, **username**, email, password, choose Attendee or Organizer; Admin accounts come from an existing admin changing a role), Login with **email or username** + password (see the username note in Section 5), Reset password, Logout. Create `users/{uid}` (status `active`) and `usernames/{username}` on registration; usernames are unique and lowercase. Persist the session across restarts. A **suspended** account that signs in sees the "Account suspended" screen and can only sign out. Record `login`/`logout` in the activity log. Show friendly messages for wrong password, duplicate email/username, weak password, and network failure. Every screen is role-guarded.

**Event discovery.** Home list (published, upcoming events, sorted by date) with `EventCard` (poster, title, date, venue, price from, seats left, rating, distance if known, heart icon). Home also shows **Recommended for you** and **Nearby** sections once those extras exist. Browse **upcoming, published** events only. Search by title or keyword (title, description, category). Filters: **date**, **category**, **location/city**, and **price** (min/max or free). Handle empty results. Firestore has no full-text search: implement simple client-side filtering on a bounded query, and say so in `PROGRESS.md`.

**Event details.** Description, date/time, venue (+ map if enabled), price per ticket type with available / sold-out counts, schedule, seats left, organizer information (name and contact), average rating and recent reviews, a favorite (heart) button, **Book** button (disabled with a clear label if sold out or cancelled → show **Join waitlist** when full), **Add to calendar**, **Share**.

**Event management (organizer).** Create, edit, and list/manage **my events**. Fields: title, description, date/time, **category and venue chosen from the admin-managed lists**, one or more **images**, capacity (cannot exceed the venue capacity), **ticket sale start/end dates**, **registration deadline**, cancellation policy (hours before start), and visibility **Draft / Published / Cancelled**. Validate: start in the future, sale end before event start, deadline before start, capacity >= 1. An organizer sees only their own events. **Block new ticket sales** for cancelled or sold-out events, outside the sale window, and after the registration deadline (the UI explains why; backend/rules enforce it). Cancelling an event cancels its bookings and tickets, notifies ticket holders, and logs the action.

**Ticket types and inventory (organizer).** Per event: create types (General Admission, VIP, Student, ...) with **price, quantity limit, and availability period**; edit or **deactivate** a type before sales when permitted (Section 5 rules); show **available** and **sold-out** quantities per type; the sum of type limits cannot exceed the event capacity; sales can never exceed a type's limit or the event capacity.

**Booking and checkout (attendee).** Select event, ticket type, and quantity (max per booking, for example 10) → review screen with **booking details, subtotal, service fee, discount (if a promo code is used), and total** → enter or confirm **attendee information** (name, email, phone) → confirm. The result screen shows a **unique booking reference** and the result **Confirmed / Pending / Failed / Cancelled**, plus ticket references for confirmed bookings. Capacity is validated server-side/transactionally. **Prevent duplicate confirmations** (disable the button while loading and send a per-checkout `idempotencyKey`; a repeated key returns the same booking) and **overselling**. Handle: sold out between viewing and booking, cancelled event, closed sale window, invalid promo, and no internet (show Failed with a retry).

**My Tickets and booking history.** Tabs Upcoming / Previous / Cancelled. Tapping a booking shows its tickets. The ticket screen shows event, ticket type, date, attendee, **unique reference**, a **QR code** (or at minimum the ticket identifier), and the current status **Valid / Used / Cancelled / Refunded**. Attendees can **cancel a booking according to the event cancellation policy** (button disabled with an explanation after the deadline). Tickets can always be opened from history without making a new booking.

**Organizer check-in.** Screen with QR scanner (CameraX + ML Kit) and a **manual reference entry** fallback. Show big, clear results: ✅ valid / ⚠️ already used / ❌ wrong event / ❌ cancelled / ❌ not found. Show **check-in totals** (checked in / total tickets) and an **attendee list** (name, ticket type, status, check-in time) with search and a status filter. Validate every scan against the selected event.

**User management (admin).** List users with role and status; search/filter; open a user to edit name/email info, **activate or suspend** the account, **assign or update the role** (Admin / Organizer / Attendee), and view that user's events (organizer) or bookings (attendee) where authorized. Suspended users are blocked from protected operations (Section 7). Log `user_role_changed`, `user_suspended`, and `user_activated`. An admin cannot demote or suspend themselves if they are the last admin.

**Category and venue management (admin).** Add, edit, and delete **categories** (Music, Sports, Conferences, ...) and **venues** (name, address, location (lat/lng), capacity). Event forms select from these lists. **Deleting a category or venue that is in use is blocked** (maintain `eventCount`, Section 5) with a clear message.

**Dashboards and reports.** One `dashboard` route with a role-specific view. **Admin:** total users, events, bookings, tickets sold, revenue summary. **Organizer:** own events, ticket sales, occupancy (sold / capacity), check-in statistics. **Attendee:** upcoming events and recent bookings. **Filters:** date range and event. Show **charts and tables** (sales over time, occupancy per event, check-in rate). Exclude cancelled/refunded bookings from revenue. Use backend aggregation (Mode A) or bounded queries with client-side aggregation (Mode B).

**Notifications (in-app).** Notification center screen plus a bell with an unread badge. Create notifications when a booking is confirmed or cancelled, when an event is updated or cancelled (to all ticket holders), and as **reminders before an upcoming event** (24 hours before; WorkManager locally and/or a scheduled Cloud Function). Show read/unread status, mark as read on tap, and "mark all as read". Push (FCM) is optional on top.

**Activity log (admin).** Record sign-in, event creation/editing/cancelling, ticket booking, cancellation, check-in, and admin actions through one `ActivityLogger` helper. Admin screen with filters by **user, date range, and action type**. Logs are **append-only** (rules deny update/delete) and readable only by admins.

**Sample data and demo (required).** Provide sample data and demonstrate the main workflows. Build a seed tool: (a) preferred, a Node script in `/tools/seed` using the Firebase Admin SDK with a service-account key kept out of Git, or (b) a debug-only "Load sample data" button visible only to admins in debug builds. Seed: 1 admin, 2 organizers, 4 attendees (one suspended), at least 6 categories, 4 venues, 10+ events (draft, published, cancelled, sold out, past), several ticket types per event (one inactive), bookings in each status, tickets Valid/Used/Cancelled/Refunded, read and unread notifications, and activity logs. Mark seeded documents with `isSample: true` so they can be removed. List the demo accounts and a demo script in `README.md`.

**Profile and settings.** Show name/email/role, change name, dark-mode toggle (DataStore), logout.

---

## 9. UI/UX guidelines

- Material 3, consistent spacing (8dp grid), one primary color + accent, rounded cards, readable typography.
- Reusable components in `ui/components`. Previews (`@Preview`) for key components.
- Forms: inline validation messages, `imePadding`, keyboard actions, password visibility toggle.
- Use `LazyColumn` for lists with stable keys. Show shimmer or progress indicators while loading.
- Support portrait first; ensure layouts survive rotation and small/large screens.
- Use string resources (`strings.xml`) instead of hard-coded text so a second language can be added later.
- Never block the main thread; all Firebase calls via coroutines.

---

## 10. Extra features (build only after ALL required lab features in Section 1A work, in this order)

1. **QR code tickets and scanning.** Generate a QR from the ticket reference (ZXing); scan with CameraX + ML Kit in the check-in screen; request camera permission with a rationale; handle denied permission; manual entry fallback. Server/transaction check-in as in Section 7.
2. **Cancellations and refunds.** Attendee can cancel before a cancellation deadline (default: 24 hours before start; make it a constant). Seats are released; tickets become `cancelled`. Organizer can cancel an event: all its bookings become cancelled, attendees get an in-app notification. If payments exist, request the refund via the backend; otherwise record `refunded`/`none` status only.
3. **Waitlist.** "Join waitlist" when full. First-come-first-served. When a seat is freed, the next entry gets an **offer with an expiry time** (for example 24 hours); an expired offer moves to the next person. Show offers in My Bookings.
4. **Promo codes** (ticket types and inventory are already required in Section 1A). Organizer creates promo codes (percent or fixed, limit, expiry). Validate and apply server-side/transactionally. Handle expired, exhausted, wrong-event, and sold-out cases.
5. **Per-event analytics** (extends the required dashboards). Per event: tickets sold by type, revenue, remaining capacity, check-in rate, waitlist size. Summary cards + simple charts. Organizer/admin only. Exclude cancelled bookings from revenue.
6. **Add to calendar and share.** `Intent.ACTION_INSERT` to `CalendarContract.Events.CONTENT_URI` (title, begin/end, location) — no calendar permission needed. Share sheet via `Intent.ACTION_SEND` with title, date, venue.
7. **Favorites and reminders.** Heart button on `EventCard` and event details; `favorites` screen listing saved events; stored in `users/{uid}/favorites`. Reminders use **WorkManager** local notifications (no server): remind 24 hours before a favorited or booked event starts. Create a notification channel, request `POST_NOTIFICATIONS` on Android 13+, reschedule if the event time changes, cancel the reminder if the user unfavorites, the booking is cancelled, or the event is cancelled. Per-event reminder toggle. Does not need FCM.
8. **Ratings and reviews.** Only attendees with a **checked-in ticket** (an `attendance` doc) may review, and only after the event ends. 1-5 stars + optional comment (max 500 chars). One review per user per event, editable. Show average rating and count on `EventCard` and event details; reviews screen lists newest first. Organizers can read but not edit/delete; admins can remove abusive reviews. Keep `ratingAverage/ratingCount` in sync (Section 7 modes).
9. **Nearby events.** Ask for coarse/fine location permission with a rationale (`FusedLocationProviderClient`). Organizer provides the venue coordinates (use Android `Geocoder` on the address, or manual lat/lng entry) and the app stores `latitude`, `longitude`, and `geohash`. Query by geohash bounds (GeoFire utils) for a chosen radius (chips: 5/10/25/50 km), then filter by exact distance and sort by it. Show distance on cards. If permission is denied or location is off, fall back to the city filter with a clear message. This does **not** need the Maps SDK.
10. **Recommendations.** A "Recommended for you" row on Home, computed on the device by a pure-Kotlin `RecommendationEngine` (unit tested). Score upcoming published events by: category affinity (counts from the user's bookings, favorites, and reviews rated 4-5), plus small boosts for nearby distance, popularity (`bookedCount/capacity`), and `ratingAverage`. Exclude events the user already booked, cancelled events, and past events. Cold start (new user): show popular upcoming events. Show a short reason such as "Because you booked Music events". No machine learning needed.
11. **Google Wallet pass (stretch, last).** Add an "Add to Google Wallet" button on the ticket screen, using an Event Ticket pass whose barcode is the ticket reference. This needs human setup (Google Pay & Wallet Console issuer account, a service account, and the Wallet API enabled) and a signing backend (`createWalletPassJwt`, Mode A only; the private key must never be in the app). **Ask the students before starting.** If it is not feasible (Mode B, no issuer access, or time), implement the fallback instead: "Save ticket as image" (render the ticket composable to a bitmap and save via `MediaStore`) or export a PDF.

### Optional features (last)
- **Push notifications (FCM)** on top of the required in-app notifications: booking confirmation, reminders (for example 24h before), event-change/cancellation alerts; `POST_NOTIFICATIONS` permission on Android 13+; token stored in `deviceTokens`; backend decides recipients; handle taps to open the right screen.
- **Venue maps:** Maps Compose marker on event details; API key via `local.properties` + manifest placeholder, restricted in Google Cloud; do not commit the key.
- **Offline access:** Firestore offline persistence for events/tickets; optionally Room cache of the user's tickets so they open without internet; show an offline banner.
- **Online payments:** sandbox only. Backend creates the payment session; the app opens the provider flow; the backend verifies via signed webhook; only then mark `paid`. Store provider reference, never card data. Confirm provider eligibility for the students' country first.
- **Payment processing and refund tracking (lab optional):** see Online payments; record refunds in `paymentRecords` and set booking `paymentStatus` and ticket status `refunded`.
- **Seat selection (lab optional):** events with `seatingType = assigned` have a `seats` subcollection; the booking flow shows a seat map; seats are reserved/held and booked inside a transaction (no double booking); cancelling releases the seats.
- **Export reports (lab optional):** export ticket and attendance reports as CSV (organizer: their events; admin: all) and open the Android share sheet.
- **Chat:** only if requested.

---

## 11. MVP and required-feature milestones

**Milestone 1 — MVP (must be fully working and tested):**
- Login and registration (email or username)
- Attendee, organizer, and admin roles (the first admin set in the console)
- Event creation and browsing
- Event details
- Ticket booking with secure capacity validation
- Booking history
- Unique ticket references
- Basic organizer check-in (manual reference entry is enough for the MVP; QR scanning is an extra)
- Essential error handling and tests

**Milestone 2 — Remaining required lab features (Section 1A):** ticket types and inventory; sale windows and registration deadlines; price filter; checkout with fees and attendee info; booking result statuses and idempotency; cancellation policy; ticket statuses Valid/Used/Cancelled/Refunded; attendee lists and check-in totals; user management with suspension; category and venue management; dashboards and reports for all three roles; in-app notifications; activity log; sample data and demo script.

Only after Milestone 2 start the extra features (Section 10) and optional features.

---

## 12. Build phases (follow in order; verify each with a Gradle build + tests)

**Phase 0 — Inspect and prepare.** Read the project, report `applicationId`, SDK versions, Kotlin/AGP/Compose versions. Confirm there is **no Java source**. Create `PROGRESS.md`, `.gitignore` additions, and the package folders. Ask which Security Mode (A or B) the group will use, and whether Firebase Storage is available (Section 6).

**Phase 1 — Requirements and screen design.** Create `docs/screens.md` listing every screen from Section 1A, its route, state fields, and navigation. (Wireframes are done by the students in Figma; do not block on them.)

**Phase 2 — Gradle and Firebase configuration.** Add dependencies (version catalog), Google services plugin, Firebase BoM, core desugaring if needed, and `AppContainer`. Guide the human Firebase steps (Section 6). Verify the app still builds and starts.

**Phase 3 — Theme, shared components, navigation.** Material 3 theme (light/dark), shared composables, `Routes`, `AppNavHost`, bottom bars per role, notification bell placeholder, placeholder screens for every route. Acceptance: every route reachable; app runs.

**Phase 4 — Authentication, roles, and suspension (Screen 1).** `AuthRepository`, Register (with username) / Login by email or username / Reset / Logout, `users` and `usernames` creation, session restore, role-based start destination and route guards, suspended-account screen, `ActivityLogger` (login/logout). Tests: validators, username resolution, ViewModels (fake repository).

**Phase 5 — Categories, venues, and events (Screens 2 and 9).** Admin category and venue management with in-use protection (`eventCount`). Organizer event create/edit/list with category, venue, images, capacity <= venue capacity, sale dates, registration deadline, and Draft/Published/Cancelled. Start the **seed tool** now and extend it each phase. Tests: validation rules, in-use deletion.

**Phase 6 — Ticket types, inventory, and discovery (Screens 3 and 4).** Ticket type screen (price, limit, availability period, deactivate, available/sold-out). Attendee browse (upcoming + published), keyword search, filters (date, category, location, price), event details with organizer info. Tests: filter logic, inventory rules.

**Phase 7 — Booking and checkout (Screen 5).** Capacity-safe booking per the chosen Security Mode: ticket type + quantity, review with fee and total, attendee information, unique booking reference, idempotency, result status Confirmed/Pending/Failed/Cancelled, sale-block rules. Tests: price/fee calculation, sale windows, duplicate confirmation, sold-out handling. Include a documented **concurrency test** (two accounts booking the last seat).

**Phase 8 — My Tickets, history, and cancellation (Screen 6).** Upcoming/previous bookings, ticket screen with reference and status Valid/Used/Cancelled/Refunded, QR code if time allows (otherwise ticket identifier), cancellation per `cancellationPolicyHours`, open e-ticket from history. Tests: cancellation window, seat release.

**Phase 9 — Check-in and attendee lists (Screen 7).** Organizer check-in by ticket identifier (QR scanner comes in the extras), atomic anti-reuse, validation against the event, totals and attendee list. Tests: reuse rejection, wrong-event rejection, cancelled ticket rejection.

**Phase 10 — User management (Screen 8).** Admin user list, edit info, activate/suspend, change role, view a user's events/bookings, last-admin protection, suspended users blocked everywhere. Tests: rules for role/status changes, suspended access.

**Phase 11 — In-app notifications (Screen 11).** Notification center, unread badge, read/unread, creation on booking confirmed/cancelled and event updated/cancelled, reminders 24 hours before events. Tests: fan-out to ticket holders, private access.

**Phase 12 — Dashboards, reports, and activity log (Screens 10 and 12).** Role-specific dashboards with date-range and event filters, charts and tables; admin activity log with user/date/action filters; make sure every important action writes a log. Tests: aggregation, log immutability and admin-only read.

**Phase 13 — Extra features** in the order of Section 10 (items 1-6 first; items 7-11 are lower priority and are built only if time allows). Complete each one (code + rules + tests + `PROGRESS.md` note) before starting the next. Stop and ask if time looks short; drop unfinished extras rather than leave them half-working.

**Phase 14 — Lab-optional and optional features** if the students want them: payments and refund tracking, seat selection, report export, push notifications, maps, offline.

**Phase 15 — Sample data, testing, hardening, and demo prep.** Finalize the seed tool and sample data (Section 8). Run full unit tests, add Compose UI tests for the key journeys (register → browse → book → see ticket; organizer create event → check in; admin suspend user), test Security Rules in the Emulator Suite, review for leaked secrets, write `README.md` (prerequisites, Firebase setup, how to run, demo accounts, demo script, known limitations), and produce a debug APK path. Verify every row of Section 1A against `docs/test-results.md`.

---

## 13. Testing requirements

**Unit tests (JUnit + MockK + Turbine):** validators, price/discount calculation, filter logic, ViewModels with fake repositories, ticket-reference format, booking-reference format, fee calculation, sale-window/registration-deadline/inventory rules, idempotency, username resolution, waitlist ordering logic, `RecommendationEngine` scoring, distance/geohash bounds, review eligibility and rating average, reminder time calculation.

**Compose UI tests:** login form validation, event list rendering/empty state, booking flow happy path, sold-out state.

**Security rules / backend tests (Emulator Suite):** attendee cannot edit another user's event or booking; organizer cannot modify another organizer's event; user cannot change own role; client cannot write bookings/tickets directly (Mode A); reviews rejected without an `attendance` doc; favorites readable only by their owner; suspended users blocked from protected operations; only admins change roles/status; activity logs cannot be updated or deleted; notifications private to their owner; categories/venues in use cannot be deleted; sales rejected for cancelled, sold-out, or closed-window events; promo codes validated; ticket reuse rejected; capacity cannot be exceeded under concurrent requests.

**Manual test checklist** (record results in `docs/test-results.md`):
- Account registration and login (valid, invalid, duplicate, reset)
- Searching and filtering events
- Creating and editing events
- Booking tickets when seats are available
- Fully booked and cancelled events
- Preventing unauthorized booking changes
- Rejecting invalid or previously used tickets
- Failed payments (if payments are implemented)
- In-app notifications (booking confirmed/cancelled, reminder, event updated/cancelled; read/unread); push and offline behavior (if implemented)
- Lab requirements: sign-in by email and by username; suspended user blocked; each role sees only its own screens; sale window closed and registration deadline passed; duplicate confirmation prevented; cancellation allowed only within the policy; attendee list and check-in totals; user role change and suspension; category/venue deletion blocked when in use; dashboards with date-range/event filters for all three roles; activity log filters by user/date/action
- Extras: promo codes (valid/expired/exhausted), ticket-type sold out, cancellation + seat release, waitlist offer + expiry, QR scan (camera denied, damaged code, wrong event), analytics access by non-organizer rejected, add-to-calendar/share
- More extras: favorites and reminders (fires once, cancelled when unfavorited or booking cancelled), review allowed only after check-in and only once per event, nearby with location permission denied, recommendations cold start, Wallet pass or its fallback export

---

## 14. Git workflow

- Branches: `feature/auth`, `feature/events`, `feature/booking`, `feature/tickets-checkin`, `feature/qr`, `feature/cancellations`, `feature/waitlist`, `feature/promo-types`, `feature/analytics`, `feature/catalog`, `feature/user-management`, `feature/notifications`, `feature/activity-log`, `feature/dashboards`, `feature/seed-data`, `feature/calendar-share`, `feature/favorites-reminders`, `feature/reviews`, `feature/nearby`, `feature/recommendations`, `feature/wallet`.
- Small commits, imperative messages (for example "Add booking repository and ViewModel").
- One pull request per feature, reviewed by a teammate. Keep `main` buildable.
- Suggested ownership (adjustable): **Student 1** setup/navigation/shared UI/integration + dashboards and reports + in-app notifications + calendar/share + favorites and reminders; **Student 2** auth/roles/usernames/suspension + user management + activity log + check-in/attendee lists + QR scanner + ratings and reviews + Wallet pass (or fallback); **Student 3** events + categories and venues + ticket types and inventory + discovery/search/filters + promo codes + nearby events + recommendations; **Student 4** booking and checkout + My Tickets/history + cancellations + waitlist + QR generation + sample data and testing coordination.

---

## 15. Definition of done (every feature)

- Works on the emulator/device with no known blocker; `./gradlew assembleDebug` and unit tests pass.
- Loading, empty, error, and permission-denied states are handled.
- Input validation and access control are implemented **and tested**.
- No secrets in code or Git history.
- `PROGRESS.md` updated; README updated if setup or behavior changed.

---

## 16. Common pitfalls to avoid

- Forgetting `google-services.json` (build or runtime crash) — stop and ask the student.
- Firestore data classes without default values (deserialization failures).
- Storing money as `Double`.
- Letting the client set `status`, `bookedCount`, `timesUsed`, or `role`.
- Updating UI state from the wrong thread, or collecting flows without lifecycle awareness (`collectAsStateWithLifecycle`).
- Hard-coding the package name, API keys, or strings.
- Leaving listeners running after the screen closes.
- Starting extras before the MVP is stable.
- Putting the Google Wallet service-account key (or any private key) in the app.
- Asking for location on app start without a reason; request it only when the user opens Nearby.
- Leaving WorkManager reminders scheduled after an event or booking is cancelled.
- Creating Java files (the lab forbids Java), or building extras before Section 1A is complete.
- Forgetting that suspended users must be blocked by rules/backend, not just hidden in the UI.
- Using the old ticket status `checkedIn`; the required statuses are `valid`, `used`, `cancelled`, `refunded`.
- Deleting a category or venue without checking `eventCount`, or letting event capacity exceed venue capacity.

---

## 17. First message to send me (the students will paste this)

> Read `CLAUDE.md` fully. Start **Phase 0**: inspect the project, report the applicationId and tool versions, create `PROGRESS.md`, and ask me which Security Mode (A or B) we will use. Do not write feature code yet.
