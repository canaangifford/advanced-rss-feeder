package utils

import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL

import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException
import models.Feed
import models.FeedItem
import kotlin.collections.HashMap

/**
 * Reads and consumes an RSS [feed] from a given URL.
 */
class RSSReader(feedUrl: String) {

    private var url: URL? = null
    private var feed: Feed? = null

    init {
        try {
            this.url = URL(feedUrl)
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        }
        this.feed = buildFeed()
    }

    /**
     * Return the [FeedItem] for the newest posting.
     */
    fun getNewestItem(): FeedItem? {
        return feed!!.items[0]
    }

    /**
     * Return info for the specific search term. Loops through the entire [Feed] starting with most recent to find the
     * first match.
     */
    fun getSpecificItem(searchTerm: String): FeedItem? {
        val matchValues = HashMap<FeedItem, Int>()
        if (searchTerm != "") {
            val separatedSearchTerms = searchTerm.toLowerCase().
                    replace(Regex("[,.]"), "").split(" ")
            for (item: FeedItem in feed!!.items) {
                // check for complete match
                if (searchTerm.toLowerCase() == item.title.toLowerCase()) {
                    return item
                }
                val possibleMatchesInSeparatedTitle = item.title.toLowerCase().
                        replace(Regex("[,.]"), "").split(" ")
                // Check for multiple search terms
                if (separatedSearchTerms.size > 1) {
                    // Need to calculate highest valued match
                    // First two strings are weighted higher
                    var numberOfMatches = 0
                    if (possibleMatchesInSeparatedTitle.contains(separatedSearchTerms[0])) {
                        for (term in separatedSearchTerms) {
                            possibleMatchesInSeparatedTitle
                                    .filter { term == it }
                                    .forEach {
                                        numberOfMatches++
                                        if (term == separatedSearchTerms[0] || term == separatedSearchTerms[1]) {
                                            numberOfMatches++
                                        }
                                    }
                        }
                        matchValues.put(item, numberOfMatches)
                    }
                } else {
                    // Check for occurrence of single term search string
                    if (possibleMatchesInSeparatedTitle.contains(searchTerm)) {
                        return item
                    }
                }
            }
        }
        return matchValues.maxBy { it.value }?.key
    }

    /**
     * Gets the new state of the RSS feed and checks it against the current [Feed]. If the new state of the RSS feed
     * contains more episodes, swap the existing feed into the new feed. Return true if the swap was successful.
     */
    fun pollFeed(): Boolean {
        val currFeed = buildFeed()
        if (this.feed!!.items.size < currFeed!!.items.size) {
            this.feed = currFeed
            return true
        }
        return false
    }

    /**
     * Reads from the rss xml and builds a [Feed].
     */
    private fun buildFeed(): Feed? {
        var feed: Feed? = null
        try {
            var isFeedHeader = true

            // Initialize empty feed values
            var description = ""
            var title = ""
            var link = ""
            var language = ""
            var copyright = ""
            var author = ""
            var publishDate = ""
            var guid = ""

            // Create an XMLInputFactory
            val inputFactory = XMLInputFactory.newInstance()
            // Setup a new eventReader
            val input = read()
            val eventReader = inputFactory.createXMLEventReader(input)
            // Parse the RSS XML
            while (eventReader.hasNext()) {
                val event = eventReader.nextEvent()
                if (event.isStartElement) {
                    val localPart = event.asStartElement().name.localPart
                    when (localPart) {
                        "item" -> {
                            if (isFeedHeader) {
                                isFeedHeader = false
                                feed = Feed(title, link, description, language,
                                        copyright, publishDate)
                            }
                            eventReader.nextEvent()
                        }
                        "title" -> title = eventReader.elementText
                        "description" -> description = eventReader.elementText
                        "link" -> link = eventReader.elementText
                        "guid" -> guid = eventReader.elementText
                        "language" -> language = eventReader.elementText
                        "author" -> author = eventReader.elementText
                        "pubDate" -> publishDate = eventReader.elementText
                        "copyright" -> copyright = eventReader.elementText
                    }
                } else if (event.isEndElement) {
                    if (event.asEndElement().name.localPart === "item") {
                        val newItem = FeedItem()
                        newItem.author = author
                        newItem.description = description
                        newItem.guid = guid
                        newItem.link = link
                        newItem.title = title
                        newItem.pubDate = publishDate
                        feed!!.items.add(newItem)
                        eventReader.nextEvent()
                        continue
                    }
                }
            }
            eventReader.close()
        } catch (e: XMLStreamException) {
            throw RuntimeException(e)
        }
        return feed
    }

    /**
     * Helper function to open input stream from RSS url.
     */
    private fun read(): InputStream {
        try {
            return url!!.openStream()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

}