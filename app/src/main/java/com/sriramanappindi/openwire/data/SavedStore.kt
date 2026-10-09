package com.sriramanappindi.openwire.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Stories the person has explicitly bookmarked to read later. Kept
 * separate from [NewsCache] (which only holds a rolling window of
 * whatever's currently live) so a saved story stays available indefinitely
 * — even after it scrolls out of every feed that originally carried it.
 */
class SavedStore(context: Context) {
    private val file: File = File(context.filesDir, "saved_stories.json")

    fun load(): List<Story> = try {
        if (!file.exists()) {
            emptyList()
        } else {
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
        }
    } catch (_: Exception) {
        emptyList()
    }

    private fun persist(stories: List<Story>) {
        try {
            val arr = JSONArray()
            stories.forEach { s ->
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
            // Best-effort; worst case a save silently doesn't survive a restart.
        }
    }

    /** Adds the story if it isn't already saved, or removes it if it is. Returns the new full list. */
    fun toggle(story: Story): List<Story> {
        val current = load()
        val updated = if (current.any { it.id == story.id }) {
            current.filterNot { it.id == story.id }
        } else {
            listOf(story) + current
        }
        persist(updated)
        return updated
    }
}
