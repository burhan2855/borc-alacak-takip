package com.burhan2855.borctakip.data.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.ContentResolver
import android.provider.CalendarContract
import android.util.Log
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.data.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class TransactionCalendarSync(
    private val transactionRepository: TransactionRepository,
    private val contentResolver: ContentResolver
) {
    fun syncTransaction(transaction: Transaction) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val calendarId = getCalendarId() ?: return@launch
                val eventId = findEventId(transaction)

                if (eventId != null) {
                    updateEventInCalendar(eventId, transaction)
                } else {
                    addEventToCalendar(transaction, calendarId)
                }
            } catch (e: Exception) {
                Log.e("TransactionCalendarSync", "Error syncing transaction: ${e.message}")
            }
        }
    }

    private fun addEventToCalendar(transaction: Transaction, calendarId: Long) {
        val startTime = transaction.transactionDate ?: return
        val endTime = startTime + 3600000 // 1 hour

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.TITLE, transaction.title)
            put(CalendarContract.Events.DESCRIPTION, transaction.description)
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }

        val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        Log.d("TransactionCalendarSync", "Event added: $uri")
    }

    private fun updateEventInCalendar(eventId: Long, transaction: Transaction) {
        val startTime = transaction.transactionDate ?: return
        val endTime = startTime + 3600000 // 1 hour

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.TITLE, transaction.title)
            put(CalendarContract.Events.DESCRIPTION, transaction.description)
        }

        val updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        contentResolver.update(updateUri, values, null, null)
        Log.d("TransactionCalendarSync", "Event updated: $updateUri")
    }

    private fun deleteEventFromCalendar(eventId: Long) {
        val deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        contentResolver.delete(deleteUri, null, null)
        Log.d("TransactionCalendarSync", "Event deleted: $deleteUri")
    }

    private fun findEventId(transaction: Transaction): Long? {
        val projection = arrayOf(CalendarContract.Events._ID)
        val selection = "${CalendarContract.Events.TITLE} = ? AND ${CalendarContract.Events.DTSTART} = ?"
        val selectionArgs = arrayOf(transaction.title, transaction.transactionDate.toString())

        val cursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        var eventId: Long? = null
        cursor?.use {
            if (it.moveToFirst()) {
                eventId = it.getLong(0)
            }
        }
        return eventId
    }

    private fun getCalendarId(): Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.IS_PRIMARY} = 1"

        val cursor = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        var calendarId: Long? = null
        cursor?.use {
            if (it.moveToFirst()) {
                calendarId = it.getLong(0)
            }
        }
        return calendarId
    }
}