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
import com.sriramanappindi.openwire.MainActivity
import com.sriramanappindi.openwire.R

/**
 * Posts an immediate, on-demand notification from the Settings screen so a
 * person can confirm the whole pipeline (permission, channel, display)
 * actually works on their device, without waiting for the next periodic
 * background check — which, by design, only fires once new stories show up.
 */
object TestNotification {
    private const val TEST_NOTIFICATION_ID = 1002

    /** Returns true if the notification was posted, false if permission is missing. */
    fun send(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationWorker.CHANNEL_ID, "New stories", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Lets you know when a new story appears in your feed" }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationWorker.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Open Wire")
            .setContentText("Notifications are working — you'll hear from us when new stories land.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(TEST_NOTIFICATION_ID, notification)
        return true
    }
}
