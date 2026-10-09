package com.sriramanappindi.openwire.data

import org.w3c.dom.Element
import java.io.InputStream
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Minimal RSS 2.0 / Atom reader built on the JDK's own DOM parser, so the
 * app needs no third-party XML or networking library. Deliberately
 * forgiving: a malformed or unexpected feed yields an empty list rather
 * than crashing the refresh.
 */
object RssParser {

    fun parse(input: InputStream, feed: FeedSource): List<Story> {
        return try {
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = false
                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            }
            val doc = factory.newDocumentBuilder().parse(input)
            val items = doc.getElementsByTagName("item")
            val entries = doc.getElementsByTagName("entry")
            val nodeList = if (items.length > 0) items else entries

            (0 until nodeList.length)
                .mapNotNull { nodeList.item(it) as? Element }
                .mapNotNull { toStory(it, feed) }
        } catch (e: Exception) {
            throw IllegalStateException("parse failed for ${feed.url}: ${e.javaClass.simpleName}: ${e.message}", e)
        }
    }

    private fun toStory(el: Element, feed: FeedSource): Story? {
        val title = textOf(el, "title")?.let(::stripHtml)?.takeIf { it.isNotBlank() } ?: return null
        val link = linkOf(el)?.takeIf { it.isNotBlank() } ?: return null
        val summaryRaw = textOf(el, "description") ?: textOf(el, "summary") ?: textOf(el, "content") ?: ""
        val summary = stripHtml(summaryRaw).take(240)
        return Story(
            id = link,
            category = feed.category,
            region = feed.region,
            title = title,
            summary = summary,
            link = link,
            source = feed.sourceName,
            publishedAt = dateOf(el)
        )
    }

    private fun textOf(parent: Element, tag: String): String? {
        val list = parent.getElementsByTagName(tag)
        if (list.length == 0) return null
        return list.item(0)?.textContent?.trim()
    }

    /** RSS: `<link>text</link>`. Atom: one or more `<link href="…" rel="…"/>`. */
    private fun linkOf(parent: Element): String? {
        val list = parent.getElementsByTagName("link")
        if (list.length == 0) return null
        var fallback: String? = null
        for (i in 0 until list.length) {
            val node = list.item(i) as? Element ?: continue
            val href = node.getAttribute("href")
            if (href.isNotBlank()) {
                val rel = node.getAttribute("rel")
                if (rel.isBlank() || rel == "alternate") return href
                if (fallback == null) fallback = href
            } else {
                val text = node.textContent?.trim()
                if (!text.isNullOrBlank() && fallback == null) fallback = text
            }
        }
        return fallback
    }

    private fun dateOf(parent: Element): Long {
        val raw = textOf(parent, "pubDate")
            ?: textOf(parent, "published")
            ?: textOf(parent, "updated")
            ?: return 0L
        return parseDate(raw)
    }

    private fun parseDate(raw: String): Long {
        return runCatching {
            java.time.ZonedDateTime.parse(raw, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
        }.recoverCatching {
            Instant.parse(raw).toEpochMilli()
        }.getOrDefault(0L)
    }

    private fun stripHtml(s: String): String =
        s.replace(Regex("<[^>]*>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
}
