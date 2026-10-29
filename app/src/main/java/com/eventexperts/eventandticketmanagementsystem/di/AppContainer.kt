package com.eventexperts.eventandticketmanagementsystem.di

import com.eventexperts.eventandticketmanagementsystem.data.repository.ActivityLogRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.AuthRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.BookingRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.CatalogRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.CheckInRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.EventRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.NotificationRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.PromoCodeRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.ReviewAndFavoritesRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.SupportGalleryRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.TicketTypeRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Manual Dependency Injection Container.
 * Application-scoped dependencies are lazily initialized here.
 */
class AppContainer {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val activityLogRepository: ActivityLogRepository by lazy {
        ActivityLogRepository(firestore)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(auth, firestore, activityLogRepository)
    }

    val catalogRepository: CatalogRepository by lazy {
        CatalogRepository(firestore, activityLogRepository)
    }

    val eventRepository: EventRepository by lazy {
        EventRepository(firestore, activityLogRepository)
    }

    val ticketTypeRepository: TicketTypeRepository by lazy {
        TicketTypeRepository(firestore)
    }

    val bookingRepository: BookingRepository by lazy {
        BookingRepository(firestore, activityLogRepository)
    }

    val checkInRepository: CheckInRepository by lazy {
        CheckInRepository(firestore, activityLogRepository)
    }

    val userRepository: UserRepository by lazy {
        UserRepository(firestore, activityLogRepository)
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepository(firestore)
    }

    val promoCodeRepository: PromoCodeRepository by lazy {
        PromoCodeRepository(firestore)
    }

    val reviewAndFavoritesRepository: ReviewAndFavoritesRepository by lazy {
        ReviewAndFavoritesRepository(firestore)
    }

    val supportGalleryRepository: SupportGalleryRepository by lazy {
        SupportGalleryRepository(firestore)
    }
}
