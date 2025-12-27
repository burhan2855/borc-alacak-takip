package com.burhan2855.borctakip.data.calendar

import kotlinx.coroutines.flow.Flow

class CalendarEventRepositoryImpl(private val calendarEventDao: CalendarEventDao) : CalendarEventRepository {

    override fun getAllEvents(): Flow<List<CalendarEvent>> {
        return calendarEventDao.getAllEvents()
    }

    override suspend fun getAllPendingEvents(): List<CalendarEvent> {
        return calendarEventDao.getAllPendingEvents()
    }

    override suspend fun insertEvent(event: CalendarEvent): Long {
        return calendarEventDao.insertEvent(event)
    }

    override suspend fun getEventByTransactionId(transactionId: Long): CalendarEvent? {
        return calendarEventDao.getEventByTransactionId(transactionId)
    }

    override suspend fun getEventsByTransactionId(transactionId: Long): List<CalendarEvent> {
        return calendarEventDao.getEventsByTransactionId(transactionId)
    }

    override suspend fun updateCalendarEvent(event: CalendarEvent) {
        calendarEventDao.updateCalendarEvent(
            event.id,
            event.title,
            event.description,
            event.startTime,
            event.endTime,
            event.reminderMinutes,
            event.syncStatus
        )
    }

    override suspend fun deleteEventByTransactionId(transactionId: Long) {
        calendarEventDao.deleteEventByTransactionId(transactionId)
    }

    override suspend fun deleteCalendarEvent(eventId: Long) {
        calendarEventDao.deleteCalendarEvent(eventId)
    }
}
