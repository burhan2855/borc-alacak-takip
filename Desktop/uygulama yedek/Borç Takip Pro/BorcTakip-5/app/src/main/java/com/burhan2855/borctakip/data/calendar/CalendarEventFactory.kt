package com.burhan2855.borctakip.data.calendar

import android.content.Context
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.data.calendar.CalendarSettings
import com.burhan2855.borctakip.data.calendar.EventDetails
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object CalendarEventFactory {

    fun createEventValues(
        transaction: Transaction,
        settings: CalendarSettings?,
        calendarId: Long
    ): EventDetails {
        val (title, description) = createEventTitleAndDescription(transaction, settings)

        // Start time: use transaction.dueDate (vade tarihi) instead of transaction.date (işlem tarihi)
        // If dueDate is not set, fall back to transaction.date
        val referenceTime = transaction.dueDate ?: transaction.date ?: System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = referenceTime }
        cal.set(Calendar.HOUR_OF_DAY, 10)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val startTime = cal.timeInMillis
        val endTime = startTime + (60 * 60 * 1000) // 1 hour duration -> 11:00

        return EventDetails(
            calendarId = calendarId,
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            timezone = TimeZone.getDefault().id,
            hasAlarm = settings?.addReminder ?: true,
            customAppUri = "borctakip://transaction/${transaction.id}"
        )
    }

    private fun createEventTitleAndDescription(
        transaction: Transaction,
        settings: CalendarSettings?
    ): Pair<String, String> {
        val amountString = "%.2f".format(transaction.amount)
        val title: String
        val description: String

        if (settings?.privacyModeEnabled == true) {
            title = if (transaction.isDebt) "Ödeme Hatırlatıcısı" else "Tahsilat Hatırlatıcısı"
            description = "Vadesi gelen bir işlem var."
        } else {
            title = if (transaction.isDebt) {
                "Borç: $amountString - ${transaction.title}"
            } else {
                "Alacak: $amountString - ${transaction.title}"
            }
            description = "İşlem: ${transaction.title}\nTutar: $amountString\nDurum: ${transaction.status}"
        }
        return Pair(title, description)
    }
}