package com.burhan2855.borctakip.data

import androidx.room.TypeConverter
import com.burhan2855.borctakip.data.calendar.CalendarEventType
import com.burhan2855.borctakip.data.calendar.SyncStatus
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromCalendarEventType(value: CalendarEventType): String {
        return value.name
    }

    @TypeConverter
    fun toCalendarEventType(value: String): CalendarEventType {
        return CalendarEventType.valueOf(value)
    }

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String {
        return value.name
    }

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return SyncStatus.valueOf(value)
    }
}