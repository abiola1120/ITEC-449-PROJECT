package com.eventexperts.eventandticketmanagementsystem.ui.home

import app.cash.turbine.test
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.Category
import com.eventexperts.eventandticketmanagementsystem.data.model.Venue
import com.eventexperts.eventandticketmanagementsystem.data.repository.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private class FakeEventRepository(private val events: List<Event>) : EventRepository {
    override fun observePublishedEvents(): Flow<List<Event>> = flowOf(events)
    override fun observeOrganizerEvents(organizerId: String): Flow<List<Event>> = flowOf(emptyList())
    override suspend fun getEventById(eventId: String): Event? = events.find { it.eventId == eventId }
    override suspend fun saveEvent(
        existingEventId: String?, title: String, description: String, categoryId: String, venueId: String,
        capacity: Int, basePrice: Int, startAt: com.google.firebase.Timestamp?, endAt: com.google.firebase.Timestamp?,
        saleStartAt: com.google.firebase.Timestamp?, saleEndAt: com.google.firebase.Timestamp?, registrationDeadline: com.google.firebase.Timestamp?,
        cancellationPolicyHours: Int, status: com.eventexperts.eventandticketmanagementsystem.data.model.EventStatus,
        imageUrls: List<String>, user: com.eventexperts.eventandticketmanagementsystem.data.model.User
    ): com.eventexperts.eventandticketmanagementsystem.util.Result<Event> = com.eventexperts.eventandticketmanagementsystem.util.Result.Success(events.first())
    override suspend fun cancelEvent(eventId: String, user: com.eventexperts.eventandticketmanagementsystem.data.model.User) = com.eventexperts.eventandticketmanagementsystem.util.Result.Success(Unit)
}

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun homeViewModel_emitsPublishedEvents() = runTest {
        val mockEvents = listOf(
            Event(eventId = "e1", title = "Music Concert", description = "Live concert", city = "New York")
        )
        val fakeRepo = FakeEventRepository(mockEvents)

        val viewModel = HomeViewModel(fakeRepo)

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(true, initialState.isLoading)

            val loadedState = awaitItem()
            assertEquals(false, loadedState.isLoading)
            assertEquals(1, loadedState.publishedEvents.size)
            assertEquals("Music Concert", loadedState.publishedEvents[0].title)
        }
    }
}
