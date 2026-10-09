package com.sriramanappindi.openwire.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * On-disk cache so the app has something to show instantly on launch and
 * while offline, instead of a blank screen until the network responds.
 */
class NewsCache(context: Context) {
    private val file: File = File(context.filesDir, "stories_cache.json")

    fun load(): List<Story> {
        return try {
            if (!file.exists()) return emptyList()
            val arr = JSONArray(file.readText())
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                Story(
                    id = o.optString("id"),
                    category = o.optString("category"),
                    region = o.optString("region"),
                    title = o.optString("title"),
                    summary = o.optString("summary"),
                    link = o.optString("link"),
                    source = o.optString("source"),
                    publishedAt = o.optLong("publishedAt"),
                    imageUrl = o.optString("imageUrl").takeIf { it.isNotBlank() }
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(stories: List<Story>) {
        try {
            val arr = JSONArray()
            stories.take(500).forEach { s ->
                val o = JSONObject()
                o.put("id", s.id)
                o.put("category", s.category)
                o.put("region", s.region)
                o.put("title", s.title)
                o.put("summary", s.summary)
                o.put("link", s.link)
                o.put("source", s.source)
                o.put("publishedAt", s.publishedAt)
                o.put("imageUrl", s.imageUrl ?: "")
                arr.put(o)
            }
            file.writeText(arr.toString())
        } catch (_: Exception) {
            // Best-effort cache; a failed write just means a blanker cold start next time.
        }
    }
}
