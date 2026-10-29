package com.eventexperts.eventandticketmanagementsystem.util

object Validators {
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    fun isValidUsername(username: String): Boolean {
        val trimmed = username.trim().lowercase()
        return trimmed.length >= 3 && trimmed.all { it.isLetterOrDigit() || it == '_' }
    }

    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2
    }
}
