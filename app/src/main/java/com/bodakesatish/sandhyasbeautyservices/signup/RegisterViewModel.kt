package com.bodakesatish.sandhyasbeautyservices.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.data.repository.AuthRepository
import com.bodakesatish.sandhyasbeautyservices.data.repository.AuthResult
import com.bodakesatish.sandhyasbeautyservices.domain.utils.NetworkResult
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Overall authentication state (is a user logged in or not)
    val currentAuthState: StateFlow<FirebaseUser?> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.currentUser
        )
    // Registration operation state, now using NetworkResult<FirebaseUser>
    private val _registrationOperationState = MutableStateFlow<NetworkResult<FirebaseUser>?>(null)
    val registrationOperationState: StateFlow<NetworkResult<FirebaseUser>?> = _registrationOperationState.asStateFlow()

    fun register(email: String, pass: String, fullName: String) {
        viewModelScope.launch {
            authRepository.register(email, pass, fullName).collect { result ->
                _registrationOperationState.value = result
                // Further actions (like updating local profile if needed beyond what repo does)
                // or navigation will be handled by observing registrationOperationState in Fragment
            }
        }
    }

    // Call this to reset the operation state, e.g., after navigating away or handling the result
    fun consumeRegistrationOperationState() {
        _registrationOperationState.value = null
    }

}