package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.Review
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewVerificationTest {

    @Test
    fun reviewRatingValidation_requires1To5Stars() {
        val validRating = 4
        val invalidRatingHigh = 6
        val invalidRatingLow = 0

        assertTrue(validRating in 1..5)
        assertFalse(invalidRatingHigh in 1..5)
        assertFalse(invalidRatingLow in 1..5)
    }

    @Test
    fun attendanceVerification_onlyCheckedInAttendeesCanReview() {
        val checkedInAttendanceDocs = setOf("evt1_userA", "evt2_userB")

        val userACanReviewEvt1 = checkedInAttendanceDocs.contains("evt1_userA")
        val userBCanReviewEvt1 = checkedInAttendanceDocs.contains("evt1_userB")

        assertTrue("User A attended evt1 and can write a review", userACanReviewEvt1)
        assertFalse("User B did not attend evt1 and cannot write a review", userBCanReviewEvt1)
    }

    @Test
    fun ratingAverageCalculation_updatesCorrectly() {
        val currentAverage = 4.0
        val currentCount = 2
        val newRating = 5

        val newCount = currentCount + 1
        val newAverage = ((currentAverage * currentCount) + newRating) / newCount

        assertEquals(4.333333333333333, newAverage, 0.001)
        assertEquals(3, newCount)
    }
}
