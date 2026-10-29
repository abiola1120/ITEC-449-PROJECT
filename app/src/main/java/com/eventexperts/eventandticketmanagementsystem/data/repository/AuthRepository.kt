package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.data.model.UserRole
import com.eventexperts.eventandticketmanagementsystem.data.model.UserStatus
import com.eventexperts.eventandticketmanagementsystem.data.model.UsernameDoc
import com.eventexperts.eventandticketmanagementsystem.util.Result
import com.eventexperts.eventandticketmanagementsystem.util.Validators
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val activityLogRepository: ActivityLogRepository
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isSessionInitialized = MutableStateFlow(false)
    val isSessionInitialized: StateFlow<Boolean> = _isSessionInitialized.asStateFlow()

    suspend fun initializeSession() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val user = fetchUserProfile(firebaseUser.uid)
            _currentUser.value = user
        } else {
            _currentUser.value = null
        }
        _isSessionInitialized.value = true
    }

    suspend fun fetchUserProfile(uid: String): User? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) doc.toObject(User::class.java) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<User> {
        if (!Validators.isValidName(name)) {
            return Result.Error("Name must be at least 2 characters")
        }
        val cleanUsername = username.trim().lowercase()
        if (!Validators.isValidUsername(cleanUsername)) {
            return Result.Error("Username must be at least 3 alphanumeric characters")
        }
        val cleanEmail = email.trim()
        if (!Validators.isValidEmail(cleanEmail)) {
            return Result.Error("Invalid email address format")
        }
        if (!Validators.isValidPassword(password)) {
            return Result.Error("Password must be at least 8 characters")
        }
        if (role == UserRole.ADMIN) {
            return Result.Error("Admin role cannot be self-registered")
        }

        return try {
            // Create Firebase Auth user first to establish authenticated session (request.auth)
            val authResult = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val uid = authResult.user?.uid ?: return Result.Error("Failed to create authentication account")

            // Check username availability in Firestore transaction (now authenticated)
            val usernameRef = firestore.collection("usernames").document(cleanUsername)
            val usernameExists = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(usernameRef)
                snapshot.exists()
            }.await()

            if (usernameExists) {
                auth.currentUser?.delete()?.await()
                return Result.Error("Username '$cleanUsername' is already taken")
            }

            val newUser = User(
                uid = uid,
                name = name.trim(),
                username = cleanUsername,
                email = cleanEmail,
                role = role.value,
                status = UserStatus.ACTIVE.value,
                createdAt = Timestamp.now()
            )

            // Save username reservation document and user profile document in Firestore batch
            val batch = firestore.batch()
            batch.set(usernameRef, UsernameDoc(uid = uid, email = cleanEmail))
            batch.set(firestore.collection("users").document(uid), newUser)
            batch.commit().await()

            _currentUser.value = newUser
            activityLogRepository.logAction(user = newUser, action = "user_registered", details = "Role: ${role.value}")

            Result.Success(newUser)
        } catch (e: Exception) {
            try {
                auth.currentUser?.delete()?.await()
            } catch (_: Exception) {}

            val errorMsg = e.message ?: "Registration failed"
            if (errorMsg.contains("PERMISSION_DENIED", ignoreCase = true) || errorMsg.contains("permission", ignoreCase = true)) {
                Result.Error("Firestore Permission Denied. Please set your Firebase Firestore Security Rules to Test Mode in the Firebase Console.")
            } else {
                Result.Error(errorMsg)
            }
        }
    }

    suspend fun login(emailOrUsername: String, password: String): Result<User> {
        val input = emailOrUsername.trim()
        if (input.isBlank()) {
            return Result.Error("Please enter your email or username")
        }
        if (password.isBlank()) {
            return Result.Error("Please enter your password")
        }

        return try {
            val emailToUse = if (input.contains("@")) {
                input
            } else {
                // Resolve username to email from Firestore `usernames` collection
                val usernameDoc = firestore.collection("usernames").document(input.lowercase()).get().await()
                if (!usernameDoc.exists()) {
                    return Result.Error("Username not found")
                }
                usernameDoc.getString("email") ?: return Result.Error("Account mapping error for username")
            }

            val authResult = auth.signInWithEmailAndPassword(emailToUse, password).await()
            val uid = authResult.user?.uid ?: return Result.Error("Authentication failed")

            var user = fetchUserProfile(uid)
            if (user == null) {
                // Auto-create missing user profile document if registration failed halfway previously
                val firebaseUser = auth.currentUser
                val fallbackUsername = firebaseUser?.email?.substringBefore("@") ?: "user_${uid.take(6)}"
                user = User(
                    uid = uid,
                    name = firebaseUser?.displayName ?: "User",
                    username = fallbackUsername,
                    email = emailToUse,
                    role = UserRole.ATTENDEE.value,
                    status = UserStatus.ACTIVE.value,
                    createdAt = Timestamp.now()
                )
                try {
                    firestore.collection("users").document(uid).set(user).await()
                } catch (_: Exception) {}
            }

            _currentUser.value = user
            activityLogRepository.logAction(user = user, action = "login")

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Login failed. Please check your credentials.")
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        val cleanEmail = email.trim()
        if (!Validators.isValidEmail(cleanEmail)) {
            return Result.Error("Please enter a valid email address")
        }
        return try {
            auth.sendPasswordResetEmail(cleanEmail).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to send reset email")
        }
    }

    suspend fun logout() {
        val user = _currentUser.value
        if (user != null) {
            activityLogRepository.logAction(user = user, action = "logout")
        }
        auth.signOut()
        _currentUser.value = null
    }
}
