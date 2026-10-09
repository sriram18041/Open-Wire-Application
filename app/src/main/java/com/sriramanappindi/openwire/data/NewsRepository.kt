package com.sriramanappindi.openwire.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.net.HttpURLConnection
import java.net.URL

class NewsRepository(private val cache: NewsCache) {

    fun cached(): List<Story> = cache.load()

    /**
     * Fetches every feed in parallel. A feed that fails (network, parse,
     * geo-block) just contributes nothing rather than failing the whole
     * refresh. Only reports failure when literally everything failed AND
     * there is no cache to fall back on.
     */
    suspend fun refresh(): Result<List<Story>> = coroutineScope {
        val deferred = Feeds.ALL.map { feed ->
            async(Dispatchers.IO) {
                runCatching { fetchFeed(feed) }
                    .onFailure { e ->
                        android.util.Log.e("OpenWire", "feed failed: ${feed.url} -> ${e.javaClass.simpleName}: ${e.message}", e)
                    }
                    .map { it to null as Throwable? }
                    .getOrElse { emptyList<Story>() to it }
            }
        }
        val results = deferred.awaitAll()
        val all = results.flatMap { it.first }
        val errors = results.mapNotNull { it.second }

        if (all.isEmpty()) {
            val fallback = cache.load()
            return@coroutineScope if (fallback.isNotEmpty()) {
                Result.success(fallback)
            } else {
                val reasons = errors
                    .groupBy { "${it.javaClass.simpleName}: ${it.message}" }
                    .entries
                    .joinToString("; ") { (reason, occurrences) -> "$reason (${occurrences.size}x)" }
                val detail = if (reasons.isNotEmpty()) " [$reasons]" else " [no sources returned data, no exceptions thrown]"
                Result.failure(IllegalStateException("No connection, and none of the sources answered.$detail"))
            }
        }

        val deduped = all.distinctBy { it.link }.sortedByDescending { it.publishedAt }
        cache.save(deduped)
        Result.success(deduped)
    }

    private fun fetchFeed(feed: FeedSource): List<Story> {
        val connection = (URL(feed.url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            requestMethod = "GET"
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "OpenWire/1.0 (Android; +https://play.google.com)")
            setRequestProperty("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml, */*")
        }
        return try {
            connection.connect()
            val code = connection.responseCode
            if (code in 200..299) {
                val bytes = connection.inputStream.use { it.readBytes() }
                val encoding = connection.contentEncoding
                val stories = try {
                    bytes.inputStream().use { stream -> RssParser.parse(stream, feed) }
                } catch (e: Exception) {
                    val preview = runCatching { String(bytes, Charsets.UTF_8).take(80) }.getOrDefault("<unreadable>")
                    throw IllegalStateException(
                        "HTTP $code, ${bytes.size}B, encoding=$encoding, parse threw ${e.javaClass.simpleName}: ${e.message} | preview: ${preview.replace("\n", " ")}",
                        e
                    )
                }
                if (stories.isEmpty()) {
                    val preview = runCatching { String(bytes, Charsets.UTF_8).take(80) }.getOrDefault("<unreadable>")
                    throw IllegalStateException("HTTP $code, ${bytes.size}B, encoding=$encoding, 0 items | preview: ${preview.replace("\n", " ")}")
                }
                stories
            } else {
                throw java.io.IOException("HTTP $code from ${feed.url}")
            }
        } finally {
            connection.disconnect()
        }
    }
}
