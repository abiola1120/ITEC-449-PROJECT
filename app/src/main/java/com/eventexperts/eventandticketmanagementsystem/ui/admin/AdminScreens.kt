package com.eventexperts.eventandticketmanagementsystem.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.data.model.UserRole
import com.eventexperts.eventandticketmanagementsystem.data.model.UserStatus
import com.eventexperts.eventandticketmanagementsystem.ui.components.EmptyView
import com.eventexperts.eventandticketmanagementsystem.ui.components.LoadingView

@Composable
fun AdminPortalScreen(
    onUsersClick: () -> Unit,
    onCatalogClick: () -> Unit,
    onLogsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Admin Portal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        AdminPortalCard(
            title = "User Management",
            subtitle = "View users, update roles (Attendee/Organizer/Admin), activate or suspend accounts.",
            icon = Icons.Default.Group,
            onClick = onUsersClick
        )

        AdminPortalCard(
            title = "Category & Venue Catalog",
            subtitle = "Manage event categories and venues with in-use protection.",
            icon = Icons.Default.Category,
            onClick = onCatalogClick
        )

        AdminPortalCard(
            title = "Activity Logs",
            subtitle = "View append-only system audit logs for logins, bookings, and role changes.",
            icon = Icons.Default.History,
            onClick = onLogsClick
        )
    }
}

@Composable
private fun AdminPortalCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.height(36.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun UserManagementScreen(
    viewModel: UserManagementViewModel,
    onUserClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder = { Text("Search by name, email, or username...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text("Filter Role:", style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = uiState.selectedRoleFilter == null,
                    onClick = { viewModel.onRoleFilterSelect(null) },
                    label = { Text("All Roles") }
                )
            }
            items(UserRole.entries.toTypedArray()) { role ->
                FilterChip(
                    selected = uiState.selectedRoleFilter == role,
                    onClick = { viewModel.onRoleFilterSelect(role) },
                    label = { Text(role.value.uppercase()) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            LoadingView(message = "Loading users...")
        } else if (uiState.filteredUsers.isEmpty()) {
            EmptyView(
                title = "No Users Found",
                message = "No registered users match your search and filter criteria.",
                icon = Icons.Default.Group
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.filteredUsers, key = { it.uid }) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onUserClick(user.uid) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = user.name.ifBlank { user.username }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = "${user.email} • @${user.username}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Badge { Text("ROLE: ${user.role.uppercase()}") }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Badge { Text("STATUS: ${user.status.uppercase()}") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserDetailsScreen(
    uid: String,
    viewModel: UserManagementViewModel,
    currentUser: User?
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        viewModel.loadUserDetails(uid)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            if (event is UserManagementUiEvent.ShowSnackbar) {
                snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val user = uiState.selectedUser

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (user == null) {
            LoadingView(message = "Loading user profile...")
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(text = "User Profile & Access", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "@${user.username}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "User ID: ${user.uid}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text("Change User Role:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = user.role.uppercase(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Assigned Role") },
                    trailingIcon = {
                        IconButton(onClick = { roleDropdownExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = roleDropdownExpanded,
                    onDismissRequest = { roleDropdownExpanded = false }
                ) {
                    UserRole.entries.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role.value.uppercase()) },
                            onClick = {
                                if (currentUser != null) {
                                    viewModel.updateUserRole(user.uid, role, currentUser)
                                }
                                roleDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text("Account Status Control:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            if (user.status == UserStatus.ACTIVE.value) {
                Button(
                    onClick = {
                        if (currentUser != null) {
                            viewModel.updateUserStatus(user.uid, UserStatus.SUSPENDED, currentUser)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Suspend Account")
                }
            } else {
                Button(
                    onClick = {
                        if (currentUser != null) {
                            viewModel.updateUserStatus(user.uid, UserStatus.ACTIVE, currentUser)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Activate Account")
                }
            }
        }
    }
}
