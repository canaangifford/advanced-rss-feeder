package utils

import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL

import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.events.Characters
import javax.xml.stream.events.XMLEvent
import models.Feed
import models.FeedItem

/**
 * Reads and consumes an RSS Feed from a given URL.
 * Includes a method to check the feed for new a post.
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
     * Return the info for the new posting.
     */
    fun getNewestItem(): FeedItem? {
        return feed!!.items[0]
    }

    /**
     * Return info for the specific searched item.
     */
    fun getSpecificItem(searchTerm: String): FeedItem? {
        for(item: FeedItem in feed!!.items) {
            if(item.title.contains(searchTerm, ignoreCase = true)) {
                return item
            }
        }
        return null
    }

    /**
     * Checks the current state of the RSS feed with the Initialized feed.
     */
    fun pollFeed(): Boolean {
        val currFeed = buildFeed()
        if(this.feed!!.items.size < currFeed!!.items.size) {
            this.feed = currFeed
            return true
        }
        return false
    }

    /**
     * Reads from the rss xml and builds a Feed.
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
            var pubdate = ""
            var guid = ""

            // Create an XMLInputFactory
            val inputFactory = XMLInputFactory.newInstance()
            // Setup a new eventReader
            val input = read()
            val eventReader = inputFactory.createXMLEventReader(input)
            // Parse the RSS XML
            while (eventReader.hasNext()) {
                var event = eventReader.nextEvent()
                if (event.isStartElement) {
                    val localPart = event.asStartElement().name.localPart
                    when (localPart) {
                        "item" -> {
                            if (isFeedHeader) {
                                isFeedHeader = false
                                feed = Feed(title, link, description, language,
                                        copyright, pubdate)
                            }
                            event = eventReader.nextEvent()
                        }
                        "title" -> title = getCharacterData(event, eventReader)
                        "description" -> description = getCharacterData(event, eventReader)
                        "link" -> link = getCharacterData(event, eventReader)
                        "guid" -> guid = getCharacterData(event, eventReader)
                        "language" -> language = getCharacterData(event, eventReader)
                        "author" -> author = getCharacterData(event, eventReader)
                        "pubDate" -> pubdate = getCharacterData(event, eventReader)
                        "copyright" -> copyright = getCharacterData(event, eventReader)
                    }
                } else if (event.isEndElement) {
                    if (event.asEndElement().name.localPart === "item") {
                        val newItem = FeedItem()
                        newItem.author = author
                        newItem.description = description
                        newItem.guid = guid
                        newItem.link = link
                        newItem.title = title
                        newItem.pubDate = pubdate
                        feed!!.items.add(newItem)
                        event = eventReader.nextEvent()
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
     * Helper function to handle a single XML event.
     */
    @Throws(XMLStreamException::class)
    private fun getCharacterData(event: XMLEvent, eventReader: XMLEventReader): String {
        var event = event
        var result = ""
        event = eventReader.nextEvent()
        if (event is Characters) {
            result = event.asCharacters().data
        }
        return result
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