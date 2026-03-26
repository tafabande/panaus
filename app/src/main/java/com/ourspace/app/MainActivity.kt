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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ourspace.app.ui.auth.LoginScreen
import com.ourspace.app.ui.auth.RegisterScreen
import com.ourspace.app.ui.features.CalendarScreen
import com.ourspace.app.ui.features.FeaturesViewModel
import com.ourspace.app.ui.features.NotesScreen
import com.ourspace.app.ui.features.TodosScreen
import com.ourspace.app.ui.features.MoodScreen
import com.ourspace.app.ui.features.AsksScreen
import com.ourspace.app.ui.user.DashboardScreen
import com.ourspace.app.ui.user.PairingScreen
import com.ourspace.app.ui.user.UserViewModel
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.remember
import com.ourspace.app.util.GlobalErrorHandler
import com.ourspace.app.util.UiFreezeDetector
import com.ourspace.app.ui.theme.OurSpaceTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val freezeDetector = UiFreezeDetector()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        freezeDetector.start()
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        setContent {
            val userViewModel: UserViewModel = viewModel()
            
            var isFirebaseReady by remember { mutableStateOf(true) }
            val userProfile by userViewModel.userProfile.collectAsState()
            val hasSkippedPairing by userViewModel.hasSkippedPairing.collectAsState()
            
            val featuresViewModel: FeaturesViewModel = viewModel()

            LaunchedEffect(userProfile) {
                if (userProfile?.coupleId != null) {
                    featuresViewModel.startObserving(userProfile!!)
                }
            }

            val navController = rememberNavController()
            
            // Calculate start destination only after Firebase is ready
            val startDest = remember(isFirebaseReady) {
                if (isFirebaseReady) {
                    if (FirebaseAuth.getInstance().currentUser != null) "main_flow" else "login"
                } else {
                    "loading"
                }
            }
            
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                GlobalErrorHandler.errorEvents.collect { message ->
                    snackbarHostState.showSnackbar(message)
                }
            }

            OurSpaceTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDest,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("loading") {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Initializing App...")
                        }
                    }
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
                        val snackbarHostState = remember { SnackbarHostState() }
                        var hasShownOfflineMessage by remember { mutableStateOf(false) }

                        LaunchedEffect(userProfile, hasShownOfflineMessage) {
                            if (userProfile == null && !hasShownOfflineMessage) {
                                delay(60000) // 1-minute failure handler
                                if (userProfile == null) {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Data load is taking longer than expected. Using offline mode.",
                                        actionLabel = "Retry",
                                        duration = SnackbarDuration.Long
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        hasShownOfflineMessage = false // Reset to retry
                                    } else {
                                        hasShownOfflineMessage = true
                                    }
                                }
                            }
                        }

                        if (userProfile?.coupleId == null && !hasSkippedPairing && userProfile != null) {
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
                            // Always show MainScreen shell while authenticated
                            com.ourspace.app.ui.MainScreen(
                                rootNavController = navController,
                                userProfile = userProfile,
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
                        NotesScreen(
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
                    
                    composable("calendar") {
                        CalendarScreen(
                            userProfile = userProfile,
                            viewModel = featuresViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("moods") {
                        MoodScreen(
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


                }
            }
        }
    }
}

    override fun onDestroy() {
        super.onDestroy()
        freezeDetector.stop()
    }
}
