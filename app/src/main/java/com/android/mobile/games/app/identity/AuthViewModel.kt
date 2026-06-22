package com.android.mobile.games.app.identity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val identityManager: IdentityManager) : ViewModel() {

    sealed class AuthState {
        data object Loading : AuthState()
        data class SessionExists(val programmerId: String) : AuthState()
        data object NewUser : AuthState()
        data object GeneratingId : AuthState()
        data class Identified(val programmerId: String) : AuthState()
    }

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun checkSession() {
        viewModelScope.launch {
            identityManager.loadSession()
            _state.value = if (identityManager.hasSession()) {
                AuthState.SessionExists(identityManager.programmerId)
            } else {
                AuthState.NewUser
            }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            _state.value = AuthState.GeneratingId
            val session = identityManager.createAnonymousSession()
            _state.value = AuthState.Identified(session.programmerId)
        }
    }
}
