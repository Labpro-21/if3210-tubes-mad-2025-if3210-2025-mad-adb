package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    /**
     * Check if the user is currently authenticated
     * @return true if authenticated, false otherwise
     */
    suspend fun checkAuthStatus(): Boolean {
        // First check if we have tokens
        if (!tokenManager.hasTokens()) {
            return false
        }

        // Try to validate or refresh tokens
        return authRepository.validateAndRefreshTokenIfNeeded()
    }

    /**
     * Attempt to log in with the provided credentials
     * @param email User's email
     * @param password User's password
     * @return Result containing Unit if successful, or an error if failed
     */
    suspend fun login(email: String, password: String): Result<Unit> {
        return authRepository.login(email, password)
    }

    /**
     * Log out the current user by clearing tokens
     */
    fun logout() {
        tokenManager.clearTokens()
    }
}