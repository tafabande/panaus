package com.ourspace.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourspace.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.login(email, pass)
            result.fold(
                onSuccess = { _authState.value = AuthState.Success },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Unknown login error") }
            )
        }
    }

    fun register(name: String, email: String, pass: String) {
        if (name.isBlank()) {
            _authState.value = AuthState.Error("Name is required")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.register(name, email, pass)
            result.fold(
                onSuccess = { _authState.value = AuthState.Success },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Unknown register error") }
            )
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
