package com.burhan2855.borctakip.data.calendar

import kotlinx.coroutines.flow.StateFlow

interface PrivacyModeManager {
    val isPrivacyModeEnabled: StateFlow<Boolean>
    suspend fun setPrivacyMode(isEnabled: Boolean)
}
