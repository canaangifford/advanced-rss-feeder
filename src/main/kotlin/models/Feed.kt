package models

import java.util.ArrayList

/**
 * Data class for an RSS feed.
 */
data class Feed(private val title: String, private val link: String, private val description: String, private val language: String,
           private val copyright: String, private val pubDate: String) {

    var items: MutableList<FeedItem> = ArrayList()
}
