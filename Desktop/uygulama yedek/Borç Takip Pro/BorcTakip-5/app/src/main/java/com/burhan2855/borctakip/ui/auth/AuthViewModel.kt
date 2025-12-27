package com.burhan2855.borctakip.ui.auth

import android.app.Application
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.burhan2855.borctakip.auth.AuthManager
import com.burhan2855.borctakip.auth.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isSignedIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userDisplayName: String? = null,
    val userEmail: String? = null,
    val userPhotoUrl: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authManager = AuthManager(application)
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val isSignedIn = authManager.isUserSignedIn()
        _uiState.value = _uiState.value.copy(
            isSignedIn = isSignedIn,
            userDisplayName = if (isSignedIn) authManager.getUserDisplayName() else null,
            userEmail = if (isSignedIn) authManager.getUserEmail() else null,
            userPhotoUrl = if (isSignedIn) authManager.getUserPhotoUrl() else null
        )
    }

    fun signInWithCredentials(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Önce demo hesap kontrolü
            if (email == "demo@example.com" && password == "1234") {
                // Demo hesap ile giriş
                authManager.setSignedIn(true, email, "Demo User")
                _uiState.value = _uiState.value.copy(
                    isSignedIn = true,
                    isLoading = false,
                    errorMessage = null,
                    userDisplayName = "Demo User",
                    userEmail = email
                )
            } else {
                // Firebase Auth ile gerçek giriş
                when (val result = authManager.signInWithEmailPassword(email, password)) {
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isSignedIn = true,
                            isLoading = false,
                            errorMessage = null,
                            userDisplayName = result.user.displayName ?: "Kullanıcı",
                            userEmail = result.user.email,
                            userPhotoUrl = result.user.photoUrl?.toString()
                        )
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
    
    fun signUpWithCredentials(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = authManager.signUpWithEmailPassword(name, email, password)
                if (result) {
                    // Başarılı kayıt
                    authManager.setSignedIn(true, email, name)
                    _uiState.value = _uiState.value.copy(
                        isSignedIn = true,
                        isLoading = false,
                        errorMessage = null,
                        userDisplayName = name,
                        userEmail = email
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Kayıt başarısız oldu. Lütfen tekrar deneyin."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Bilinmeyen bir hata oluştu"
                )
            }
        }
    }
    
    fun signInWithGoogle(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val success = authManager.signInWithGoogle(launcher)
            if (!success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google giriş başlatılamadı. Lütfen tekrar deneyin."
                )
            }
            // Başarılı olursa handleSignInResult fonksiyonu çağrılacak
        }
    }

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            when (val result = authManager.handleSignInResult(data)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSignedIn = true,
                        isLoading = false,
                        errorMessage = null,
                        userDisplayName = result.user.displayName,
                        userEmail = result.user.email,
                        userPhotoUrl = result.user.photoUrl?.toString()
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun signOut() {
        authManager.setSignedIn(false)
        authManager.signOut()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}