package com.burhan2855.borctakip.data.calendar

import com.burhan2855.borctakip.data.Transaction

interface CalendarManager {
    suspend fun createPaymentReminder(transaction: Transaction): CalendarEventResult
    suspend fun updateTransactionEvent(transactionId: Long, updates: EventUpdates): CalendarEventResult
    suspend fun deleteTransactionEvent(transactionId: Long): CalendarEventResult
    suspend fun syncCalendarEvents(): SyncResult
    suspend fun getCalendarPermissions(): PermissionStatus
}

data class CalendarEventResult(
    val success: Boolean,
    val eventId: Long? = null,
    val errorMessage: String? = null
)

data class EventUpdates(
    val title: String? = null,
    val description: String? = null,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val reminderMinutes: Int? = null
)

data class SyncResult(
    val success: Boolean,
    val syncedCount: Int = 0,
    val failedCount: Int = 0,
    val errorMessage: String? = null
)
