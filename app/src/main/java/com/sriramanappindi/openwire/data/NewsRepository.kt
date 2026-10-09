package com.sriramanappindi.openwire.data

import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull

class NewsRepository(private val cache: NewsCache) {

    /** article link -> resolved image URL, "" meaning "looked, found none". Avoids re-fetching the same article page every refresh. */
    private val imageCache = ConcurrentHashMap<String, String>()

    fun cached(): List<Story> = cache.load()

    /**
     * Fetches the given feeds in parallel. A feed that fails (network,
     * parse, geo-block) just contributes nothing rather than failing the
     * whole refresh. Only reports failure when literally everything failed
     * AND there is no cache to fall back on.
     *
     * Callers pass only the feeds they currently care about — with ~190
     * countries in the catalog, fetching all of them on every refresh would
     * be slow, data-heavy, and likely to get rate-limited, so the caller
     * (the view model) decides which regions are "active" right now.
     */
    suspend fun refresh(feeds: List<FeedSource>): Result<List<Story>> = coroutineScope {
        val (all, errors) = fetchMany(feeds)

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
        val enriched = fillMissingImages(deduped)
        cache.save(enriched)
        Result.success(enriched)
    }

    /**
     * A one-off fetch that doesn't touch the cache or fall back to it —
     * used for the "compare coverage" lens, which is a small supplementary
     * lookup on top of whatever's already on screen, not a replacement for
     * it. Returns whatever came back, empty on total failure.
     */
    suspend fun fetchSupplementary(feeds: List<FeedSource>): List<Story> = coroutineScope {
        val (all, _) = fetchMany(feeds)
        all.distinctBy { it.link }.sortedByDescending { it.publishedAt }
    }

    /** Lets callers enrich a small, already-selected list of stories (e.g. comparison matches) with [fillMissingImages]. */
    suspend fun enrichImages(stories: List<Story>): List<Story> = fillMissingImages(stories)

    private suspend fun fetchMany(feeds: List<FeedSource>): Pair<List<Story>, List<Throwable>> = coroutineScope {
        val deferred = feeds.map { feed ->
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
        results.flatMap { it.first } to results.mapNotNull { it.second }
    }

    /**
     * Some feeds (Google News among them) never include a thumbnail in the
     * feed itself — only a title, link and summary. For stories missing an
     * image, this opens the article page the story links to and reads its
     * `og:image` meta tag, the same tag every news site sets so the link
     * looks right when shared on social media. Bounded on three sides —
     * how many stories, how many requests at once, and a total time budget —
     * so one slow or unusual site can't stall a refresh; whatever doesn't
     * resolve in time is simply left without a photo, same as before.
     */
    private suspend fun fillMissingImages(stories: List<Story>): List<Story> {
        val candidates = stories.filter { it.imageUrl.isNullOrBlank() }.take(MAX_IMAGE_LOOKUPS)
        if (candidates.isEmpty()) return stories

        val resolved = withTimeoutOrNull(IMAGE_LOOKUP_BUDGET_MS) {
            coroutineScope {
                val semaphore = Semaphore(IMAGE_LOOKUP_CONCURRENCY)
                candidates.associate { story ->
                    story.link to async(Dispatchers.IO) {
                        semaphore.withPermit { resolveArticleImage(story.link) }
                    }
                }.mapValues { it.value.await() }
            }
        } ?: return stories

        if (resolved.values.all { it == null }) return stories
        return stories.map { story ->
            val found = resolved[story.link]
            if (story.imageUrl.isNullOrBlank() && !found.isNullOrBlank()) story.copy(imageUrl = found) else story
        }
    }

    private fun resolveArticleImage(link: String): String? {
        imageCache[link]?.let { return it.ifBlank { null } }
        val found = runCatching { fetchOgImage(link) }.getOrNull()
        imageCache[link] = found.orEmpty()
        return found
    }

    private fun fetchOgImage(link: String): String? {
        val connection = (URL(link).openConnection() as HttpURLConnection).apply {
            connectTimeout = 5_000
            readTimeout = 5_000
            requestMethod = "GET"
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10) OpenWire/1.0")
            setRequestProperty("Accept", "text/html")
        }
        return try {
            connection.connect()
            if (connection.responseCode !in 200..299) return null
            val html = connection.inputStream.use { it.readBytes() }
                .let { bytes -> String(bytes, Charsets.UTF_8) }
                .take(120_000) // link-preview tags always sit near the top of <head>
            ogImage(html)
        } finally {
            connection.disconnect()
        }
    }

    private fun ogImage(html: String): String? {
        OG_IMAGE_PATTERNS.forEach { pattern ->
            pattern.find(html)?.groupValues?.get(1)?.let { return it }
        }
        return null
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

    companion object {
        private const val MAX_IMAGE_LOOKUPS = 50
        private const val IMAGE_LOOKUP_CONCURRENCY = 6
        private const val IMAGE_LOOKUP_BUDGET_MS = 18_000L

        private val OG_IMAGE_PATTERNS = listOf(
            Regex("""<meta[^>]+property=["']og:image["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
            Regex("""<meta[^>]+content=["']([^"']+)["'][^>]+property=["']og:image["']""", RegexOption.IGNORE_CASE),
            Regex("""<meta[^>]+name=["']twitter:image["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
            Regex("""<meta[^>]+content=["']([^"']+)["'][^>]+name=["']twitter:image["']""", RegexOption.IGNORE_CASE)
        )
    }
}
