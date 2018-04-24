package de.gutenko.roguelike.habittracker.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.util.Log

interface AlarmScheduler {
    fun scheduleRecurringIntent(
        pendingIntent: PendingIntent,
        startMillis: Long,
        intervalMillis: Long
    )
}

class LoggingAlarmScheduler(private val alarmManager: AlarmManager) : AlarmScheduler {
    override fun scheduleRecurringIntent(
        pendingIntent: PendingIntent,
        startMillis: Long,
        intervalMillis: Long
    ) {
        Log.d(
            "AlarmScheduler",
            "Scheduling alarm starting at $startMillis recurring every $intervalMillis"
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            startMillis,
            intervalMillis,
            pendingIntent
        )
    }
}