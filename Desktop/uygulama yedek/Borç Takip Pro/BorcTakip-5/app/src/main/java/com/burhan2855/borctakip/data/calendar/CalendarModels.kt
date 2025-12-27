package com.burhan2855.borctakip.data.calendar

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calendar_events",
    indices = [Index(value = ["transactionId"])]
)
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val transactionId: Long,
    var deviceCalendarEventId: Long,
    val calendarId: Long?,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val reminderMinutes: Int,
    val eventType: CalendarEventType,
    val privacyMode: Boolean,
    var syncStatus: SyncStatus,
    val createdAt: Long,
    var updatedAt: Long
)

enum class CalendarEventType {
    PAYMENT_REMINDER,
    INSTALLMENT_PAYMENT,
    FOLLOW_UP_REMINDER
}

enum class SyncStatus {
    PENDING_CREATE,
    SYNCED,
    PENDING_UPDATE,
    PENDING_DELETE,
    SYNC_FAILED
}

@Entity(tableName = "calendar_settings")
data class CalendarSettings(
    @PrimaryKey
    val id: Int = 1, // Fixed ID for the single settings row
    val defaultCalendarId: Long?,
    val autoCreateReminders: Boolean,
    val privacyModeEnabled: Boolean,
    val defaultReminderMinutes: Int,
    val followUpIntervalDays: Int,
    val maxFollowUpCount: Int,
    val addReminder: Boolean = true
)

data class Installment(
    val transactionId: Long,
    val installmentNumber: Int,
    val totalInstallments: Int,
    val amount: Double,
    val dueDate: Long,
    val remainingBalance: Double,
    val isPaid: Boolean
)
