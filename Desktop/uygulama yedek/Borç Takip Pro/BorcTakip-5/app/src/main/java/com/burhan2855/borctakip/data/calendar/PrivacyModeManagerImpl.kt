package com.burhan2855.borctakip.data.calendar

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope

class PrivacyModeManagerImpl(
    private val calendarSettingsRepository: CalendarSettingsRepository,
    private val coroutineScope: CoroutineScope
) : PrivacyModeManager {

    override val isPrivacyModeEnabled = calendarSettingsRepository.getSettings()
        .map { it?.privacyModeEnabled ?: false }
        .stateIn(coroutineScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, false)

    override suspend fun setPrivacyMode(isEnabled: Boolean) {
        val currentSettings = calendarSettingsRepository.getSettingsSync() ?: return
        val updatedSettings = currentSettings.copy(privacyModeEnabled = isEnabled)
        calendarSettingsRepository.updateSettings(updatedSettings)
    }
}
