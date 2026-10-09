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
            async(Dispatchers.IO) { runCatching { fetchFeed(feed) }.getOrDefault(emptyList()) }
        }
        val all = deferred.awaitAll().flatten()

        if (all.isEmpty()) {
            val fallback = cache.load()
            return@coroutineScope if (fallback.isNotEmpty()) {
                Result.success(fallback)
            } else {
                Result.failure(IllegalStateException("No connection, and none of the sources answered."))
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
            if (connection.responseCode in 200..299) {
                connection.inputStream.use { stream -> RssParser.parse(stream, feed) }
            } else {
                emptyList()
            }
        } finally {
            connection.disconnect()
        }
    }
}
