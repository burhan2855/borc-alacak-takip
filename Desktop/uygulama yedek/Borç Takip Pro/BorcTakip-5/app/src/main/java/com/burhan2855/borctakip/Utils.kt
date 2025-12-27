package com.burhan2855.borctakip

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.burhan2855.borctakip.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatCurrency(amount: Double, currencySymbol: String): String {
    val format = NumberFormat.getNumberInstance(Locale.getDefault())
    return "${format.format(amount)} $currencySymbol"
}

fun formatRemainingDays(context: Context, dueDate: Long): Pair<String, Color> {
    val now = Calendar.getInstance()
    val due = Calendar.getInstance().apply { timeInMillis = dueDate }

    // Reset time part for accurate day difference calculation
    now.set(Calendar.HOUR_OF_DAY, 0)
    now.set(Calendar.MINUTE, 0)
    now.set(Calendar.SECOND, 0)
    now.set(Calendar.MILLISECOND, 0)

    due.set(Calendar.HOUR_OF_DAY, 0)
    due.set(Calendar.MINUTE, 0)
    due.set(Calendar.SECOND, 0)
    due.set(Calendar.MILLISECOND, 0)

    val diff = due.timeInMillis - now.timeInMillis
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        days < 0 -> context.getString(R.string.gecmis_vade) to Color.Red
        days == 0L -> context.getString(R.string.son_gun) to Color(0xFFF9A825) // Orange
        else -> context.resources.getQuantityString(R.plurals.kalan_gun, days.toInt(), days.toInt()) to Color.Gray
    }
}
