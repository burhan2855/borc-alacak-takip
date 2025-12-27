package com.burhan2855.borctakip.ui.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.burhan2855.borctakip.data.calendar.CalendarEvent
import com.burhan2855.borctakip.data.calendar.CalendarEventRepository
import com.burhan2855.borctakip.data.calendar.CalendarSettings
import com.burhan2855.borctakip.data.calendar.CalendarSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CalendarScreenUiState(
    val events: List<CalendarEvent> = emptyList(),
    val settings: CalendarSettings? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class CalendarViewViewModel(private val calendarEventRepository: CalendarEventRepository, private val calendarSettingsRepository: CalendarSettingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarScreenUiState())
    val uiState: StateFlow<CalendarScreenUiState> = _uiState.asStateFlow()

    init {
        loadCalendarData()
    }

    fun loadCalendarData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                combine(
                    calendarEventRepository.getAllEvents(),
                    calendarSettingsRepository.getSettings()
                ) { events, settings ->
                    Log.d("DB_DUMP", "CalendarViewViewModel - Loaded events: ${events.size}")
                    events.forEach { event ->
                        Log.d("DB_DUMP", "  Event: ${event.title} (id=${event.id}, transactionId=${event.transactionId})")
                    }
                    CalendarScreenUiState(events = events, settings = settings, isLoading = false)
                }.collect { combinedState ->
                    Log.d("DB_DUMP", "CalendarViewViewModel - UI State updated with ${combinedState.events.size} events")
                    _uiState.value = combinedState
                }
            } catch (e: Exception) {
                Log.e("DB_DUMP", "CalendarViewViewModel - Error loading calendar data: ${e.message}", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load calendar data: ${e.message}")
            }
        }
    }

    fun onErrorShown() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

class CalendarViewViewModelFactory(private val calendarEventRepository: CalendarEventRepository, private val calendarSettingsRepository: CalendarSettingsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewViewModel(calendarEventRepository, calendarSettingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
