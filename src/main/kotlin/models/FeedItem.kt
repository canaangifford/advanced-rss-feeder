package models

/**
 * Representation data for a single RSS feed item.
 */
data class FeedItem(var title: String = "",
                    var description: String = "",
                    var link: String = "",
                    var author: String = "",
                    var pubDate: String = "",
                    var guid: String = "")


