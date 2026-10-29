package com.eventexperts.eventandticketmanagementsystem.util

import com.eventexperts.eventandticketmanagementsystem.data.model.Category
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.EventStatus
import com.eventexperts.eventandticketmanagementsystem.data.model.Venue
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object SeedDataTool {

    suspend fun seedSampleDataIfEmpty(firestore: FirebaseFirestore) {
        try {
            val categoriesSnapshot = firestore.collection("categories").get().await()
            if (categoriesSnapshot.isEmpty) {
                seedCategoriesAndVenues(firestore)
            }
        } catch (_: Exception) {
            // Ignore seeding errors on fresh startup
        }
    }

    private suspend fun seedCategoriesAndVenues(firestore: FirebaseFirestore) {
        val categories = listOf(
            Category(id = "cat_music", name = "Music & Concerts", eventCount = 2, createdAt = Timestamp.now()),
            Category(id = "cat_tech", name = "Tech & Innovation", eventCount = 1, createdAt = Timestamp.now()),
            Category(id = "cat_sports", name = "Sports & Fitness", eventCount = 1, createdAt = Timestamp.now()),
            Category(id = "cat_arts", name = "Arts & Theater", eventCount = 0, createdAt = Timestamp.now()),
            Category(id = "cat_food", name = "Food & Drink", eventCount = 0, createdAt = Timestamp.now())
        )

        val venues = listOf(
            Venue(id = "ven_arena", name = "City Main Arena", address = "100 Grand Ave", city = "Metropolis", capacity = 5000, eventCount = 2, createdAt = Timestamp.now()),
            Venue(id = "ven_hall", name = "Grand Community Hall", address = "45 Park St", city = "Metropolis", capacity = 500, eventCount = 1, createdAt = Timestamp.now()),
            Venue(id = "ven_tech", name = "Innovation Tech Center", address = "12 Cyber Way", city = "Tech City", capacity = 300, eventCount = 1, createdAt = Timestamp.now())
        )

        val batch = firestore.batch()

        categories.forEach { cat ->
            batch.set(firestore.collection("categories").document(cat.id), cat)
        }

        venues.forEach { ven ->
            batch.set(firestore.collection("venues").document(ven.id), ven)
        }

        val nowSeconds = Timestamp.now().seconds
        val oneDay = 86400L

        val events = listOf(
            Event(
                eventId = "evt_music_festival",
                title = "Summer Music Festival 2025",
                description = "Experience live performances from top indie and pop artists under the stars.",
                categoryId = "cat_music",
                categoryName = "Music & Concerts",
                organizerId = "seed_organizer_1",
                organizerName = "Event Experts Org",
                startAt = Timestamp(nowSeconds + (7 * oneDay), 0),
                venueId = "ven_arena",
                venueName = "City Main Arena",
                address = "100 Grand Ave",
                city = "Metropolis",
                capacity = 2000,
                bookedCount = 150,
                basePrice = 4999, // $49.99
                status = EventStatus.PUBLISHED.value,
                isSample = true,
                createdAt = Timestamp.now()
            ),
            Event(
                eventId = "evt_tech_summit",
                title = "Global AI & Mobile Tech Summit",
                description = "Join industry leaders exploring Jetpack Compose, Kotlin, and GenAI on Android.",
                categoryId = "cat_tech",
                categoryName = "Tech & Innovation",
                organizerId = "seed_organizer_1",
                organizerName = "Event Experts Org",
                startAt = Timestamp(nowSeconds + (14 * oneDay), 0),
                venueId = "ven_tech",
                venueName = "Innovation Tech Center",
                address = "12 Cyber Way",
                city = "Tech City",
                capacity = 250,
                bookedCount = 80,
                basePrice = 2999, // $29.99
                status = EventStatus.PUBLISHED.value,
                isSample = true,
                createdAt = Timestamp.now()
            )
        )

        events.forEach { evt ->
            batch.set(firestore.collection("events").document(evt.eventId), evt)
        }

        batch.commit().await()
    }
}
