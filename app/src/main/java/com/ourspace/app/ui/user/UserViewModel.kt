package com.ourspace.app.ui.user

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
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
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await
import android.util.Log

private const val TAG = "UserViewModel"

sealed class PairingState {
    object Idle : PairingState()
    object Loading : PairingState()
    object Success : PairingState()
    data class RequestSent(val toCode: String) : PairingState()
    data class ReceivingRequest(val fromId: String, val fromName: String) : PairingState()
    data class Error(val message: String) : PairingState()
}

class UserViewModel(application: Application, private val repository: UserRepository) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("honeybee_prefs", Context.MODE_PRIVATE)

    private val _themePreference = MutableStateFlow(prefs.getString("theme_pref", "SYSTEM") ?: "SYSTEM")
    val themePreference: StateFlow<String> = _themePreference.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _partnerProfile = MutableStateFlow<UserProfile?>(null)
    val partnerProfile: StateFlow<UserProfile?> = _partnerProfile.asStateFlow()

    private val _pairingState = MutableStateFlow<PairingState>(PairingState.Idle)
    val pairingState: StateFlow<PairingState> = _pairingState.asStateFlow()

    private val _hasSkippedPairing = MutableStateFlow(false)
    val hasSkippedPairing: StateFlow<Boolean> = _hasSkippedPairing.asStateFlow()

    private var observerJob: Job? = null
    private var partnerObserverJob: Job? = null
    private var requestObserverJob: Job? = null

    init {
        Log.d(TAG, "Initializing UserViewModel and setting up AuthStateListener")
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            Log.d(TAG, "AuthState changed: User is ${if (user != null) "Logged in (${user.uid})" else "Logged out"}")
            if (user != null) {
                observeUser(user.uid)
                observeRequests(user.uid)
            } else {
                observerJob?.cancel()
                requestObserverJob?.cancel()
                _userProfile.value = null
            }
        }
    }

    private fun observeUser(uid: String) {
        Log.d(TAG, "Calling repository.observeUser for $uid")
        observerJob?.cancel()
        observerJob = viewModelScope.launch {
            repository.observeUser(uid)
                .catch { e -> 
                    Log.e(TAG, "Error in observeUser flow for $uid", e)
                    GlobalErrorHandler.recordException(e) 
                }
                .collectLatest { profile ->
                    Log.d(TAG, "Received profile update for $uid: ${profile?.name ?: "null/not-found"}")
                    _userProfile.value = profile
                    
                    // Observe partner independently
                    if (profile?.partnerId != null) {
                        observePartner(profile.partnerId)
                    } else {
                        partnerObserverJob?.cancel()
                        _partnerProfile.value = null
                    }
                }
        }
    }

    private fun observeRequests(uid: String) {
        requestObserverJob?.cancel()
        requestObserverJob = viewModelScope.launch {
            repository.observePairingRequest(uid)
                .collectLatest { request ->
                    if (request != null && _pairingState.value !is PairingState.Loading) {
                        val fromId = request["fromId"] as? String ?: return@collectLatest
                        // In a real app, you might fetch the seeker's name here
                        _pairingState.value = PairingState.ReceivingRequest(fromId, "Someone")
                    }
                }
        }
    }

    private fun observePartner(partnerId: String) {
        if (partnerObserverJob?.isActive == true && _partnerProfile.value?.userId == partnerId) return
        partnerObserverJob?.cancel()
        partnerObserverJob = viewModelScope.launch {
            repository.observeUser(partnerId)
                .catch { e -> GlobalErrorHandler.recordException(e) }
                .collectLatest { profile ->
                    _partnerProfile.value = profile
                }
        }
    }

    fun pairWithPartner(partnerCode: String) {
        if (partnerCode.isBlank()) return
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        _pairingState.value = PairingState.Loading
        viewModelScope.launch {
            val result = repository.sendPairingRequest(currentUserId, partnerCode)
            result.fold(
                onSuccess = { _pairingState.value = PairingState.RequestSent(partnerCode) },
                onFailure = { _pairingState.value = PairingState.Error(it.message ?: "Request failed") }
            )
        }
    }

    fun acceptRequest(fromId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _pairingState.value = PairingState.Loading
        viewModelScope.launch {
            val result = repository.acceptPairingRequest(fromId, currentUserId)
            result.fold(
                onSuccess = { _pairingState.value = PairingState.Success },
                onFailure = { _pairingState.value = PairingState.Error(it.message ?: "Acceptance failed") }
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

    fun updateExtendedProfile(updates: Map<String, Any?>) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            _isSavingProfile.value = true
            val result = repository.updateExtendedProfile(currentUserId, updates)
            result.onFailure { GlobalErrorHandler.recordException(it) }
            _isSavingProfile.value = false
        }
    }

    fun uploadProfilePicture(uri: android.net.Uri) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            _isSavingProfile.value = true
            val result = repository.uploadProfilePicture(currentUserId, uri)
            result.onFailure { GlobalErrorHandler.recordException(it) }
            _isSavingProfile.value = false
        }
    }

    fun setTheme(theme: String) {
        prefs.edit().putString("theme_pref", theme).apply()
        _themePreference.value = theme
    }

    private val _timelineEvents = MutableStateFlow<List<com.ourspace.app.data.model.TimelineEvent>>(emptyList())
    val timelineEvents: StateFlow<List<com.ourspace.app.data.model.TimelineEvent>> = _timelineEvents.asStateFlow()

    private var timelineJob: kotlinx.coroutines.Job? = null

    fun startObservingTimeline() {
        val coupleId = _userProfile.value?.coupleId ?: return
        timelineJob?.cancel()
        timelineJob = viewModelScope.launch {
            repository.observeTimelineEvents(coupleId).collect {
                _timelineEvents.value = it
            }
        }
    }

    fun addTimelineEvent(name: String, date: String, description: String, category: String = "MILESTONE") {
        val coupleId = _userProfile.value?.coupleId ?: return
        val event = com.ourspace.app.data.model.TimelineEvent(
            name = name,
            date = date,
            description = description,
            category = category,
            coupleId = coupleId
        )
        viewModelScope.launch {
            _isSavingProfile.value = true
            repository.addTimelineEvent(event)
            _isSavingProfile.value = false
        }
    }

    fun unlinkPartner() {
        val user = _userProfile.value ?: return
        val currentUserId = user.userId
        val partnerId = user.partnerId ?: return
        val coupleId = user.coupleId ?: return

        viewModelScope.launch {
            _pairingState.value = PairingState.Loading
            val result = repository.unlinkPartner(currentUserId, partnerId, coupleId)
            result.fold(
                onSuccess = { _pairingState.value = PairingState.Idle },
                onFailure = { _pairingState.value = PairingState.Error(it.message ?: "Unlinking failed") }
            )
        }
    }

    private val _searchResults = MutableStateFlow<List<UserProfile>>(emptyList())
    val searchResults: StateFlow<List<UserProfile>> = _searchResults.asStateFlow()

    fun performSearch(query: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (query.length < 3) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            val result = repository.searchUsers(query, currentUserId)
            result.onSuccess { _searchResults.value = it }
        }
    }

    fun sendPairingRequestToId(partnerId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _pairingState.value = PairingState.Loading
        viewModelScope.launch {
            // We need a way to send request by ID directly.
            // For now, I'll reuse the logic from sendPairingRequest but with ID
            val requestId = listOf(currentUserId, partnerId).sorted().joinToString("_")
            val requestData = hashMapOf(
                "fromId" to currentUserId,
                "toId" to partnerId,
                "status" to "PENDING",
                "createdAt" to com.ourspace.app.data.util.DateUtils.getCurrentIsoTime()
            )
            try {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("pairingRequests").document(requestId).set(requestData).await()
                _pairingState.value = PairingState.RequestSent("User found via search")
            } catch (e: Exception) {
                _pairingState.value = PairingState.Error(e.message ?: "Failed to send request")
            }
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
}
