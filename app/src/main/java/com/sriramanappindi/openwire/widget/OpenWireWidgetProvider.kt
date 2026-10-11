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
 *
 * Each widget instance can be scoped to one category (picked once, in
 * [WidgetConfigActivity], when the widget is added) instead of always
 * showing everything.
 */
class OpenWireWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> updateWidget(context, appWidgetManager, id) }
    }

    /** Cleans up the per-widget category choice once a widget is removed, so those prefs don't grow forever. */
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val editor = context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE).edit()
        appWidgetIds.forEach { id -> editor.remove(categoryKey(id)) }
        editor.apply()
    }

    companion object {
        private const val SLOTS = 3
        private const val WIDGET_PREFS = "openwire_widget_prefs"
        private val TITLE_IDS = intArrayOf(R.id.widget_title_1, R.id.widget_title_2, R.id.widget_title_3)
        private val META_IDS = intArrayOf(R.id.widget_meta_1, R.id.widget_meta_2, R.id.widget_meta_3)
        private val ROW_IDS = intArrayOf(R.id.widget_story_1, R.id.widget_story_2, R.id.widget_story_3)

        private fun categoryKey(widgetId: Int) = "widget_category_$widgetId"

        /** Called from [WidgetConfigActivity] once the person picks a category for this widget instance. */
        fun setWidgetCategory(context: Context, widgetId: Int, category: String) {
            context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE)
                .edit().putString(categoryKey(widgetId), category).apply()
        }

        private fun widgetCategory(context: Context, widgetId: Int): String =
            context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE)
                .getString(categoryKey(widgetId), "All") ?: "All"

        /** Called after any successful feed refresh (foreground or background) so no widget ever goes stale. */
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, OpenWireWidgetProvider::class.java)
            )
            ids.forEach { id -> updateWidget(context, manager, id) }
        }

        /** Called right after the configure screen saves a category, so the widget reflects it immediately. */
        fun refreshOne(context: Context, widgetId: Int) {
            updateWidget(context, AppWidgetManager.getInstance(context), widgetId)
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val category = widgetCategory(context, widgetId)
            val allStories = NewsCache(context).load()
            val stories = (if (category == "All") allStories else allStories.filter { it.category == category })
                .sortedByDescending { it.publishedAt }
                .take(SLOTS)
            val views = RemoteViews(context.packageName, R.layout.widget_openwire)

            views.setTextViewText(R.id.widget_header, if (category == "All") "Open Wire" else "Open Wire · $category")

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
                        views.setOnClickPendingIntent(ROW_IDS[i], openStoryIntent(context, story, widgetId * SLOTS + i))
                    }
                }
            }

            // Tapping the header (or any empty area) opens the app itself.
            views.setOnClickPendingIntent(R.id.widget_header, openAppIntent(context, widgetId))

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

        private fun openAppIntent(context: Context, widgetId: Int): android.app.PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            // Distinct request code per widget (offset past the SLOTS range
            // used by openStoryIntent above) so each widget's "open app"
            // tap gets its own PendingIntent instead of colliding.
            return android.app.PendingIntent.getActivity(
                context, widgetId * SLOTS + SLOTS, intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
