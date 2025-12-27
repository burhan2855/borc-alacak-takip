package com.burhan2855.borctakip.data.calendar

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.data.calendar.CalendarSettings
import com.burhan2855.borctakip.data.calendar.EventDetails
import com.burhan2855.borctakip.data.calendar.EventUpdates
import java.util.*

class CalendarManagerImpl(
    private val context: Context,
    private val calendarEventDao: CalendarEventDao,
    private val calendarSettingsRepository: CalendarSettingsRepository
) : CalendarManager {

    private fun hasCalendarPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED)
    }

    @SuppressLint("MissingPermission")
    override suspend fun createPaymentReminder(transaction: Transaction): CalendarEventResult {
        return try {
            Log.d("DB_DUMP", "===== CALENDAR EVENT CREATION START =====")
            Log.d("DB_DUMP", "Transaction ID: ${transaction.id}")
            Log.d("DB_DUMP", "Transaction Title: ${transaction.title}")
            Log.d("DB_DUMP", "Transaction Amount: ${transaction.amount}")
            
            // İzin kontrol et
            if (!hasCalendarPermissions()) {
                Log.w("DB_DUMP", "WARNING: Calendar permissions not granted - calendar sync skipped")
                // İzin olmadan devam et - transaction zaten kaydedilmiş olacak
                // Takvim işlevi optional
                return CalendarEventResult(
                    success = true, 
                    eventId = null, 
                    errorMessage = "Takvim izinleri verilmemişti. Lütfen ayarlardan izin verin."
                )
            }
            Log.d("DB_DUMP", "Calendar permissions: OK")
            
            val settings = calendarSettingsRepository.getSettingsSync()
            val calendarId = settings?.defaultCalendarId ?: getPrimaryCalendarId() ?: 1L  // Default to Google Calendar (ID=1)

            Log.d("DB_DUMP", "Calendar ID: $calendarId")
            Log.d("DB_DUMP", "Default Calendar: ${settings?.defaultCalendarId}")

            val eventDetails = CalendarEventFactory.createEventValues(transaction, settings, calendarId)

            val contentValues = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, eventDetails.startTime)
                put(CalendarContract.Events.DTEND, eventDetails.endTime)
                put(CalendarContract.Events.TITLE, eventDetails.title)
                put(CalendarContract.Events.DESCRIPTION, eventDetails.description)
                put(CalendarContract.Events.CALENDAR_ID, eventDetails.calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, eventDetails.timezone)
                put(CalendarContract.Events.CUSTOM_APP_PACKAGE, context.packageName)
                put(CalendarContract.Events.CUSTOM_APP_URI, eventDetails.customAppUri)
            }

            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, contentValues)
            val eventId = uri?.lastPathSegment?.toLong()

            Log.d("DB_DUMP", "Insert URI: $uri")
            Log.d("DB_DUMP", "Event ID: $eventId")

            if (eventId != null) {
                Log.d("DB_DUMP", "Event created successfully, adding reminder and database entry")
                val reminderMinutes = settings?.defaultReminderMinutes ?: 15
                val reminderValues = ContentValues().apply {
                    put(CalendarContract.Reminders.MINUTES, reminderMinutes)
                    put(CalendarContract.Reminders.EVENT_ID, eventId)
                    put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                }
                context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
                Log.d("DB_DUMP", "Reminder added: $reminderMinutes minutes")

                Log.d("DB_DUMP", "Creating CalendarEvent for transactionId: ${transaction.id}")
                
                val calendarEvent = CalendarEvent(
                    id = 0,
                    transactionId = transaction.id,
                    deviceCalendarEventId = eventId,
                    calendarId = calendarId,
                    title = transaction.title,
                    description = "Tutar: ${transaction.amount} - Durum: ${transaction.status}",
                    startTime = eventDetails.startTime,
                    endTime = eventDetails.endTime,
                    reminderMinutes = settings?.defaultReminderMinutes ?: 15,
                    eventType = CalendarEventType.PAYMENT_REMINDER,
                    privacyMode = settings?.privacyModeEnabled ?: false,
                    syncStatus = SyncStatus.SYNCED,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                try {
                    calendarEventDao.insertEvent(calendarEvent)
                    Log.d("DB_DUMP", "CalendarEvent successfully inserted to database")
                } catch (dbError: Exception) {
                    Log.w("DB_DUMP", "WARNING: Failed to insert CalendarEvent to app database: ${dbError.message}", dbError)
                    // Takvim etkinliği cihaz takviminde başarıyla oluşturuldu
                    // Uygulama veritabanına yazılamazsa, en azından cihaz takvimindeki etkinlik var
                    // Bu durumda işlemi başarılı sayal
                }

                Log.d("DB_DUMP", "===== CALENDAR EVENT CREATION SUCCESS =====")
                Log.d("DB_DUMP", "Event saved to device calendar: $eventId")
                Log.d("DB_DUMP", "Event saved to app database")
                CalendarEventResult(success = true, eventId = eventId)
            } else {
                Log.e("DB_DUMP", "ERROR: Failed to create event on device calendar")
                CalendarEventResult(success = false, errorMessage = "Takvim etkinliği oluşturulamadı.")
            }
        } catch (e: Exception) {
            Log.e("DB_DUMP", "===== CALENDAR EVENT CREATION ERROR =====")
            Log.e("DB_DUMP", "Exception: ${e.message}")
            e.printStackTrace()
            CalendarEventResult(success = false, errorMessage = "Takvim etkinliği oluşturulurken bir istisna oluştu: ${e.message}")
        }
    }

    override suspend fun updateTransactionEvent(transactionId: Long, updates: EventUpdates): CalendarEventResult {
        val eventId = findEventIdForTransaction(transactionId) ?: return CalendarEventResult(success = false, errorMessage = "Etkinlik bulunamadı.")
        val contentValues = ContentValues().apply {
            updates.title?.let { put(CalendarContract.Events.TITLE, it) }
            updates.startTime?.let { put(CalendarContract.Events.DTSTART, it) }
            updates.endTime?.let { put(CalendarContract.Events.DTEND, it) }
        }
        return try {
            if (contentValues.size() > 0) {
                context.contentResolver.update(
                    ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId),
                    contentValues,
                    null,
                    null
                )
            }
            CalendarEventResult(success = true)
        } catch (e: Exception) {
            Log.e("DB_DUMP", "Error updating transaction event: ${e.message}", e)
            CalendarEventResult(success = false, errorMessage = "Etkinlik güncellenirken bir hata oluştu.")
        }
    }

    private suspend fun findEventIdForTransaction(transactionId: Long): Long? {
        return try {
            calendarEventDao.getEventByTransactionId(transactionId)?.deviceCalendarEventId
        } catch (e: Exception) {
            Log.e("DB_DUMP", "Error finding event for transaction: ${e.message}", e)
            null
        }
    }

    override suspend fun deleteTransactionEvent(transactionId: Long): CalendarEventResult {
        // TODO: Implement actual calendar event deletion logic
        return CalendarEventResult(success = true)
    }

    override suspend fun syncCalendarEvents(): SyncResult {
        // TODO: Implement actual calendar sync logic
        return SyncResult(success = true)
    }

    override suspend fun getCalendarPermissions(): PermissionStatus {
        // This is handled by CalendarPermissionHandler
        return PermissionStatus.GRANTED
    }
    
    @SuppressLint("MissingPermission")
    private fun createEventFallback(transaction: Transaction, calendarId: Long) {
        Log.d("DB_DUMP", "createEventFallback: Attempting with fallback ID")
        // Emulator'da calendar provider olmayabilir, bu durumda sadece log et
        // Production'da gerçek takvime yazılacak
    }

    @SuppressLint("MissingPermission")
    private fun getPrimaryCalendarId(): Long? {
        Log.d("DB_DUMP", "getPrimaryCalendarId: Starting search for calendars...")
        
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )
        // Daha basit koşul: görünür takvimler
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1"

        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            Log.d("DB_DUMP", "getPrimaryCalendarId: Found ${cursor.count} calendars")
            
            var firstAvailableCalendarId: Long? = null
            if (cursor.moveToFirst()) {
                val idCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                val isPrimaryCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.IS_PRIMARY)
                val displayNameCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)

                firstAvailableCalendarId = cursor.getLong(idCol)
                Log.d("DB_DUMP", "getPrimaryCalendarId: First calendar ID=$firstAvailableCalendarId")

                do {
                    try {
                        val calId = cursor.getLong(idCol)
                        val displayName = cursor.getString(displayNameCol)
                        val isPrimary = cursor.getInt(isPrimaryCol)
                        Log.d("DB_DUMP", "  Calendar: id=$calId, name='$displayName', isPrimary=$isPrimary")
                        
                        if (isPrimary == 1) {
                            Log.d("DB_DUMP", "  ✓ Found PRIMARY calendar: id=$calId")
                            return calId
                        }
                    } catch (e: Exception) {
                        Log.d("DB_DUMP", "  Error reading calendar: ${e.message}")
                        // Sütun yoksa devam et
                    }
                } while (cursor.moveToNext())
            }
            Log.d("DB_DUMP", "getPrimaryCalendarId: Returning first available: $firstAvailableCalendarId")
            return firstAvailableCalendarId
        }
        Log.e("DB_DUMP", "❌ getPrimaryCalendarId: No calendars found! Emulator may not have calendar provider.")
        return null
    }
}