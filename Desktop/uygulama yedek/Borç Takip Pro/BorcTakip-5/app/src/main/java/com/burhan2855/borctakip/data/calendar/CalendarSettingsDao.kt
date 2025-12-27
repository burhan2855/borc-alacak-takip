package com.burhan2855.borctakip.data.calendar

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarSettingsDao {
    @Query("SELECT * FROM calendar_settings WHERE id = 1")
    fun getSettings(): Flow<CalendarSettings?>

    @Query("SELECT * FROM calendar_settings WHERE id = 1")
    suspend fun getSettingsSync(): CalendarSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: CalendarSettings)

    @Update
    suspend fun updateSettings(settings: CalendarSettings)
}
