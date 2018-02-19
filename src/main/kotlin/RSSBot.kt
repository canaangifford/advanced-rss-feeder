import models.ChronoPost
import models.FeedItem
import models.RSSPost
import utils.PropertiesReader
import utils.RSSReader
import net.dean.jraw.RedditClient
import net.dean.jraw.http.UserAgent
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.models.SubmissionKind
import net.dean.jraw.oauth.OAuthHelper
import net.dean.jraw.references.SubredditReference
import net.dean.jraw.models.Listing
import net.dean.jraw.models.Message
import net.dean.jraw.references.CommentReference
import net.dean.jraw.references.InboxReference


/**
 * The main Bot class.
 */
class RSSBot {

    private var reader: RSSReader? = null
    private lateinit var redditClient: RedditClient
    private var subreddit: String = ""
    private var subredditReference: SubredditReference? = null

    init {
        // Reads app properties file
        try {
            val propReader = PropertiesReader()
            val appProps = propReader.parseProperties()

            // Builds the RSSReader with initial state of the feed
            reader = RSSReader(appProps.rssFeedUrl)

            // Builds JRAW Client
            subreddit = appProps.subreddit

            val credentials = Credentials.script(appProps.username, appProps.password, appProps.id, appProps.secret)
            val userAgent =  UserAgent(appProps.platform, appProps.appId, appProps.version, appProps.devRedditUser)
            val adapter = OkHttpNetworkAdapter(userAgent)

            redditClient = OAuthHelper.automatic(adapter, credentials)
            subredditReference = redditClient.subreddit(subreddit)

        } catch(e: Exception) {
            println("Error reading properties file. Ensure the formatting is as specified in the readme file.")
            println(e.message)
        }
    }

    /**
     * Creates a new 'self' text Post based on an RSS Post.
     *
     * @post The RSSPost model to use
     */
    fun makeRSSTextPost(post: RSSPost) {
        subredditReference!!.submit(SubmissionKind.SELF, post.postTitle, post.postText, false)
    }

    /**
     * Creates a new link Post based on the new RSS Feed item.
     *
     * @post The RSSPost model to use
     */
    fun makeRSSLinkPost(post: RSSPost) {
        subredditReference!!.submit(SubmissionKind.LINK, post.postTitle, post.postUrl, false)
    }

    /**
     * Creates a new 'self' text Post based on a chronological event.
     */
    fun makeChronoPost(post: ChronoPost) {
        subredditReference!!.submit(SubmissionKind.SELF, post.postTitle, post.postText, false)
    }

    /**
     * Make a comment.
     *
     * @comment Text for the comment
     * @id The id of the parent to reply to.
     */
    fun makeCommentReply(comment: String, id: String) {
        CommentReference(redditClient, id).reply(comment)
    }

    /**
     * Submits a reddit comment with the appropriate fetched RSS item.
     *
     * @comment Text for the comment.
     * @url The link address associated with the RSS item.
     * @id The id of the parent to reply to.
     */
    fun makeCommentReplyWithRSSItemLink(title: String, url: String, id: String) {
        val formattedRSSLink = "[$title]($url)"
        CommentReference(redditClient, id).reply(formattedRSSLink)
    }

    /**
     * Self check for username mentions.
     *
     * @return The list of unread PMs with user mentions.
     */
    private fun getUsernameMentions(): ArrayList<Message> {
        val mentionedMessages: ArrayList<Message> = ArrayList(0)
        val inbox: InboxReference = redditClient.me().inbox()
        val mentions = inbox.iterate("mentions").limit(10).build()
        for (page: Listing<Message> in mentions) {
            for(m: Message in page) {
                if(m.isUnread) {
                    mentionedMessages.add(m)
                    inbox.markRead(true, m.fullName)
                }
            }
        }
        return mentionedMessages
    }

    /**
     * Process the current list of episode requests made to the bot.
     * Searches RSS feed for the requested item. If a match is found, a comment
     * containing a link to the item is submitted as a reply.
     */
    fun processEpisodeRequests() {
        val requests = getUsernameMentions()
        for (m: Message in requests) {
            val searchTerm = m.body.substring(m.body.indexOf("`"), m.body.lastIndexOf("`"))
            val item = getSpecificFeedItem(searchTerm.removePrefix("`"))
            if (item != null) {
                makeCommentReplyWithRSSItemLink(item.title, item.link, m.id)
            } else {
                makeCommentReply("I could not find that.", m.id)
            }
        }
    }

    /**
     * Polls the current feed.
     * Determines if the current internal model matches the live RSS feed.
     */
    fun hasNewFeedItem(): Boolean {
        return reader!!.pollFeed()
    }

    /**
     * Returns the newest item in the RSS feed.
     */
    fun getNewestFeedItem(): FeedItem? {
        return reader!!.getNewestItem()
    }

    /**
     * Fetches the item based on title search data.
     *
     * @searchText The episode title, or part of the episode title,
     * that begins with `!`
     */
    private fun getSpecificFeedItem(searchText: String): FeedItem? {
        return reader!!.getSpecificItem(searchText.removePrefix("`"))
    }
}