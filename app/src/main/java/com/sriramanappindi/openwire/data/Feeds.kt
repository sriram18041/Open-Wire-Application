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
        FeedSource("https://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml", "Entertainment", "Global", "BBC News"),

        // India — reused across World/Politics the same way the Al Jazeera
        // "all" feed above covers two tabs from one endpoint, since Times of
        // India doesn't publish a separate politics-only feed.
        FeedSource("https://timesofindia.indiatimes.com/rssfeeds/-2128936835.cms", "World", "India", "Times of India"),
        FeedSource("https://timesofindia.indiatimes.com/rssfeeds/-2128936835.cms", "Politics", "India", "Times of India"),
        FeedSource("https://timesofindia.indiatimes.com/rssfeeds/1898055.cms", "Business", "India", "Times of India"),
        FeedSource("https://timesofindia.indiatimes.com/rssfeeds/66949542.cms", "Tech", "India", "Times of India"),
        FeedSource("https://timesofindia.indiatimes.com/rssfeeds/-2128672765.cms", "Science", "India", "Times of India"),
        FeedSource("https://timesofindia.indiatimes.com/rssfeeds/4719148.cms", "Sports", "India", "Times of India"),
        FeedSource("https://timesofindia.indiatimes.com/rssfeeds/1081479906.cms", "Entertainment", "India", "Times of India"),

        // United States
        FeedSource("https://feeds.nbcnews.com/nbcnews/public/news", "World", "United States", "NBC News"),
        FeedSource("https://feeds.nbcnews.com/nbcnews/public/news", "Politics", "United States", "NBC News"),
        FeedSource("https://feeds.businessinsider.com/custom/all", "Business", "United States", "Business Insider"),
        FeedSource("https://techcrunch.com/feed/", "Tech", "United States", "TechCrunch"),

        // Canada
        FeedSource("https://www.cbc.ca/webfeed/rss/rss-world", "World", "Canada", "CBC News"),
        FeedSource("https://www.cbc.ca/webfeed/rss/rss-politics", "Politics", "Canada", "CBC News"),
        FeedSource("https://www.cbc.ca/webfeed/rss/rss-business", "Business", "Canada", "CBC News"),
        FeedSource("https://www.cbc.ca/webfeed/rss/rss-technology", "Tech", "Canada", "CBC News"),
        FeedSource("https://www.cbc.ca/webfeed/rss/rss-sports", "Sports", "Canada", "CBC News"),
        FeedSource("https://www.cbc.ca/webfeed/rss/rss-arts", "Entertainment", "Canada", "CBC News"),

        // Australia
        FeedSource("https://www.abc.net.au/news/feed/104217382/rss.xml", "World", "Australia", "ABC News"),
        FeedSource("https://www.abc.net.au/news/feed/104217372/rss.xml", "Politics", "Australia", "ABC News"),
        FeedSource("https://www.abc.net.au/news/feed/104217374/rss.xml", "Business", "Australia", "ABC News"),
        FeedSource("https://www.abc.net.au/news/feed/103728570/rss.xml", "Sports", "Australia", "ABC News"),
        FeedSource("https://www.abc.net.au/news/feed/103728568/rss.xml", "Entertainment", "Australia", "ABC News")
    )
}
