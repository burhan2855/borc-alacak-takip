package com.burhan2855.borctakip.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.burhan2855.borctakip.DebtApplication
import com.burhan2855.borctakip.data.calendar.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CalendarSettingsUiState(
    val autoCreateReminders: Boolean = true,
    val privacyModeEnabled: Boolean = false,
    val defaultReminderMinutes: Int = 1440,
    val followUpIntervalDays: Int = 3,
    val maxFollowUpCount: Int = 3,
    val hasCalendarPermission: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CalendarSettingsViewModel(
    private val calendarSettingsRepository: CalendarSettingsRepository,
    private val calendarPermissionHandler: CalendarPermissionHandler,
    private val privacyModeManager: PrivacyModeManager,
    private val calendarManager: CalendarManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarSettingsUiState())
    val uiState: StateFlow<CalendarSettingsUiState> = _uiState.asStateFlow()

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val settings = calendarSettingsRepository.getSettingsSync()
                val hasPermission = calendarPermissionHandler.getInitialStatus() == PermissionStatus.GRANTED

                val debugMessage = if (hasPermission) {
                    "Calendar permissions granted"
                } else {
                    "Calendar permissions not granted - please grant permissions to use calendar features"
                }

                if (settings != null) {
                    _uiState.value = _uiState.value.copy(
                        autoCreateReminders = settings.autoCreateReminders,
                        privacyModeEnabled = settings.privacyModeEnabled,
                        defaultReminderMinutes = settings.defaultReminderMinutes,
                        followUpIntervalDays = settings.followUpIntervalDays,
                        maxFollowUpCount = settings.maxFollowUpCount,
                        hasCalendarPermission = hasPermission,
                        isLoading = false,
                        errorMessage = if (!hasPermission) debugMessage else null
                    )
                } else {
                    calendarSettingsRepository.initializeDefaultSettings()
                    _uiState.value = _uiState.value.copy(
                        hasCalendarPermission = hasPermission,
                        isLoading = false,
                        errorMessage = if (!hasPermission) debugMessage else null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading settings: ${e.message}"
                )
            }
        }
    }

    fun onPermissionGranted() {
        loadSettings()
    }

    fun updateAutoCreateReminders(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = calendarSettingsRepository.getSettingsSync()
                    ?: CalendarSettings(
                        id = 1,
                        defaultCalendarId = null,
                        autoCreateReminders = _uiState.value.autoCreateReminders,
                        privacyModeEnabled = _uiState.value.privacyModeEnabled,
                        defaultReminderMinutes = _uiState.value.defaultReminderMinutes,
                        followUpIntervalDays = _uiState.value.followUpIntervalDays,
                        maxFollowUpCount = _uiState.value.maxFollowUpCount
                    )

                val updatedSettings = currentSettings.copy(autoCreateReminders = enabled)
                calendarSettingsRepository.updateSettings(updatedSettings)

                _uiState.value = _uiState.value.copy(
                    autoCreateReminders = enabled,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error updating auto-create setting: ${e.message}"
                )
            }
        }
    }

    fun updatePrivacyMode(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                privacyModeManager.setPrivacyMode(enabled)

                _uiState.value = _uiState.value.copy(
                    privacyModeEnabled = enabled,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error updating privacy mode: ${e.message}"
                )
            }
        }
    }

    fun updateDefaultReminderMinutes(minutes: Int) {
        viewModelScope.launch {
            try {
                val currentSettings = calendarSettingsRepository.getSettingsSync()
                    ?: CalendarSettings(
                        id = 1,
                        defaultCalendarId = null,
                        autoCreateReminders = _uiState.value.autoCreateReminders,
                        privacyModeEnabled = _uiState.value.privacyModeEnabled,
                        defaultReminderMinutes = _uiState.value.defaultReminderMinutes,
                        followUpIntervalDays = _uiState.value.followUpIntervalDays,
                        maxFollowUpCount = _uiState.value.maxFollowUpCount
                    )

                val updatedSettings = currentSettings.copy(defaultReminderMinutes = minutes)
                calendarSettingsRepository.updateSettings(updatedSettings)

                _uiState.value = _uiState.value.copy(
                    defaultReminderMinutes = minutes,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error updating reminder time: ${e.message}"
                )
            }
        }
    }

    fun updateFollowUpInterval(days: Int) {
        viewModelScope.launch {
            try {
                val currentSettings = calendarSettingsRepository.getSettingsSync()
                    ?: CalendarSettings(
                        id = 1,
                        defaultCalendarId = null,
                        autoCreateReminders = _uiState.value.autoCreateReminders,
                        privacyModeEnabled = _uiState.value.privacyModeEnabled,
                        defaultReminderMinutes = _uiState.value.defaultReminderMinutes,
                        followUpIntervalDays = _uiState.value.followUpIntervalDays,
                        maxFollowUpCount = _uiState.value.maxFollowUpCount
                    )

                val updatedSettings = currentSettings.copy(followUpIntervalDays = days)
                calendarSettingsRepository.updateSettings(updatedSettings)

                _uiState.value = _uiState.value.copy(
                    followUpIntervalDays = days,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error updating follow-up interval: ${e.message}"
                )
            }
        }
    }

    fun updateMaxFollowUpCount(count: Int) {
        viewModelScope.launch {
            try {
                val currentSettings = calendarSettingsRepository.getSettingsSync()
                    ?: CalendarSettings(
                        id = 1,
                        defaultCalendarId = null,
                        autoCreateReminders = _uiState.value.autoCreateReminders,
                        privacyModeEnabled = _uiState.value.privacyModeEnabled,
                        defaultReminderMinutes = _uiState.value.defaultReminderMinutes,
                        followUpIntervalDays = _uiState.value.followUpIntervalDays,
                        maxFollowUpCount = _uiState.value.maxFollowUpCount
                    )

                val updatedSettings = currentSettings.copy(maxFollowUpCount = count)
                calendarSettingsRepository.updateSettings(updatedSettings)

                _uiState.value = _uiState.value.copy(
                    maxFollowUpCount = count,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error updating max follow-up count: ${e.message}"
                )
            }
        }
    }

    fun testCalendarIntegration() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                if (calendarPermissionHandler.getInitialStatus() != PermissionStatus.GRANTED) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Takvim izinleri gerekli - lütfen izin verin"
                    )
                    return@launch
                }

                val testTransaction = com.burhan2855.borctakip.data.Transaction(
                    id = System.currentTimeMillis(),
                    title = "Test Takvim Etkinliği",
                    amount = 100.0,
                    category = "Test",
                    date = System.currentTimeMillis() + 86400000, 
                    transactionDate = System.currentTimeMillis(),
                    isDebt = true,
                    status = "Ödenmedi",
                    contactId = null
                )

                val result = calendarManager.createPaymentReminder(testTransaction)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = if (result.success) {
                        "Takvim entegrasyonu test başarılı! Takvim uygulamanızı kontrol edin."
                    } else {
                        "Takvim entegrasyonu test başarısız: ${result.errorMessage}"
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Takvim test hatası: ${e.message}"
                )
            }
        }
    }

    class Factory(private val application: DebtApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalendarSettingsViewModel::class.java)) {
                return CalendarSettingsViewModel(
                    calendarSettingsRepository = application.calendarSettingsRepository,
                    calendarPermissionHandler = application.calendarPermissionHandler,
                    privacyModeManager = application.privacyModeManager,
                    calendarManager = application.calendarManager
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
