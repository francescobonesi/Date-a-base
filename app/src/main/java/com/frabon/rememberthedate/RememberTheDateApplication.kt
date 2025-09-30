package com.frabon.rememberthedate

import android.app.Application
import androidx.work.*
import com.frabon.rememberthedate.data.AppDatabase
import com.frabon.rememberthedate.data.EventRepository
import com.frabon.rememberthedate.workers.NotificationWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class RememberTheDateApplication : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { EventRepository(database.eventDao()) }

    override fun onCreate() {
        super.onCreate()
        setupDailyNotification()
    }

    private fun setupDailyNotification() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Calculate the delay until the next 8:00 AM
        val currentTime = Calendar.getInstance()
        val dueTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        // If it's already past 8 AM today, schedule for 8 AM tomorrow
        if (currentTime.after(dueTime)) {
            dueTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        val initialDelay = dueTime.timeInMillis - currentTime.timeInMillis

        val repeatingRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "daily_notification_work",
            ExistingPeriodicWorkPolicy.KEEP, // Use KEEP to not reschedule if work already exists
            repeatingRequest
        )
    }
}