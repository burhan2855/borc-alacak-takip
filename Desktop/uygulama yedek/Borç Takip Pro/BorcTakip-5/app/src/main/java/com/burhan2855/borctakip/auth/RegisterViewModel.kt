package com.burhan2855.borctakip.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class RegisterViewModel(
    private val authManagerProvider: (() -> AuthManager)? = null
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state

    fun onNameChange(v: String) { _state.value = _state.value.copy(name = v, error = null) }
    fun onEmailChange(v: String) { _state.value = _state.value.copy(email = v, error = null) }
    fun onPasswordChange(v: String) { _state.value = _state.value.copy(password = v, error = null) }

    fun register(context: android.content.Context) {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.value = s.copy(error = "İsim girin")
            return
        }
        if (s.email.isBlank() || !s.email.contains('@')) {
            _state.value = s.copy(error = "Geçerli e-posta girin")
            return
        }
        if (s.password.length < 6) {
            _state.value = s.copy(error = "Şifre en az 6 karakter olmalı")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val mgr = authManagerProvider?.invoke() ?: AuthManager(context)
            try {
                val ok = mgr.signUpWithEmailPassword(s.name.trim(), s.email.trim(), s.password)
                _state.value = if (ok) {
                    _state.value.copy(loading = false, success = true)
                } else {
                    _state.value.copy(loading = false, error = "Kayıt başarısız")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message ?: "Kayıt başarısız")
            }
        }
    }
}
