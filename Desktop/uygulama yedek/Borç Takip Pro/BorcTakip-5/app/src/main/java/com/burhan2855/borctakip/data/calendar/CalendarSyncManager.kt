package com.burhan2855.borctakip.data.calendar

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.util.TimeZone

class CalendarSyncManager(private val context: Context) {

    private fun getDefaultCalendarId(): Long? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            android.util.Log.w("CalendarSync", "READ_CALENDAR permission not granted, cannot get default calendar.")
            return null
        }

        // Try to find the primary calendar first
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val primarySelection = "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.IS_PRIMARY} = 1"
        
        try {
            context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, primarySelection, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(0)
                    android.util.Log.d("CalendarSync", "Found primary calendar with ID: $id")
                    return id
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CalendarSync", "Error querying for primary calendar: ${e.message}", e)
        }

        // Fallback to the first visible calendar if no primary is found
        try {
            context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, "${CalendarContract.Calendars.VISIBLE} = 1", null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(0)
                    android.util.Log.d("CalendarSync", "No primary calendar found. Using first visible calendar with ID: $id")
                    return id
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CalendarSync", "Error querying for a fallback calendar: ${e.message}", e)
        }

        android.util.Log.e("CalendarSync", "Could not find any visible calendar.")
        return null
    }

    fun addEvent(event: CalendarEvent): Long? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            android.util.Log.e("CalendarSync", "WRITE_CALENDAR permission is not granted. Cannot add event.")
            return null
        }

        val calendarId = event.calendarId.takeIf { it != null && it > 0 } ?: getDefaultCalendarId()

        if (calendarId == null) {
            android.util.Log.e("CalendarSync", "Failed to add event: No valid calendar ID could be found.")
            return null
        }

        android.util.Log.d("CalendarSync", "Adding event '${event.title}' to calendar ID: $calendarId")

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, event.startTime)
            put(CalendarContract.Events.DTEND, event.endTime)
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DESCRIPTION, event.description)
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }

        return try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()

            if (eventId != null) {
                android.util.Log.d("CalendarSync", "Event added successfully with ID: $eventId")
                if (event.reminderMinutes > 0) {
                    addReminder(eventId, event.reminderMinutes)
                }
            } else {
                android.util.Log.e("CalendarSync", "Failed to add event. Insert operation returned a null URI.")
            }
            eventId
        } catch (e: Exception) {
            android.util.Log.e("CalendarSync", "Exception occurred while adding event: ${e.message}", e)
            null
        }
    }

    private fun addReminder(eventId: Long, minutes: Int) {
        try {
            val reminderValues = ContentValues().apply {
                put(CalendarContract.Reminders.MINUTES, minutes)
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_DEFAULT)
            }
            val uri = context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
            if (uri != null) {
                android.util.Log.d("CalendarSync", "Reminder added successfully for event ID: $eventId")
            } else {
                android.util.Log.w("CalendarSync", "Failed to add reminder for event ID: $eventId. Insert returned null URI.")
            }
        } catch (e: Exception) {
            android.util.Log.e("CalendarSync", "Exception occurred while adding reminder for event ID $eventId: ${e.message}", e)
        }
    }
}
