package com.sriramanappindi.openwire.notify

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules the periodic new-story check. Safe to call on every app start —
 * [ExistingPeriodicWorkPolicy.KEEP] means it's a no-op if the work is
 * already scheduled, so this doesn't reset the timer or duplicate runs.
 * Whether anything actually gets posted is decided inside
 * [NotificationWorker] itself (the person's own preference, plus whether
 * notification permission is granted) — this just makes sure the check
 * happens at all when both of those are true.
 */
object NotificationScheduler {
    private val INTERVAL = 30L to TimeUnit.MINUTES

    fun ensureScheduled(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(INTERVAL.first, INTERVAL.second)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(
                NotificationWorker.UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }
}
