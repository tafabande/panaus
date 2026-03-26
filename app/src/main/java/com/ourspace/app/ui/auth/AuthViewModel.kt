package com.ourspace.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourspace.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.ourspace.app.util.GlobalErrorHandler

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(identifier: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val email = if (identifier.contains("@")) {
                identifier
            } else {
                repository.getEmailByUsername(identifier)
            }

            if (email == null) {
                _authState.value = AuthState.Error("User not found with this username")
                return@launch
            }

            val result = repository.login(email, pass)
            result.fold(
                onSuccess = { _authState.value = AuthState.Success },
                onFailure = { 
                    GlobalErrorHandler.recordException(it)
                    _authState.value = AuthState.Error(it.message ?: "Unknown login error") 
                }
            )
        }
    }

    fun register(name: String, username: String, email: String, pass: String) {
        if (name.isBlank() || username.isBlank() || email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.register(name, username, email, pass)
            result.fold(
                onSuccess = { _authState.value = AuthState.Success },
                onFailure = { 
                    GlobalErrorHandler.recordException(it)
                    _authState.value = AuthState.Error(it.message ?: "Unknown register error") 
                }
            )
        }
    }


    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
