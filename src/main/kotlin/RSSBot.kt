import models.ChronoPost
import models.RSSComment
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
import net.dean.jraw.references.SubmissionReference


/**
 * The main Bot class. This class holds an [RSSReader] object which it uses to perform actions on the [Feed]. It also
 * builds a [redditClient] to authenticate and interact with Reddit.
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
            val userAgent = UserAgent(appProps.platform, appProps.appId, appProps.version, appProps.devRedditUser)
            val adapter = OkHttpNetworkAdapter(userAgent)

            redditClient = OAuthHelper.automatic(adapter, credentials)
            redditClient.logHttp = false // should be off by default honestly
            subredditReference = redditClient.subreddit(subreddit)

        } catch (e: Exception) {
            println("Error reading properties file. Ensure the formatting is as specified in the readme file.")
            println(e.message)
        }
    }

    /**
     * Creates a new link [post] based on the new RSS Feed item.
     */
    fun makeRSSLinkPost(post: RSSPost, spoiler: Boolean = false, sticky: Boolean = false) {
        val submission = subredditReference!!.submit(SubmissionKind.LINK, post.postTitle, post.postUrl, false)
        submission.flagAsSpoiler(spoiler)
        submission.stickyPost(sticky)
    }

    /**
     * Creates a new 'self' [post] based on the new RSS Feed item.
     */
    fun makeRSSSelfPost(post: RSSPost) {
        subredditReference!!.submit(SubmissionKind.SELF, post.postTitle, post.postText, false)
    }

    /**
     * Creates a new 'self' text [post] based on a chronological event.
     */
    fun makeChronoPost(post: ChronoPost) {
        subredditReference!!.submit(SubmissionKind.SELF, post.postTitle, post.postText, false)
    }

    /**
     * Make a Reddit [comment] based on an [RSSComment] model.
     */
    fun makeCommentReply(comment: RSSComment) {
        CommentReference(redditClient, comment.parentId).reply(comment.commentText)
    }

    /**
     * Self check for username mentions. Returns the list of unread PMs with user mentions.
     */
    fun getUsernameMentions(): ArrayList<Message> {
        val mentionedMessages: ArrayList<Message> = ArrayList(0)
        val inbox: InboxReference = redditClient.me().inbox()
        val mentions = inbox.iterate("mentions").limit(10).build()
        for (page: Listing<Message> in mentions) {
            for (m: Message in page) {
                if (m.isUnread) {
                    mentionedMessages.add(m)
                    inbox.markRead(true, m.fullName)
                }
            }
        }
        return mentionedMessages
    }

    /**
     * Processes a potential FeedItem message [request] submitted to the bot. Searches RSS feed for the requested item.
     * If a match is found, the item is returned.
     */
    fun processFeedItemRequest(request: Message): FeedItem? {
        if (request.body.count { it == '`' } >= 2) {
            val searchTerm = request.body.substring(request.body.indexOf("`"), request.body.lastIndexOf("`"))
            val item = getSpecificFeedItem(searchTerm.removePrefix("`"))
            return when (item != null) {
                true -> item
                false -> FeedItem() // Return empty item if request was valid but there was no match
            }
        }
        return null
    }

    /**
     * Polls the current feed. Determines if the current internal model matches the live RSS feed.
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
     * Fetches the item based on title [searchText] surrounded by `...` notation.
     */
    private fun getSpecificFeedItem(searchText: String): FeedItem? {
        return reader!!.getSpecificItem(searchText)
    }
}