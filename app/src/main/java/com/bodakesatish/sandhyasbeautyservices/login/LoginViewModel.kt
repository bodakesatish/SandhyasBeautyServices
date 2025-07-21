package com.bodakesatish.sandhyasbeautyservices.login

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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Login operation state, now using NetworkResult<FirebaseUser>
    private val _loginOperationState = MutableStateFlow<NetworkResult<FirebaseUser>?>(null)
    val loginOperationState: StateFlow<NetworkResult<FirebaseUser>?> = _loginOperationState.asStateFlow()


    // Overall authentication state (is a user logged in or not)
    val currentAuthState: StateFlow<FirebaseUser?> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.currentUser
        )


    fun login(email: String, pass: String) {
        viewModelScope.launch {
            authRepository.login(email, pass).collect { result ->
                _loginOperationState.value = result // Update the operation state
                if (result is NetworkResult.Success) {
                    // Successfully authenticated, now update last login time
                    result.data.uid?.let { userId -> // result.data is FirebaseUser
                        authRepository.updateUserLastLoginTime(userId)
                    }
                    // Navigation or other success actions will be handled by observing loginOperationState in Fragment
                }
                // Failure/Loading/NoInternet cases will also be handled by observing loginOperationState
            }
        }
    }

    // Call this if you want to reset the operation state, e.g., after navigating away
    fun consumeLoginOperationState() {
        _loginOperationState.value = null
    }
}