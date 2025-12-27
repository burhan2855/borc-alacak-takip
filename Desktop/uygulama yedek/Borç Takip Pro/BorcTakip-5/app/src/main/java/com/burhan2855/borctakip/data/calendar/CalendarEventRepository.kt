package com.burhan2855.borctakip.data.calendar

import kotlinx.coroutines.flow.Flow

interface CalendarEventRepository {
    fun getAllEvents(): Flow<List<CalendarEvent>>
    suspend fun getAllPendingEvents(): List<CalendarEvent>
    suspend fun insertEvent(event: CalendarEvent): Long
    suspend fun getEventByTransactionId(transactionId: Long): CalendarEvent?
    suspend fun getEventsByTransactionId(transactionId: Long): List<CalendarEvent>
    suspend fun updateCalendarEvent(event: CalendarEvent)
    suspend fun deleteEventByTransactionId(transactionId: Long)
    suspend fun deleteCalendarEvent(eventId: Long)
}
