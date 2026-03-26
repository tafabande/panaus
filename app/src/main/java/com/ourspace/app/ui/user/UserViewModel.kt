package com.ourspace.app.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.ourspace.app.util.GlobalErrorHandler

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

    private val _hasSkippedPairing = MutableStateFlow(false)
    val hasSkippedPairing: StateFlow<Boolean> = _hasSkippedPairing.asStateFlow()

    private var observerJob: Job? = null

    init {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                observeUser(user.uid)
            } else {
                observerJob?.cancel()
                _userProfile.value = null
            }
        }
    }

    private fun observeUser(uid: String) {
        observerJob?.cancel()
        observerJob = viewModelScope.launch {
            repository.observeUser(uid)
                .catch { e -> GlobalErrorHandler.recordException(e) }
                .collectLatest { profile ->
                    _userProfile.value = profile
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
    
    fun setDiscoverability(isDiscoverable: Boolean) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val result = repository.updateDiscoverability(currentUserId, isDiscoverable)
            result.onFailure { GlobalErrorHandler.recordException(it) }
        }
    }

    fun skipPairing() {
        _hasSkippedPairing.value = true
    }

    fun resetPairingState() {
        _pairingState.value = PairingState.Idle
    }

    private val _isSavingProfile = MutableStateFlow(false)
    val isSavingProfile: StateFlow<Boolean> = _isSavingProfile.asStateFlow()

    fun updateProfile(name: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            _isSavingProfile.value = true
            val result = repository.updateProfile(currentUserId, name)
            result.onFailure { GlobalErrorHandler.recordException(it) }
            _isSavingProfile.value = false
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
}
