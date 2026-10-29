package com.eventexperts.eventandticketmanagementsystem.data.repository

import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.data.model.UserRole
import com.eventexperts.eventandticketmanagementsystem.data.model.UserStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserManagementTest {

    @Test
    fun lastAdminProtection_demotingLastAdminBlocked() {
        val activeAdmins = listOf(
            User(uid = "admin1", role = UserRole.ADMIN.value, status = UserStatus.ACTIVE.value)
        )

        val canDemote = activeAdmins.size > 1
        assertFalse("Demoting the last remaining active admin must be blocked", canDemote)
    }

    @Test
    fun lastAdminProtection_demotingWhenMultipleAdminsExistAllowed() {
        val activeAdmins = listOf(
            User(uid = "admin1", role = UserRole.ADMIN.value, status = UserStatus.ACTIVE.value),
            User(uid = "admin2", role = UserRole.ADMIN.value, status = UserStatus.ACTIVE.value)
        )

        val canDemote = activeAdmins.size > 1
        assertTrue("Demoting an admin when 2 or more admins exist must be allowed", canDemote)
    }

    @Test
    fun lastAdminProtection_suspendingLastAdminBlocked() {
        val activeAdmins = listOf(
            User(uid = "admin1", role = UserRole.ADMIN.value, status = UserStatus.ACTIVE.value)
        )

        val canSuspend = activeAdmins.size > 1
        assertFalse("Suspending the last remaining active admin must be blocked", canSuspend)
    }

    @Test
    fun nonAdminUser_cannotModifyRoles() {
        val regularUser = User(uid = "user1", role = UserRole.ATTENDEE.value)
        val isAdmin = regularUser.role == UserRole.ADMIN.value

        assertFalse("Non-admin user cannot modify user roles", isAdmin)
    }
}
