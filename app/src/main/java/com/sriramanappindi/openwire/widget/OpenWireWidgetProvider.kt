package com.sriramanappindi.openwire.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.sriramanappindi.openwire.MainActivity
import com.sriramanappindi.openwire.R
import com.sriramanappindi.openwire.data.NewsCache
import com.sriramanappindi.openwire.data.Story

/**
 * A small, read-only home screen widget: the top three headlines from
 * whatever's already cached on disk (the same cache the app itself reads
 * on a cold start), so showing the widget never needs its own network
 * fetch — it just reflects whatever the app or the background
 * notification check last pulled down. Tapping a headline opens that
 * article directly, same as tapping it inside the app.
 */
class OpenWireWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> updateWidget(context, appWidgetManager, id) }
    }

    companion object {
        private const val SLOTS = 3
        private val TITLE_IDS = intArrayOf(R.id.widget_title_1, R.id.widget_title_2, R.id.widget_title_3)
        private val META_IDS = intArrayOf(R.id.widget_meta_1, R.id.widget_meta_2, R.id.widget_meta_3)
        private val ROW_IDS = intArrayOf(R.id.widget_story_1, R.id.widget_story_2, R.id.widget_story_3)

        /** Called after any successful feed refresh (foreground or background) so the widget never goes stale. */
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, OpenWireWidgetProvider::class.java)
            )
            ids.forEach { id -> updateWidget(context, manager, id) }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val stories = NewsCache(context).load().sortedByDescending { it.publishedAt }.take(SLOTS)
            val views = RemoteViews(context.packageName, R.layout.widget_openwire)

            if (stories.isEmpty()) {
                views.setTextViewText(TITLE_IDS[0], "Open the app to load your feed")
                views.setTextViewText(META_IDS[0], "")
                for (i in 1 until SLOTS) {
                    views.setTextViewText(TITLE_IDS[i], "")
                    views.setTextViewText(META_IDS[i], "")
                }
            } else {
                for (i in 0 until SLOTS) {
                    val story = stories.getOrNull(i)
                    views.setTextViewText(TITLE_IDS[i], story?.title ?: "")
                    views.setTextViewText(META_IDS[i], story?.let { "${it.region} · ${it.source}" } ?: "")
                    if (story != null) {
                        views.setOnClickPendingIntent(ROW_IDS[i], openStoryIntent(context, story, i))
                    }
                }
            }

            // Tapping the header (or any empty area) opens the app itself.
            views.setOnClickPendingIntent(R.id.widget_header, openAppIntent(context))

            manager.updateAppWidget(widgetId, views)
        }

        private fun openStoryIntent(context: Context, story: Story, requestCode: Int): android.app.PendingIntent {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(story.link)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return android.app.PendingIntent.getActivity(
                context, requestCode, intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun openAppIntent(context: Context): android.app.PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return android.app.PendingIntent.getActivity(
                context, SLOTS, intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
