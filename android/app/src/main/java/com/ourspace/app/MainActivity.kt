package com.ourspace.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.ourspace.app.ui.auth.LoginScreen
import com.ourspace.app.ui.auth.RegisterScreen
import com.ourspace.app.ui.features.CalendarScreen
import com.ourspace.app.ui.features.FeaturesViewModel
import com.ourspace.app.ui.features.NotesScreen
import com.ourspace.app.ui.features.TodosScreen
import com.ourspace.app.ui.features.MoodScreen
import com.ourspace.app.ui.features.AsksScreen
import com.ourspace.app.ui.features.PremiumFeatureScreen
import com.ourspace.app.ui.user.DashboardScreen
import com.ourspace.app.ui.user.PairingScreen
import com.ourspace.app.ui.user.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userViewModel: UserViewModel = viewModel()
            val userProfile by userViewModel.userProfile.collectAsState()
            val hasSkippedPairing by userViewModel.hasSkippedPairing.collectAsState()
            
            val featuresViewModel: FeaturesViewModel = viewModel()

            LaunchedEffect(userProfile) {
                if (userProfile?.coupleId != null) {
                    featuresViewModel.startObserving(userProfile!!)
                }
            }

            val navController = rememberNavController()
            val startDest = if (FirebaseAuth.getInstance().currentUser != null) "main_flow" else "login"

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDest,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("login") {
                        LoginScreen(
                            onNavigateToRegister = { navController.navigate("register") },
                            onLoginSuccess = { 
                                navController.navigate("main_flow") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onNavigateToLogin = { navController.navigate("login") },
                            onRegisterSuccess = { 
                                navController.navigate("main_flow") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("main_flow") {
                        if (userProfile == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Loading Data...")
                            }
                        } else if (userProfile?.coupleId == null && !hasSkippedPairing) {
                            PairingScreen(
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("main_flow") { inclusive = true }
                                    }
                                },
                                onPaired = {
                                    // Automatic recomposition will show dashboard
                                },
                                onSkip = { userViewModel.skipPairing() },
                                viewModel = userViewModel
                            )
                        } else {
                            DashboardScreen(
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("main_flow") { inclusive = true }
                                    }
                                },
                                onNavigateToPairing = { navController.navigate("pairing") },
                                onNavigateToNotes = { navController.navigate("notes") },
                                onNavigateToTodos = { navController.navigate("todos") },
                                onNavigateToCalendar = { navController.navigate("calendar") },
                                onNavigateToMoods = { navController.navigate("moods") },
                                onNavigateToAsks = { navController.navigate("asks") },
                                onNavigateToLocation = { navController.navigate("location") },
                                userViewModel = userViewModel,
                                featuresViewModel = featuresViewModel
                            )
                        }
                    }

                    composable("pairing") {
                        PairingScreen(
                            onLogout = { navController.navigate("login") { popUpTo("main_flow") { inclusive = true } } },
                            onPaired = { navController.popBackStack() },
                            onSkip = { navController.popBackStack() },
                            viewModel = userViewModel
                        )
                    }
                    
                    composable("notes") {
                        if (userProfile != null) {
                            NotesScreen(
                                userProfile = userProfile!!,
                                viewModel = featuresViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    
                    composable("todos") {
                        if (userProfile != null) {
                            TodosScreen(
                                userProfile = userProfile!!,
                                viewModel = featuresViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    
                    composable("calendar") {
                        if (userProfile != null) {
                            CalendarScreen(
                                userProfile = userProfile!!,
                                viewModel = featuresViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    composable("moods") {
                        if (userProfile != null) {
                            MoodScreen(
                                userProfile = userProfile!!,
                                viewModel = featuresViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    
                    composable("asks") {
                        if (userProfile != null) {
                            AsksScreen(
                                userProfile = userProfile!!,
                                viewModel = featuresViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    composable("location") {
                        PremiumFeatureScreen(
                            title = "Location Check-in",
                            description = "Upgrade to Premium to unlock Location Check-ins. Share coordinates safely with your partner over encrypted, highly-permissioned channels.",
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
