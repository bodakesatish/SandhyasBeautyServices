package com.bodakesatish.sandhyasbeautyservices.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern // For email validation
import javax.inject.Inject

// --- Domain/Data Layer (Interfaces - usually in their own modules) ---
// Simplified for this example, assuming they return a Result<User> or similar
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User> // User is a data class for user info
}

data class User(val id: String, val email: String, val name: String? = null, val authToken: String) // Example User data class

// --- UI State ---
data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null, // Specific error for email field
    val passwordError: String? = null // Specific error for password field
)

@HiltViewModel
class LoginViewModel @Inject constructor(
   // private val authRepository: AuthRepository // Or a LoginUseCase
) : ViewModel() {

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    // Basic email validation pattern
    private val emailPattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    fun login(email: String, password: String) {
        // Reset previous field-specific errors
        _loginUiState.update { it.copy(emailError = null, passwordError = null, errorMessage = null) }

        var isValid = true
        if (email.isBlank() || !emailPattern.matcher(email).matches()) {
            _loginUiState.update { it.copy(emailError = "Invalid email address") }
            isValid = false
        }

        if (password.isBlank()) {
            _loginUiState.update { it.copy(passwordError = "Password cannot be empty") }
            isValid = false
        }
        // You could add more password validation rules here (e.g., min length)

        if (!isValid) {
            return // Don't proceed if basic validation fails
        }

//        viewModelScope.launch {
//            _loginUiState.update { it.copy(isLoading = true) }
//            try {
//                // In a real app, you'd likely have a LoginUseCase here:
//                // val result = loginUseCase(email, password)
//                val result = authRepository.login(email, password)
//
//                if (result.isSuccess) {
//                    // Login successful, token would typically be saved by the repository/use case
//                    // val user = result.getOrNull() // You can get the user object if needed
//                    _loginUiState.update {
//                        it.copy(isLoading = false, isLoginSuccessful = true, errorMessage = null)
//                    }
//                } else {
//                    // Login failed, extract error message
//                    val errorMessage = result.exceptionOrNull()?.message ?: "Login failed. Please try again."
//                    _loginUiState.update {
//                        it.copy(
//                            isLoading = false,
//                            isLoginSuccessful = false,
//                            errorMessage = errorMessage
//                        )
//                    }
//                }
//            } catch (e: Exception) { // Catch unexpected errors from the repository/network layer
//                _loginUiState.update {
//                    it.copy(
//                        isLoading = false,
//                        isLoginSuccessful = false,
//                        errorMessage = "An unexpected error occurred: ${e.localizedMessage}"
//                    )
//                }
//            }
//        }
    }

    /**
     * Call this after the error message has been shown to the user (e.g., in a Snackbar)
     * to prevent it from being shown again on configuration change.
     */
    fun errorMessageShown() {
        _loginUiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Call this after navigation has been handled to prevent re-navigation
     * on configuration changes or recompositions.
     */
    fun navigationComplete() {
        _loginUiState.update { it.copy(isLoginSuccessful = false) }
    }
}