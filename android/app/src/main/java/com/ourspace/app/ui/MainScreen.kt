package com.ourspace.app.ui

import androidx.compose.foundation.layout.padding
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

sealed class BottomNavItem(var title: String, var icon: androidx.compose.ui.graphics.vector.ImageVector, var route: String) {
    object Home : BottomNavItem("Home", Icons.Default.FavoriteBorder, "home")
    object Notes : BottomNavItem("Notes", Icons.Default.Email, "notes")
    object Calendar : BottomNavItem("Calendar", Icons.Default.DateRange, "calendar")
    object Memories : BottomNavItem("Memories", Icons.Default.PhotoAlbum, "memories")
    object Profile : BottomNavItem("Profile", Icons.Default.Person, "profile")
}

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    userProfile: UserProfile,
    userViewModel: UserViewModel,
    featuresViewModel: FeaturesViewModel
) {
    val navController = rememberNavController()
    
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Notes,
        BottomNavItem.Calendar,
        BottomNavItem.Memories,
        BottomNavItem.Profile
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF7F6F3) // Aura Amour background
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF621837), // on_primary_container
                            unselectedIconColor = Color(0xFF5B5C5A), // on_surface_variant
                            selectedTextColor = Color(0xFF621837),
                            unselectedTextColor = Color(0xFF5B5C5A),
                            indicatorColor = Color(0xFFFE97B9) // primary_container
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = BottomNavItem.Home.route, modifier = Modifier.padding(innerPadding)) {
            composable(BottomNavItem.Home.route) {
                DashboardScreen(
                    onLogout = {
                        rootNavController.navigate("login") {
                            popUpTo("main_flow") { inclusive = true }
                        }
                    },
                    userViewModel = userViewModel,
                    featuresViewModel = featuresViewModel,
                    onNavigateToAnalytics = { navController.navigate("analytics") }
                )
            }
            composable(BottomNavItem.Notes.route) {
                // To be re-styled later
                NotesScreen(userProfile = userProfile, viewModel = featuresViewModel, onBack = {})
            }
            composable(BottomNavItem.Calendar.route) {
                // To be re-styled later
                CalendarScreen(userProfile = userProfile, viewModel = featuresViewModel, onBack = {})
            }
            composable(BottomNavItem.Memories.route) {
                MemoriesScreen(userProfile = userProfile, viewModel = featuresViewModel)
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
                AnalyticsScreen(userProfile = userProfile, viewModel = featuresViewModel)
            }
        }
    }
}
