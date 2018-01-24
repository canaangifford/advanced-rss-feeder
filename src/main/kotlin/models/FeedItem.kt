package models

/**
 * Representation data for a single RSS feed item.
 */
class FeedItem {

    var title: String = ""
    var description: String = ""
    var link: String = ""
    var author: String = ""
    var pubDate: String = ""
    var guid: String = ""

    override fun toString(): String {
        return ("FeedItem [title=" + title + ", description=" + description
                + ", link=" + link + ", author=" + author + ", date=" + pubDate + ", guid=" + guid
                + "]")
    }

}


