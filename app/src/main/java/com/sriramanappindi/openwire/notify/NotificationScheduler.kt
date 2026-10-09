package com.sriramanappindi.openwire.notify

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules the periodic new-story check. Safe to call on every app start.
 * Uses [ExistingPeriodicWorkPolicy.UPDATE] rather than KEEP: KEEP would
 * silently ignore any future change to [INTERVAL] or [constraints] for
 * someone who already has the old request enqueued from a previous install,
 * since it's a no-op whenever work already exists under this name. UPDATE
 * re-applies the current request's schedule/constraints in place (without
 * losing the work's run history) so an interval change like this one
 * actually takes effect on the next app start, no reinstall required.
 * Whether anything actually gets posted is decided inside
 * [NotificationWorker] itself (the person's own preference, plus whether
 * notification permission is granted) — this just makes sure the check
 * happens at all when both of those are true.
 */
object NotificationScheduler {
    // 15 minutes is the shortest interval Android allows for periodic
    // background work — WorkManager silently clamps anything shorter up to
    // this floor. It's also what keeps notification lag from drifting too
    // far behind the in-app feed, which auto-refreshes on the same cadence.
    private val INTERVAL = 15L to TimeUnit.MINUTES

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
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }
}
