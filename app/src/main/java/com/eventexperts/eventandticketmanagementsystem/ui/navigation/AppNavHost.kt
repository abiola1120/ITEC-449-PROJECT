package com.eventexperts.eventandticketmanagementsystem.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eventexperts.eventandticketmanagementsystem.EventApp
import com.eventexperts.eventandticketmanagementsystem.data.model.UserRole
import com.eventexperts.eventandticketmanagementsystem.ui.activitylog.ActivityLogsScreen
import com.eventexperts.eventandticketmanagementsystem.ui.activitylog.ActivityLogsViewModel
import com.eventexperts.eventandticketmanagementsystem.ui.admin.AdminPortalScreen
import com.eventexperts.eventandticketmanagementsystem.ui.admin.UserDetailsScreen
import com.eventexperts.eventandticketmanagementsystem.ui.admin.UserManagementScreen
import com.eventexperts.eventandticketmanagementsystem.ui.admin.UserManagementViewModel
import com.eventexperts.eventandticketmanagementsystem.ui.auth.AuthViewModel
import com.eventexperts.eventandticketmanagementsystem.ui.auth.LoginScreen
import com.eventexperts.eventandticketmanagementsystem.ui.auth.RegisterScreen
import com.eventexperts.eventandticketmanagementsystem.ui.auth.ResetPasswordScreen
import com.eventexperts.eventandticketmanagementsystem.ui.auth.SuspendedScreen
import com.eventexperts.eventandticketmanagementsystem.ui.auth.WelcomeScreen
import com.eventexperts.eventandticketmanagementsystem.ui.booking.BookingScreen
import com.eventexperts.eventandticketmanagementsystem.ui.booking.BookingViewModel
import com.eventexperts.eventandticketmanagementsystem.ui.booking.MyBookingsScreen
import com.eventexperts.eventandticketmanagementsystem.ui.catalog.CatalogScreen
import com.eventexperts.eventandticketmanagementsystem.ui.catalog.CatalogViewModel
import com.eventexperts.eventandticketmanagementsystem.ui.checkin.AttendeeListScreen
import com.eventexperts.eventandticketmanagementsystem.ui.checkin.CheckInScreen
import com.eventexperts.eventandticketmanagementsystem.ui.checkin.CheckInViewModel
import com.eventexperts.eventandticketmanagementsystem.ui.components.LoadingView
import com.eventexperts.eventandticketmanagementsystem.ui.components.RoleBottomNavBar
import com.eventexperts.eventandticketmanagementsystem.ui.components.TopAppBarWithNotificationBell
import com.eventexperts.eventandticketmanagementsystem.ui.dashboard.DashboardViewModel
import com.eventexperts.eventandticketmanagementsystem.ui.dashboard.RoleDashboardScreen
import com.eventexperts.eventandticketmanagementsystem.ui.events.EventDetailsScreen
import com.eventexperts.eventandticketmanagementsystem.ui.events.EventDetailsViewModel
import com.eventexperts.eventandticketmanagementsystem.ui.events.ReviewsScreen
import com.eventexperts.eventandticketmanagementsystem.ui.events.SearchScreen
import com.eventexperts.eventandticketmanagementsystem.ui.events.SearchViewModel
import com.eventexperts.eventandticketmanagementsystem.ui.home.HomeScreen
import com.eventexperts.eventandticketmanagementsystem.ui.home.HomeViewModel
import com.eventexperts.eventandticketmanagementsystem.ui.notifications.NotificationCenterScreen
import com.eventexperts.eventandticketmanagementsystem.ui.notifications.NotificationViewModel
import com.eventexperts.eventandticketmanagementsystem.ui.organizer.EventEditScreen
import com.eventexperts.eventandticketmanagementsystem.ui.organizer.OrganizerDashboardScreen
import com.eventexperts.eventandticketmanagementsystem.ui.organizer.OrganizerViewModel
import com.eventexperts.eventandticketmanagementsystem.ui.organizer.TicketTypesScreen
import com.eventexperts.eventandticketmanagementsystem.ui.organizer.TicketTypesViewModel
import com.eventexperts.eventandticketmanagementsystem.ui.profile.ProfileScreen
import com.eventexperts.eventandticketmanagementsystem.ui.tickets.DigitalTicketScreen
import com.eventexperts.eventandticketmanagementsystem.ui.tickets.DigitalTicketViewModel
import com.eventexperts.eventandticketmanagementsystem.ui.tickets.FavoritesScreen
import com.eventexperts.eventandticketmanagementsystem.util.SeedDataTool

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as EventApp).container

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.provideFactory(appContainer.authRepository)
    )
    val catalogViewModel: CatalogViewModel = viewModel(
        factory = CatalogViewModel.provideFactory(appContainer.catalogRepository)
    )
    val organizerViewModel: OrganizerViewModel = viewModel(
        factory = OrganizerViewModel.provideFactory(appContainer.eventRepository, appContainer.catalogRepository)
    )
    val ticketTypesViewModel: TicketTypesViewModel = viewModel(
        factory = TicketTypesViewModel.provideFactory(appContainer.eventRepository, appContainer.ticketTypeRepository)
    )
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(appContainer.eventRepository)
    )
    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.provideFactory(appContainer.eventRepository, appContainer.catalogRepository)
    )
    val eventDetailsViewModel: EventDetailsViewModel = viewModel(
        factory = EventDetailsViewModel.provideFactory(appContainer.eventRepository, appContainer.ticketTypeRepository)
    )
    val bookingViewModel: BookingViewModel = viewModel(
        factory = BookingViewModel.provideFactory(appContainer.eventRepository, appContainer.ticketTypeRepository, appContainer.bookingRepository, appContainer.promoCodeRepository)
    )
    val digitalTicketViewModel: DigitalTicketViewModel = viewModel(
        factory = DigitalTicketViewModel.provideFactory(appContainer.bookingRepository, appContainer.eventRepository)
    )
    val checkInViewModel: CheckInViewModel = viewModel(
        factory = CheckInViewModel.provideFactory(appContainer.checkInRepository, appContainer.eventRepository)
    )
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModel.provideFactory(appContainer.notificationRepository)
    )
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.provideFactory(appContainer.firestore)
    )

    val currentUser by authViewModel.currentUser.collectAsState()
    val isSessionInitialized by authViewModel.isSessionInitialized.collectAsState()
    val notificationsUiState by notificationViewModel.uiState.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            notificationViewModel.loadNotifications(currentUser!!.uid)
            try {
                SeedDataTool.seedSampleDataIfEmpty(appContainer.firestore)
            } catch (e: Exception) {
                // Ignore seed error
            }
        }
    }

    if (!isSessionInitialized) {
        LoadingView(message = "Initializing app session...")
        return
    }

    val userRole = currentUser?.role ?: UserRole.ATTENDEE.value
    val initialRoute = when {
        currentUser?.isSuspended == true -> Routes.SUSPENDED
        currentUser != null -> when (userRole) {
            UserRole.ORGANIZER.value -> Routes.ORGANIZER_DASHBOARD
            UserRole.ADMIN.value -> Routes.ADMIN_HOME
            else -> Routes.HOME
        }
        else -> Routes.WELCOME
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hideBars = currentRoute in listOf(
        Routes.WELCOME,
        Routes.LOGIN,
        Routes.REGISTER,
        Routes.RESET_PASSWORD,
        Routes.SUSPENDED
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            if (!hideBars) {
                val title = when (currentRoute) {
                    Routes.HOME -> "Discover Events"
                    Routes.SEARCH -> "Search Events"
                    Routes.MY_BOOKINGS -> "My Bookings"
                    Routes.FAVORITES -> "Favorites & Reminders"
                    Routes.ORGANIZER_DASHBOARD -> "My Events"
                    Routes.ADMIN_HOME -> "Admin Portal"
                    Routes.ADMIN_USERS -> "User Management"
                    Routes.ADMIN_CATALOG -> "Category & Venue Catalog"
                    Routes.ADMIN_LOGS -> "Activity Logs"
                    Routes.DASHBOARD -> "Dashboard & Analytics"
                    Routes.NOTIFICATIONS -> "Notifications"
                    Routes.PROFILE -> "Profile & Settings"
                    else -> "Event App"
                }

                TopAppBarWithNotificationBell(
                    title = title,
                    unreadNotificationsCount = notificationsUiState.unreadCount,
                    onBackClick = if (navController.previousBackStackEntry != null) {
                        { navController.popBackStack() }
                    } else null,
                    onNotificationBellClick = { navController.navigate(Routes.NOTIFICATIONS) }
                )
            }
        },
        bottomBar = {
            if (!hideBars) {
                RoleBottomNavBar(
                    userRole = userRole,
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onNavigateToLogin = { navController.navigate(Routes.LOGIN) },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        val role = authViewModel.currentUser.value?.role
                        val route = when (role) {
                            UserRole.ORGANIZER.value -> Routes.ORGANIZER_DASHBOARD
                            UserRole.ADMIN.value -> Routes.ADMIN_HOME
                            else -> Routes.HOME
                        }
                        navController.navigate(route) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                    onNavigateToReset = { navController.navigate(Routes.RESET_PASSWORD) },
                    onNavigateSuspended = { navController.navigate(Routes.SUSPENDED) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = {
                        val role = authViewModel.currentUser.value?.role
                        val route = when (role) {
                            UserRole.ORGANIZER.value -> Routes.ORGANIZER_DASHBOARD
                            UserRole.ADMIN.value -> Routes.ADMIN_HOME
                            else -> Routes.HOME
                        }
                        navController.navigate(route) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.navigate(Routes.LOGIN) }
                )
            }
            composable(Routes.RESET_PASSWORD) {
                ResetPasswordScreen(
                    viewModel = authViewModel,
                    onBackToLogin = { navController.popBackStack() }
                )
            }
            composable(Routes.SUSPENDED) {
                SuspendedScreen(
                    onSignOut = {
                        authViewModel.logout()
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onEventClick = { eventId -> navController.navigate(Routes.eventDetails(eventId)) }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onEventClick = { eventId -> navController.navigate(Routes.eventDetails(eventId)) }
                )
            }
            composable(
                route = Routes.EVENT_DETAILS,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                EventDetailsScreen(
                    eventId = eventId,
                    viewModel = eventDetailsViewModel,
                    onBookClick = { id -> navController.navigate(Routes.booking(id)) },
                    onReviewsClick = { id -> navController.navigate(Routes.reviews(id)) }
                )
            }
            composable(
                route = Routes.BOOKING,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                BookingScreen(
                    eventId = eventId,
                    viewModel = bookingViewModel,
                    currentUser = currentUser,
                    onBookingSuccess = { ticketId -> navController.navigate(Routes.ticketDetails(ticketId)) }
                )
            }
            composable(Routes.MY_BOOKINGS) {
                MyBookingsScreen(
                    viewModel = bookingViewModel,
                    currentUser = currentUser,
                    onTicketClick = { ticketId -> navController.navigate(Routes.ticketDetails(ticketId)) }
                )
            }
            composable(
                route = Routes.TICKET_DETAILS,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                DigitalTicketScreen(
                    bookingId = ticketId,
                    viewModel = digitalTicketViewModel,
                    currentUser = currentUser
                )
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onEventClick = { eventId -> navController.navigate(Routes.eventDetails(eventId)) }
                )
            }
            composable(
                route = Routes.REVIEWS,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                ReviewsScreen(eventId = eventId)
            }

            composable(Routes.ORGANIZER_DASHBOARD) {
                OrganizerDashboardScreen(
                    viewModel = organizerViewModel,
                    currentUser = currentUser,
                    onEditEvent = { eventId -> navController.navigate(Routes.editEvent(eventId)) },
                    onCheckIn = { eventId -> navController.navigate(Routes.checkIn(eventId)) },
                    onCreateEventClick = { navController.navigate(Routes.ORGANIZER_CREATE_EVENT) }
                )
            }
            composable(Routes.ORGANIZER_CREATE_EVENT) {
                EventEditScreen(
                    eventId = null,
                    viewModel = organizerViewModel,
                    currentUser = currentUser,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.ORGANIZER_EDIT_EVENT,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                EventEditScreen(
                    eventId = eventId,
                    viewModel = organizerViewModel,
                    currentUser = currentUser,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.ORGANIZER_TICKET_TYPES,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                TicketTypesScreen(
                    eventId = eventId,
                    viewModel = ticketTypesViewModel
                )
            }
            composable(
                route = Routes.CHECKIN,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                CheckInScreen(
                    eventId = eventId,
                    viewModel = checkInViewModel,
                    currentUser = currentUser,
                    onAttendeesListClick = { id -> navController.navigate(Routes.checkInAttendees(id)) }
                )
            }
            composable(
                route = Routes.CHECKIN_ATTENDEES,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                AttendeeListScreen(
                    eventId = eventId,
                    viewModel = checkInViewModel,
                    currentUser = currentUser
                )
            }

            composable(Routes.ADMIN_HOME) {
                AdminPortalScreen(
                    onUsersClick = { navController.navigate(Routes.ADMIN_USERS) },
                    onCatalogClick = { navController.navigate(Routes.ADMIN_CATALOG) },
                    onLogsClick = { navController.navigate(Routes.ADMIN_LOGS) }
                )
            }
            composable(Routes.ADMIN_USERS) {
                val userManagementViewModel: UserManagementViewModel = viewModel(
                    factory = UserManagementViewModel.provideFactory(appContainer.userRepository)
                )
                UserManagementScreen(
                    viewModel = userManagementViewModel,
                    onUserClick = { uid -> navController.navigate(Routes.userDetails(uid)) }
                )
            }
            composable(
                route = Routes.ADMIN_USER_DETAILS,
                arguments = listOf(navArgument("uid") { type = NavType.StringType })
            ) { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                val userManagementViewModel: UserManagementViewModel = viewModel(
                    factory = UserManagementViewModel.provideFactory(appContainer.userRepository)
                )
                UserDetailsScreen(
                    uid = uid,
                    viewModel = userManagementViewModel,
                    currentUser = currentUser
                )
            }
            composable(Routes.ADMIN_CATALOG) {
                CatalogScreen(
                    viewModel = catalogViewModel,
                    currentUser = currentUser
                )
            }
            composable(Routes.ADMIN_LOGS) {
                val activityLogsViewModel: ActivityLogsViewModel = viewModel(
                    factory = ActivityLogsViewModel.provideFactory(appContainer.activityLogRepository)
                )
                ActivityLogsScreen(
                    viewModel = activityLogsViewModel
                )
            }

            composable(Routes.DASHBOARD) {
                RoleDashboardScreen(
                    viewModel = dashboardViewModel,
                    currentUser = currentUser
                )
            }
            composable(Routes.NOTIFICATIONS) {
                NotificationCenterScreen(
                    viewModel = notificationViewModel,
                    currentUser = currentUser,
                    onNavigateToEvent = { id -> navController.navigate(Routes.eventDetails(id)) },
                    onNavigateToTicket = { id -> navController.navigate(Routes.ticketDetails(id)) }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    currentUser = currentUser,
                    onSignOut = {
                        authViewModel.logout()
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
