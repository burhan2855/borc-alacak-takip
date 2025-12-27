package com.burhan2855.borctakip.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.burhan2855.borctakip.R

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: return Result.failure()
        val amount = inputData.getDouble("amount", 0.0)
        val currencySymbol = inputData.getString("currencySymbol") ?: ""

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            "debt_channel",
            "Debt Reminder",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, "debt_channel")
            .setContentTitle("Bugün Son Ödeme Günü: $title")
            .setContentText("Ödeme tutarı: ${amount}${currencySymbol}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        return Result.success()
    }
}
