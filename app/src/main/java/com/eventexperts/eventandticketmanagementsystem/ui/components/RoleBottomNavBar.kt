package com.eventexperts.eventandticketmanagementsystem.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun RoleBottomNavBar(
    userRole: String,
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = when (userRole.lowercase()) {
        "organizer" -> listOf(
            BottomNavItem("organizer", "My Events", Icons.Default.Dashboard),
            BottomNavItem("organizer/edit", "Create", Icons.Default.AddCircle),
            BottomNavItem("home", "Discover", Icons.Default.Home),
            BottomNavItem("profile", "Profile", Icons.Default.Person)
        )
        "admin" -> listOf(
            BottomNavItem("admin", "Admin", Icons.Default.AdminPanelSettings),
            BottomNavItem("admin/users", "Users", Icons.Default.Group),
            BottomNavItem("admin/catalog", "Catalog", Icons.Default.Category),
            BottomNavItem("admin/logs", "Logs", Icons.Default.History),
            BottomNavItem("profile", "Profile", Icons.Default.Person)
        )
        else -> listOf(
            BottomNavItem("home", "Home", Icons.Default.Home),
            BottomNavItem("search", "Search", Icons.Default.Search),
            BottomNavItem("bookings", "Bookings", Icons.Default.ConfirmationNumber),
            BottomNavItem("favorites", "Favorites", Icons.Default.Favorite),
            BottomNavItem("profile", "Profile", Icons.Default.Person)
        )
    }

    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onNavigateToRoute(item.route)
                    }
                },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) }
            )
        }
    }
}
