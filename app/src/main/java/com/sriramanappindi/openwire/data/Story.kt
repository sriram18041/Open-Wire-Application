package com.sriramanappindi.openwire.data

/**
 * One headline, normalised from whichever RSS/Atom feed it came from.
 */
data class Story(
    val id: String,
    val category: String,
    val region: String,
    val title: String,
    val summary: String,
    val link: String,
    val source: String,
    val publishedAt: Long,
    val imageUrl: String? = null
)
