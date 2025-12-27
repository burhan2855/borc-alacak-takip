package com.burhan2855.borctakip.data.calendar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.burhan2855.borctakip.data.AppDatabase
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.data.TransactionRepository
import com.burhan2855.borctakip.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class FollowUpReminderSystem(
    private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val notificationHelper: NotificationHelper
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleFollowUpReminders() {
        scope.launch {
            val allTransactions = transactionRepository.getAllTransactionsOnce()
            val currentTime = System.currentTimeMillis()

            // Vadesi geçmiş ve ödenmemiş işlemleri bul
            val overdueTransactions = allTransactions.filter {
                it.status != "Ödendi" && (it.date ?: 0) < currentTime
            }

            if (overdueTransactions.isNotEmpty()) {
                notificationHelper.showFollowUpNotification(overdueTransactions)
            }
        }
    }

    fun createFollowUpTransaction(overdueTransaction: Transaction) {
        scope.launch {
            val newFollowUp = Transaction(
                title = "Takip: ${overdueTransaction.title}",
                amount = overdueTransaction.amount,
                date = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000, // 1 hafta sonrası
                transactionDate = System.currentTimeMillis(),
                isDebt = overdueTransaction.isDebt,
                status = "Ödenmedi",
                category = overdueTransaction.category,
                contactId = overdueTransaction.contactId
            )
            transactionRepository.insert(newFollowUp)
        }
    }

    fun snoozeFollowUp(transaction: Transaction, days: Int) {
        scope.launch {
            // Vade tarihini ertele (dueDate'i güncelle)
            val baseDueDate = transaction.dueDate ?: transaction.date ?: System.currentTimeMillis()
            val newDueDate = baseDueDate + (days * 24 * 60 * 60 * 1000)
            val updatedTransaction = transaction.copy(dueDate = newDueDate)
            transactionRepository.update(updatedTransaction)
        }
    }

    fun startPeriodicChecks() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                getPendingIntent()
            )
        } catch (e: Exception) {
            Log.e("FollowUpReminderSystem", "Error setting alarm: ${e.message}", e)
        }
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, FollowUpReceiver::class.java)
        return PendingIntent.getBroadcast(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun processOverdueTransaction(transaction: Transaction) {
        scope.launch {
            // Vadesi geçmiş işlemi "Takipte" olarak işaretle
            val updatedTransaction = transaction.copy(status = "Takipte")
            transactionRepository.update(updatedTransaction)

            // Yeni bir takip işlemi oluştur
            createFollowUpTransaction(transaction)
        }
    }

    fun handleSnoozeAction(transactionId: Long, days: Int) {
        scope.launch {
            val transaction = transactionRepository.getTransactionByIdOnce(transactionId)
            if (transaction != null) {
                snoozeFollowUp(transaction, days)
            }
        }
    }

    // BroadcastReceiver for handling scheduled reminders
    class FollowUpReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("FollowUpReceiver", "Follow-up reminder triggered")
            
            val db = AppDatabase.getDatabase(context)
            val transactionDao = db.transactionDao()
            
            try {
                val notificationHelper = NotificationHelper(context)
                notificationHelper.showFollowUpNotification(emptyList())
            } catch (e: Exception) {
                Log.e("FollowUpReceiver", "Error in onReceive: ${e.message}", e)
            }
        }
    }

    fun handleMarkAsPaidAction(transactionId: Long) {
        scope.launch {
            val transaction = transactionRepository.getTransactionByIdOnce(transactionId)
            if (transaction != null) {
                val updatedTransaction = transaction.copy(status = "Ödendi", remainingAmount = 0.0)
                transactionRepository.update(updatedTransaction)
            }
        }
    }

    fun handleCreateFollowUpAction(transactionId: Long) {
        scope.launch {
            val transaction = transactionRepository.getTransactionByIdOnce(transactionId)
            if (transaction != null) {
                processOverdueTransaction(transaction)
            }
        }
    }
}