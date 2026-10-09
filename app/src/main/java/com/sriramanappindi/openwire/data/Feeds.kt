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
     * Hand-picked feeds that load by default, before the person has chosen a
     * region. Kept small on purpose — these are fetched on every app open
     * and every refresh, so this list is the "always on" cost.
     */
    val HOME: List<FeedSource> = listOf(
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

    /**
     * Google News only has a genuine *edition* (a locally-reported, country
     * specific feed) for a couple dozen countries, and only a handful of
     * those have an English-language edition — requesting an unsupported
     * combination (e.g. an English edition of France's news) doesn't error,
     * it silently serves back unrelated content, which is what made
     * per-country feeds look mismatched. Google News' *search* endpoint
     * sidesteps that: querying for the country's own name (plus a category
     * keyword) through the one edition that's always valid (US English)
     * reliably returns English-language stories that are actually about
     * that country, for all ~190 countries alike, with no per-country
     * edition table to maintain and no translation step needed.
     */
    private val GOOGLE_NEWS_CATEGORY_KEYWORD: Map<String, String> = mapOf(
        "World" to "",
        "Politics" to "politics",
        "Business" to "business",
        "Tech" to "technology",
        "Science" to "science",
        "Sports" to "sports",
        "Entertainment" to "entertainment"
    )

    /** How far back Google News search should look, so feeds stay current rather than surfacing old stories. */
    private const val GOOGLE_NEWS_WINDOW = "when:7d"

    private fun googleNewsSearchUrl(country: String, categoryKeyword: String): String {
        val phrase = if (country.contains(" ")) "\"$country\"" else country
        val query = listOfNotNull(phrase, categoryKeyword.ifBlank { null }, GOOGLE_NEWS_WINDOW)
            .joinToString(" ")
        val encoded = java.net.URLEncoder.encode(query, "UTF-8").replace("+", "%20")
        return "https://news.google.com/rss/search?q=$encoded&hl=en-US&gl=US&ceid=US:en"
    }

    private fun googleNewsFeeds(country: String, regionName: String): List<FeedSource> =
        GOOGLE_NEWS_CATEGORY_KEYWORD.map { (category, keyword) ->
            FeedSource(googleNewsSearchUrl(country, keyword), category, regionName, "Google News")
        }

    /** One country's feed for a single category, or null if the category isn't searchable this way. */
    fun feedFor(category: String, country: String, regionName: String): FeedSource? {
        val keyword = GOOGLE_NEWS_CATEGORY_KEYWORD[category] ?: return null
        return FeedSource(googleNewsSearchUrl(country, keyword), category, regionName, "Google News")
    }

    /**
     * A small, globally-spread set of countries for the "compare coverage"
     * feature — picked across continents and languages so a comparison
     * feels like "the world's press", not an exhaustive list. Kept
     * separate from the full [COUNTRIES] catalog on purpose: fetching this
     * set is triggered by one person tapping "compare" on one story, so it
     * has to stay small and fast rather than exhaustive.
     */
    val WORLD_LENS_COUNTRIES: List<String> = listOf(
        "United States", "UK", "India", "France", "Germany",
        "Japan", "Brazil", "Nigeria", "Australia", "South Korea"
    )

    /** The world-lens feed set for one category, skipping a region (typically the story's own, already loaded). */
    fun worldLensFeedsFor(category: String, excludingRegion: String? = null): List<FeedSource> =
        WORLD_LENS_COUNTRIES
            .filter { it != excludingRegion }
            .mapNotNull { name -> feedFor(category, name, name) }

    /** ISO 3166-1 country name -> two-letter code, for every country the region picker can search. */
    val COUNTRIES: List<Pair<String, String>> = listOf(
        // Africa
        "Algeria" to "DZ", "Angola" to "AO", "Benin" to "BJ", "Botswana" to "BW",
        "Burkina Faso" to "BF", "Burundi" to "BI", "Cameroon" to "CM", "Cape Verde" to "CV",
        "Central African Republic" to "CF", "Chad" to "TD", "Comoros" to "KM",
        "Congo" to "CG", "DR Congo" to "CD", "Djibouti" to "DJ", "Egypt" to "EG",
        "Equatorial Guinea" to "GQ", "Eritrea" to "ER", "Eswatini" to "SZ", "Ethiopia" to "ET",
        "Gabon" to "GA", "Gambia" to "GM", "Ghana" to "GH", "Guinea" to "GN",
        "Guinea-Bissau" to "GW", "Ivory Coast" to "CI", "Kenya" to "KE", "Lesotho" to "LS",
        "Liberia" to "LR", "Libya" to "LY", "Madagascar" to "MG", "Malawi" to "MW",
        "Mali" to "ML", "Mauritania" to "MR", "Mauritius" to "MU", "Morocco" to "MA",
        "Mozambique" to "MZ", "Namibia" to "NA", "Niger" to "NE", "Nigeria" to "NG",
        "Rwanda" to "RW", "Sao Tome and Principe" to "ST", "Senegal" to "SN", "Seychelles" to "SC",
        "Sierra Leone" to "SL", "Somalia" to "SO", "South Africa" to "ZA", "South Sudan" to "SS",
        "Sudan" to "SD", "Tanzania" to "TZ", "Togo" to "TG", "Tunisia" to "TN",
        "Uganda" to "UG", "Zambia" to "ZM", "Zimbabwe" to "ZW",

        // Americas
        "United States" to "US", "Canada" to "CA", "Mexico" to "MX",
        "Belize" to "BZ", "Costa Rica" to "CR", "El Salvador" to "SV", "Guatemala" to "GT",
        "Honduras" to "HN", "Nicaragua" to "NI", "Panama" to "PA",
        "Antigua and Barbuda" to "AG", "Bahamas" to "BS", "Barbados" to "BB", "Cuba" to "CU",
        "Dominica" to "DM", "Dominican Republic" to "DO", "Grenada" to "GD", "Haiti" to "HT",
        "Jamaica" to "JM", "Saint Kitts and Nevis" to "KN", "Saint Lucia" to "LC",
        "Saint Vincent and the Grenadines" to "VC", "Trinidad and Tobago" to "TT",
        "Argentina" to "AR", "Bolivia" to "BO", "Brazil" to "BR", "Chile" to "CL",
        "Colombia" to "CO", "Ecuador" to "EC", "Guyana" to "GY", "Paraguay" to "PY",
        "Peru" to "PE", "Suriname" to "SR", "Uruguay" to "UY", "Venezuela" to "VE",

        // Asia
        "Afghanistan" to "AF", "Armenia" to "AM", "Azerbaijan" to "AZ", "Bahrain" to "BH",
        "Bangladesh" to "BD", "Bhutan" to "BT", "Brunei" to "BN", "Cambodia" to "KH",
        "China" to "CN", "Cyprus" to "CY", "Georgia" to "GE", "India" to "IN",
        "Indonesia" to "ID", "Iran" to "IR", "Iraq" to "IQ", "Israel" to "IL",
        "Japan" to "JP", "Jordan" to "JO", "Kazakhstan" to "KZ", "Kuwait" to "KW",
        "Kyrgyzstan" to "KG", "Laos" to "LA", "Lebanon" to "LB", "Malaysia" to "MY",
        "Maldives" to "MV", "Mongolia" to "MN", "Myanmar" to "MM", "Nepal" to "NP",
        "North Korea" to "KP", "Oman" to "OM", "Pakistan" to "PK", "Palestine" to "PS",
        "Philippines" to "PH", "Qatar" to "QA", "Saudi Arabia" to "SA", "Singapore" to "SG",
        "South Korea" to "KR", "Sri Lanka" to "LK", "Syria" to "SY", "Taiwan" to "TW",
        "Tajikistan" to "TJ", "Thailand" to "TH", "Timor-Leste" to "TL", "Turkey" to "TR",
        "Turkmenistan" to "TM", "United Arab Emirates" to "AE", "Uzbekistan" to "UZ",
        "Vietnam" to "VN", "Yemen" to "YE",

        // Europe
        "Albania" to "AL", "Andorra" to "AD", "Austria" to "AT", "Belarus" to "BY",
        "Belgium" to "BE", "Bosnia and Herzegovina" to "BA", "Bulgaria" to "BG",
        "Croatia" to "HR", "Czech Republic" to "CZ", "Denmark" to "DK", "Estonia" to "EE",
        "Finland" to "FI", "France" to "FR", "Germany" to "DE", "Greece" to "GR",
        "Hungary" to "HU", "Iceland" to "IS", "Ireland" to "IE", "Italy" to "IT",
        "Kosovo" to "XK", "Latvia" to "LV", "Liechtenstein" to "LI", "Lithuania" to "LT",
        "Luxembourg" to "LU", "Malta" to "MT", "Moldova" to "MD", "Monaco" to "MC",
        "Montenegro" to "ME", "Netherlands" to "NL", "North Macedonia" to "MK", "Norway" to "NO",
        "Poland" to "PL", "Portugal" to "PT", "Romania" to "RO", "Russia" to "RU",
        "San Marino" to "SM", "Serbia" to "RS", "Slovakia" to "SK", "Slovenia" to "SI",
        "Spain" to "ES", "Sweden" to "SE", "Switzerland" to "CH", "Ukraine" to "UA",
        "UK" to "GB", "Vatican City" to "VA",

        // Oceania
        "Australia" to "AU", "Fiji" to "FJ", "Kiribati" to "KI", "Marshall Islands" to "MH",
        "Micronesia" to "FM", "Nauru" to "NR", "New Zealand" to "NZ", "Palau" to "PW",
        "Papua New Guinea" to "PG", "Samoa" to "WS", "Solomon Islands" to "SB",
        "Tonga" to "TO", "Tuvalu" to "TV", "Vanuatu" to "VU"
    )

    /** Every region name the picker can show, before anything beyond HOME has been fetched. */
    val ALL_REGION_NAMES: List<String> =
        (HOME.map { it.region } + COUNTRIES.map { it.first }).distinct().sorted()

    /**
     * A deliberately small, continent-spread set of editions used by
     * "Compare coverage" — enough geographic and cultural spread to make
     * the comparison meaningful without firing off a request to all ~190
     * countries just to look at one story.
     */
    val COMPARE_REGIONS: List<String> = listOf(
        "United States", "UK", "India", "Germany", "France", "Brazil",
        "Nigeria", "Japan", "Australia", "Russia", "Mexico", "South Africa"
    )

    /**
     * One Google News feed per [COMPARE_REGIONS] country, all for the same
     * single category — not every category for every country — since this
     * only needs to answer "who else is covering *this* story" for the one
     * category the tapped story is already in.
     */
    fun compareFeedsFor(category: String): List<FeedSource> {
        val keyword = GOOGLE_NEWS_CATEGORY_KEYWORD[category] ?: return emptyList()
        return COMPARE_REGIONS.map { name ->
            FeedSource(googleNewsSearchUrl(name, keyword), category, name, "Google News")
        }
    }

    /**
     * The feeds backing one region. For a region already covered by [HOME]
     * (Global, UK, India, United States, Canada, Australia) this adds the
     * matching Google News sources on top for extra depth; for every other
     * country it's Google News alone.
     */
    fun feedsFor(region: String): List<FeedSource> {
        val curated = HOME.filter { it.region == region }
        val isCountry = COUNTRIES.any { it.first == region }
        val generated = if (isCountry) googleNewsFeeds(region, region) else emptyList()
        return curated + generated
    }
}
