package com.eventexperts.eventandticketmanagementsystem.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun passwordValidation_requiresAtLeast8Chars() {
        assertFalse(Validators.isValidPassword("short"))
        assertFalse(Validators.isValidPassword("1234567"))
        assertTrue(Validators.isValidPassword("12345678"))
        assertTrue(Validators.isValidPassword("secure_password_123"))
    }

    @Test
    fun usernameValidation_requiresAtLeast3AlphanumericChars() {
        assertFalse(Validators.isValidUsername("a"))
        assertFalse(Validators.isValidUsername("ab"))
        assertFalse(Validators.isValidUsername("user@name"))
        assertTrue(Validators.isValidUsername("john_doe"))
        assertTrue(Validators.isValidUsername("organizer123"))
    }

    @Test
    fun nameValidation_requiresAtLeast2Chars() {
        assertFalse(Validators.isValidName(""))
        assertFalse(Validators.isValidName("a"))
        assertTrue(Validators.isValidName("John Doe"))
    }
}
