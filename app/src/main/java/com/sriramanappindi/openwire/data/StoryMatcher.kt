package com.sriramanappindi.openwire.data

import kotlin.math.abs

/**
 * Lightweight, on-device matching of "probably the same underlying event"
 * across stories from different sources or regions. Feeds don't share a
 * story ID, so there's nothing to key off directly — this compares the
 * significant words shared between two headlines (a plain Jaccard overlap)
 * and requires the stories to be close in time and in the same category.
 *
 * It's a heuristic, not a guarantee: two unrelated stories that happen to
 * share several words can still match, and a real match worded very
 * differently (translated headlines especially) can be missed entirely.
 * Good enough to surface likely related coverage; not a claim of certainty.
 */
object StoryMatcher {

    private const val MATCH_WINDOW_MS = 4 * 24 * 60 * 60 * 1000L // country editions can lag a day or two
    private const val MIN_SIMILARITY = 0.32

    private val STOPWORDS = setOf(
        "the", "a", "an", "of", "in", "on", "for", "to", "and", "is", "are", "was", "were",
        "at", "with", "by", "as", "its", "it's", "after", "amid", "over", "new", "says", "say",
        "said", "into", "than", "that", "this", "from", "has", "have", "had", "will", "be",
        "been", "but", "or", "not", "no", "up", "out", "about", "what", "who", "how", "why",
        "when", "which", "their", "his", "her", "your", "you", "we", "they", "he", "she", "it"
    )

    private fun significantWords(title: String): Set<String> =
        title.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 && it !in STOPWORDS }
            .toSet()

    private fun similarity(a: String, b: String): Double {
        val wa = significantWords(a)
        val wb = significantWords(b)
        if (wa.isEmpty() || wb.isEmpty()) return 0.0
        val intersection = wa.intersect(wb).size
        if (intersection == 0) return 0.0
        val union = wa.union(wb).size
        return intersection.toDouble() / union
    }

    private fun withinWindow(a: Story, b: Story): Boolean {
        if (a.publishedAt <= 0L || b.publishedAt <= 0L) return true
        return abs(a.publishedAt - b.publishedAt) <= MATCH_WINDOW_MS
    }

    /** The best match from each other source covering the same story in the same region — the framing-diff view. */
    fun sameCountryOtherSources(story: Story, pool: List<Story>, maxResults: Int = 5): List<Story> =
        pool.asSequence()
            .filter { it.link != story.link }
            .filter { it.region == story.region }
            .filter { it.source != story.source }
            .filter { it.category == story.category }
            .filter { withinWindow(story, it) }
            .map { it to similarity(story.title, it.title) }
            .filter { it.second >= MIN_SIMILARITY }
            .sortedByDescending { it.second }
            .distinctBy { it.first.source }
            .take(maxResults)
            .map { it.first }
            .toList()

    /** The best match from each other region covering the same story — the compare-countries view. */
    fun crossCountryMatches(story: Story, pool: List<Story>, maxResults: Int = 8): List<Story> =
        pool.asSequence()
            .filter { it.link != story.link }
            .filter { it.region != story.region }
            .filter { it.category == story.category }
            .filter { withinWindow(story, it) }
            .map { it to similarity(story.title, it.title) }
            .filter { it.second >= MIN_SIMILARITY }
            .sortedByDescending { it.second }
            .distinctBy { it.first.region }
            .take(maxResults)
            .map { it.first }
            .toList()
}
