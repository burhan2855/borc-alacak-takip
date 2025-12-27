package com.burhan2855.borctakip.data.calendar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {

    @Query("SELECT * FROM calendar_events ORDER BY startTime DESC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE syncStatus != 'SYNCED' ORDER BY createdAt ASC")
    suspend fun getAllPendingEvents(): List<CalendarEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent): Long

    @Query("SELECT * FROM calendar_events WHERE transactionId = :transactionId")
    suspend fun getEventsByTransactionId(transactionId: Long): List<CalendarEvent>

    @Query("SELECT * FROM calendar_events WHERE transactionId = :transactionId LIMIT 1")
    suspend fun getEventByTransactionId(transactionId: Long): CalendarEvent?
    
    @Query("UPDATE calendar_events SET title = :title, description = :description, startTime = :startTime, endTime = :endTime, reminderMinutes = :reminderMinutes, syncStatus = :syncStatus WHERE id = :eventId")
    suspend fun updateCalendarEvent(eventId: Long, title: String, description: String, startTime: Long, endTime: Long, reminderMinutes: Int, syncStatus: SyncStatus)
    
    @Query("DELETE FROM calendar_events WHERE transactionId = :transactionId")
    suspend fun deleteEventByTransactionId(transactionId: Long)

    @Query("DELETE FROM calendar_events WHERE id = :eventId")
    suspend fun deleteCalendarEvent(eventId: Long)
}
