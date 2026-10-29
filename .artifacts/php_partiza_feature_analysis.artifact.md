# Partizza PHP Project Feature Integration Analysis

> [!NOTE]
> This analysis compares the **Partizza PHP Web Project** (`C:\xampp\htdocs\php partiza`) against our **Android Event & Ticket Management System** (`Eventandticketmanagementsystem`), identifying key features from Partizza to integrate into both user and admin portals.

---

## 1. Key Features Found in Partizza PHP Project

### A. Admin Portal Features
1. **Promo Codes Management** (`admin/promo_codes.php`): Create, toggle, delete, and manage discount promo codes for ticket purchases.
2. **Door Check-In & Search** (`admin/door.php`, `admin/door_checkin_ajax.php`): Real-time door check-in scanner and quick search for attendee door verification.
3. **FAQs & Inquiries** (`admin/faqs.php`, `admin/inquiries.php`): Managing frequently asked questions and customer inquiries/support messages.
4. **Gallery & Media Management** (`admin/gallery.php`): Managing event photo galleries, featuring/unfeaturing media.
5. **Testimonials & Community** (`admin/testimonials.php`, `admin/community.php`): Moderating user testimonials and community posts.
6. **Newsletter & Waitlist Broadcasts** (`admin/newsletter_send.php`, `admin/waitlist_notify.php`): Sending email newsletters and notifying waitlisted users when seats open up.
7. **Backups & Reports** (`admin/backup.php`, `admin/reports.php`, `admin/export.php`): Database backups, sales reports, and CSV data exports.

### B. User / Attendee Portal Features
1. **Event Calendar View** (`calendar.php`): Interactive calendar view of upcoming events.
2. **Event Photo Gallery & Media Download** (`gallery.php`, `media_download.php`): Browsing event galleries and downloading media assets.
3. **Waitlist Joining** (`waitlist_join.php`): Joining a waitlist for sold-out events.
4. **FAQs & Help Center**: Accessing frequently asked questions and submitting inquiries.
5. **Promo Codes at Checkout** (`checkout.php`): Applying promo codes during ticket booking.
6. **Multi-Language Support** (`lang/` - English, Arabic, Russian, Turkish).

---

## 2. Proposed Integration Plan for Android App

### Phase 1: Promo Codes & Checkout Discounts
- **Data Model**: Add `PromoCode` model (code, discountPercentage, maxUses, expiresAt).
- **Admin**: Add Promo Codes screen in Admin Portal.
- **User Booking**: Add promo code input field in `BookingScreen` with instant discount calculation.

### Phase 2: Event Gallery & Media
- **Data Model**: Add `GalleryItem` model.
- **User**: Add Event Gallery tab/screen for event photos.
- **Admin**: Add Gallery management in Admin Portal.

### Phase 3: Waitlist System
- **Data Model**: Add `WaitlistEntry` model.
- **User**: Option to join waitlist when an event or ticket type is sold out.
- **Organizer/Admin**: Waitlist notification tools.

### Phase 4: FAQs & Customer Inquiries
- **Data Model**: Add `Faq` and `Inquiry` models.
- **User**: Help & Support / FAQ screen with inquiry submission.
- **Admin**: Inquiry management and FAQ editor.
