package com.frabon.rememberthedate.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.frabon.rememberthedate.R
import com.frabon.rememberthedate.RememberTheDateApplication
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as RememberTheDateApplication).repository
        val today = LocalDate.now()
        val eventsToday = repository.allEvents.first().filter {
            it.day == today.dayOfMonth && it.month == today.monthValue
        }

        if (eventsToday.isNotEmpty()) {
            sendNotification(eventsToday.map { it.name })
        }

        return Result.success()
    }

    private fun sendNotification(eventNames: List<String>) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Today's Events")
            .setContentText(eventNames.joinToString(", "))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        notificationManager.notify(1, notification)
    }

    companion object {
        const val CHANNEL_ID = "remember_the_date_channel"
    }
}