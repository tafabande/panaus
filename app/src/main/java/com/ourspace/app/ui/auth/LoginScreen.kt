package com.ourspace.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import com.ourspace.app.ui.ViewModelFactory
import com.ourspace.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    factory: ViewModelFactory,
    viewModel: AuthViewModel = viewModel(factory = factory)
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) }
    
    var email by remember { mutableStateOf(sharedPrefs.getString("saved_email", "") ?: "") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(sharedPrefs.getBoolean("remember_me", false)) }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA)), // Tailwind background
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF1F2))
                        .padding(16.dp),
                    tint = Color(0xFFF43F5E)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome Back",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B) // Tailwind text color
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (authState is AuthState.Error) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = Color(0xFFE11D48), // rose-600
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF1F2), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email or Username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF43F5E), // primary
                        unfocusedBorderColor = Color(0xFFFFE4E6) // rose-100
                    )
                )


                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF43F5E),
                        unfocusedBorderColor = Color(0xFFFFE4E6)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { rememberMe = !rememberMe },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFF43F5E))
                    )
                    Text(text = "Remember me", color = Color(0xFF64748B), fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { 
                        if (rememberMe) {
                            sharedPrefs.edit().putString("saved_email", email).putBoolean("remember_me", true).apply()
                        } else {
                            sharedPrefs.edit().remove("saved_email").putBoolean("remember_me", false).apply()
                        }
                        viewModel.login(email, password) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                    enabled = authState != AuthState.Loading
                ) {
                    if (authState == AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Don't have an account? ", color = Color(0xFF64748B))
                    Text(
                        text = "Sign up",
                        color = Color(0xFFF43F5E),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}
