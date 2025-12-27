package com.burhan2855.borctakip.gemini

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burhan2855.borctakip.util.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GeminiUiState {
    object Initial : GeminiUiState()
    object Loading : GeminiUiState()
    data class Success(val output: String) : GeminiUiState()
    data class Error(val errorMessage: String) : GeminiUiState()
}

class GeminiViewModel(
    private val geminiPreferencesManager: GeminiPreferencesManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<GeminiUiState>(GeminiUiState.Initial)
    val uiState: StateFlow<GeminiUiState> = _uiState.asStateFlow()

    init {
        GeminiService.initialize()
    }

    fun generateContent(prompt: String) {
        viewModelScope.launch {
            _uiState.value = GeminiUiState.Loading
            try {
                val response = GeminiService.generateContent(prompt)
                _uiState.value = GeminiUiState.Success(response)
                Log.d("GeminiViewModel", "Response: $response")
            } catch (e: Exception) {
                Log.e("GeminiViewModel", "Error: ${e.message}", e)
                _uiState.value = GeminiUiState.Error("Hata: ${e.message}")
            }
        }
    }

    fun clearState() {
        _uiState.value = GeminiUiState.Initial
    }
}
