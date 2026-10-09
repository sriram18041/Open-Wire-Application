package com.sriramanappindi.openwire.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sriramanappindi.openwire.MainActivity
import com.sriramanappindi.openwire.R
import com.sriramanappindi.openwire.data.Feeds
import com.sriramanappindi.openwire.data.NewsCache
import com.sriramanappindi.openwire.data.NewsRepository
import com.sriramanappindi.openwire.data.Story
import com.sriramanappindi.openwire.widget.OpenWireWidgetProvider

/**
 * Periodic background check for genuinely new stories in whatever the
 * person was last looking at, so the app can nudge them without needing a
 * server of its own. There's no editorial "this is breaking" signal from
 * anywhere — it's purely "this story wasn't here last time we checked" —
 * which is honest about what it is: a new-content alert, not a breaking
 * news desk.
 */
class NotificationWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val region = prefs.getString(KEY_LAST_REGION, null)
        val feeds = Feeds.HOME + if (!region.isNullOrBlank() && region != "All regions") {
            Feeds.feedsFor(region)
        } else {
            emptyList()
        }

        val repository = NewsRepository(NewsCache(applicationContext))
        val stories = repository.refresh(feeds).getOrNull() ?: return Result.retry()

        // Keep the home screen widget fresh regardless of whether the
        // person wants notifications — those are two separate preferences,
        // and this background fetch is the only way the widget ever gets
        // new data without the app being opened in the foreground.
        OpenWireWidgetProvider.refreshAll(applicationContext)

        val notificationsWanted = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        val watermark = prefs.getLong(KEY_WATERMARK, 0L)
        val freshest = stories.maxOfOrNull { it.publishedAt } ?: 0L

        // The very first run just establishes a baseline — with nothing to
        // compare against yet, "everything is new" would mean notifying
        // about the person's entire existing feed the moment they grant
        // permission, which is exactly the kind of launch-day spam a
        // notification feature shouldn't start with.
        if (notificationsWanted && canPostNotifications() && watermark > 0L) {
            val fresh = stories.filter { it.publishedAt > watermark }
            if (fresh.isNotEmpty()) notify(fresh)
        }

        if (freshest > watermark) {
            prefs.edit().putLong(KEY_WATERMARK, freshest).apply()
        }

        return Result.success()
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ActivityCompat.checkSelfPermission(
            applicationContext, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun notify(fresh: List<Story>) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(manager)

        val top = fresh.maxByOrNull { it.publishedAt } ?: return
        val body = if (fresh.size == 1) top.title else "${top.title}  (+${fresh.size - 1} more)"

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(if (fresh.size == 1) top.region else "${fresh.size} new stories in ${top.region}")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (canPostNotifications()) {
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "New stories", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Lets you know when a new story appears in your feed"
            }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val PREFS_NAME = "openwire_prefs"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_LAST_REGION = "last_selected_region"
        const val KEY_WATERMARK = "notif_watermark_ms"
        const val CHANNEL_ID = "new_stories"
        const val NOTIFICATION_ID = 1001
        const val UNIQUE_WORK_NAME = "openwire_notification_check"
    }
}
