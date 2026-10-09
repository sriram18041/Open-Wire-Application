package com.sriramanappindi.openwire.data

/** One public RSS/Atom endpoint and the category/region/source it is filed under. */
data class FeedSource(
    val url: String,
    val category: String,
    val region: String,
    val sourceName: String
)

object Feeds {

    val CATEGORIES = listOf(
        "All", "World", "Politics", "Business", "Tech", "Science", "Sports", "Entertainment"
    )

    /**
     * Public feeds only. Each is fetched independently, so one feed being
     * unreachable (geo-blocked, renamed path, temporarily down) never takes
     * the others down with it.
     */
    val ALL: List<FeedSource> = listOf(
        FeedSource("https://feeds.bbci.co.uk/news/world/rss.xml", "World", "Global", "BBC News"),
        FeedSource("https://www.aljazeera.com/xml/rss/all.xml", "World", "Global", "Al Jazeera"),
        FeedSource("https://feeds.bbci.co.uk/news/politics/rss.xml", "Politics", "UK", "BBC News"),
        FeedSource("https://www.aljazeera.com/xml/rss/all.xml", "Politics", "Global", "Al Jazeera"),
        FeedSource("https://feeds.bbci.co.uk/news/business/rss.xml", "Business", "Global", "BBC News"),
        FeedSource("https://techcrunch.com/feed/", "Tech", "Global", "TechCrunch"),
        FeedSource("https://feeds.bbci.co.uk/news/technology/rss.xml", "Tech", "Global", "BBC News"),
        FeedSource("https://feeds.bbci.co.uk/news/science_and_environment/rss.xml", "Science", "Global", "BBC News"),
        FeedSource("https://www.sciencedaily.com/rss/top/science.xml", "Science", "Global", "ScienceDaily"),
        FeedSource("https://feeds.bbci.co.uk/sport/rss.xml?edition=int", "Sports", "Global", "BBC Sport"),
        FeedSource("https://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml", "Entertainment", "Global", "BBC News")
    )
}
