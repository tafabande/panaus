package com.ourspace.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.ourspace.app.ui.user.DashboardScreen
import com.ourspace.app.ui.user.ProfileScreen
import com.ourspace.app.ui.features.*
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.ui.user.UserViewModel
import com.ourspace.app.ui.theme.AuraColors
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

sealed class BottomNavItem(var title: String, var icon: androidx.compose.ui.graphics.vector.ImageVector, var route: String) {
    object Home : BottomNavItem("Home", Icons.Default.FavoriteBorder, "home")
    object Discover : BottomNavItem("Discover", Icons.Default.Search, "discover")
    object Notes : BottomNavItem("Notes", Icons.Default.Email, "notes")
    object Calendar : BottomNavItem("Calendar", Icons.Default.DateRange, "calendar")
    object Game : BottomNavItem("Play", Icons.Default.Star, "game")
    object Timeline : BottomNavItem("Timeline", Icons.Default.History, "timeline")
    object Profile : BottomNavItem("Profile", Icons.Default.Person, "profile")
}

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    userProfile: UserProfile?,
    userViewModel: UserViewModel,
    featuresViewModel: FeaturesViewModel
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val items = remember(userProfile) {
        val baseItems = mutableListOf<BottomNavItem>()
        baseItems.add(BottomNavItem.Home)
        
        if (userProfile?.partnerId == null) {
            // Single User Layout
            baseItems.add(BottomNavItem.Discover)
            baseItems.add(BottomNavItem.Notes) // Basic feature access
        } else {
            // Coupled User Layout
            baseItems.add(BottomNavItem.Notes)
            baseItems.add(BottomNavItem.Calendar)
            baseItems.add(BottomNavItem.Timeline)
            if (userProfile.relationshipType == "ROMANTIC") {
                baseItems.add(BottomNavItem.Game)
            }
        }
        baseItems.add(BottomNavItem.Profile)
        baseItems
    }

    // Global Error Listener
    LaunchedEffect(Unit) {
        com.ourspace.app.util.GlobalErrorHandler.errorEvents.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Start data observation when profile is loaded
    LaunchedEffect(userProfile) {
        userProfile?.let { featuresViewModel.startObserving(it) }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .shadow(12.dp, RoundedCornerShape(32.dp), spotColor = MaterialTheme.colorScheme.onSurface),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), // Glassmorphism
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                DashboardScreen(
                    onLogout = {
                        rootNavController.navigate("login") {
                            popUpTo("main_flow") { inclusive = true }
                        }
                    },
                    userViewModel = userViewModel,
                    featuresViewModel = featuresViewModel,
                    onNavigateToAnalytics = { navController.navigate("analytics") },
                    onNavigateToMood = { navController.navigate("mood") },
                    onNavigateToMemories = { navController.navigate("memories") },
                    profileTheme = AuraColors.fromName(userProfile?.profileTheme)
                )
            }
            composable(BottomNavItem.Discover.route) {
                DiscoverScreen(
                    userProfile = userProfile,
                    viewModel = featuresViewModel
                )
            }
            composable(BottomNavItem.Notes.route) {
                NotesScreen(
                    userProfile = userProfile,
                    viewModel = featuresViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(BottomNavItem.Calendar.route) {
                CalendarScreen(
                    userProfile = userProfile,
                    viewModel = featuresViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(BottomNavItem.Game.route) {
                GameScreen(
                    userProfile = userProfile,
                    viewModel = featuresViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(BottomNavItem.Timeline.route) {
                com.ourspace.app.ui.features.TimelineScreen(
                    userProfile = userProfile,
                    viewModel = featuresViewModel,
                    onAddEvent = { navController.navigate("edit_relationship") },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("edit_relationship") {
                EditRelationshipScreen(
                    userProfile = userProfile,
                    viewModel = featuresViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("memories") {
                MemoriesScreen(
                    userProfile = userProfile,
                    viewModel = featuresViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        rootNavController.navigate("login") {
                            popUpTo("main_flow") { inclusive = true }
                        }
                    },
                    userProfile = userProfile,
                    userViewModel = userViewModel
                )
            }
            composable("analytics") {
                AnalyticsScreen(
                    userProfile = userProfile,
                    viewModel = featuresViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("asks") {
                AsksScreen(
                    userProfile = userProfile,
                    viewModel = featuresViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("todos") {
                TodosScreen(
                    userProfile = userProfile,
                    viewModel = featuresViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("mood") {
                MoodScreen(
                    userProfile = userProfile,
                    viewModel = featuresViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
