package com.burhan2855.borctakip.data.calendar

import kotlinx.coroutines.flow.Flow

interface CalendarSettingsRepository {
    fun getSettings(): Flow<CalendarSettings?>
    suspend fun getSettingsSync(): CalendarSettings?
    suspend fun updateSettings(settings: CalendarSettings)
    suspend fun initializeDefaultSettings()
}

class CalendarSettingsRepositoryImpl(
    private val calendarSettingsDao: CalendarSettingsDao
) : CalendarSettingsRepository {

    override fun getSettings(): Flow<CalendarSettings?> {
        return calendarSettingsDao.getSettings()
    }

    override suspend fun getSettingsSync(): CalendarSettings? {
        return calendarSettingsDao.getSettingsSync()
    }

    override suspend fun updateSettings(settings: CalendarSettings) {
        calendarSettingsDao.updateSettings(settings)
    }

    override suspend fun initializeDefaultSettings() {
        val existingSettings = calendarSettingsDao.getSettingsSync()
        if (existingSettings == null) {
            val defaultSettings = CalendarSettings(
                defaultCalendarId = null,
                autoCreateReminders = true, // Varsayılan olarak açık
                privacyModeEnabled = false, // Gizlilik modu kapalı - işlem detayları görünür
                defaultReminderMinutes = 1440, // 1 gün önce
                followUpIntervalDays = 3,
                maxFollowUpCount = 3
            )
            calendarSettingsDao.insertSettings(defaultSettings)
        } else {
            // Mevcut ayarlarda gizlilik modunu kapalı yap
            if (existingSettings.privacyModeEnabled) {
                val updatedSettings = existingSettings.copy(privacyModeEnabled = false)
                calendarSettingsDao.updateSettings(updatedSettings)
            }
        }
    }
}
