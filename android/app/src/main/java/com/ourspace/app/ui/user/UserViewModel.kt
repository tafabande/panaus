package com.ourspace.app.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class PairingState {
    object Idle : PairingState()
    object Loading : PairingState()
    object Success : PairingState()
    data class Error(val message: String) : PairingState()
}

class UserViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _pairingState = MutableStateFlow<PairingState>(PairingState.Idle)
    val pairingState: StateFlow<PairingState> = _pairingState.asStateFlow()

    init {
        observeUser()
    }

    private fun observeUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                repository.observeUser(currentUser.uid).collectLatest { profile ->
                    _userProfile.value = profile
                }
            }
        }
    }

    fun pairWithPartner(partnerCode: String) {
        if (partnerCode.isBlank()) return
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        _pairingState.value = PairingState.Loading
        viewModelScope.launch {
            val result = repository.pairWithPartner(currentUserId, partnerCode)
            result.fold(
                onSuccess = { _pairingState.value = PairingState.Success },
                onFailure = { _pairingState.value = PairingState.Error(it.message ?: "Unknown pairing error") }
            )
        }
    }
    
    fun resetPairingState() {
        _pairingState.value = PairingState.Idle
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
}
