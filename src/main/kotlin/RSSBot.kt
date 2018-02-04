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
import net.dean.jraw.references.CommentReference
import net.dean.jraw.references.SubredditReference

/**
 * The main Bot class.
 */
class RSSBot {

    private var reader: RSSReader? = null
    private var redditClient: RedditClient? = null
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
            subredditReference = redditClient!!.subreddit(subreddit)

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
     * Submits a reddit comment with the appropriate fetched RSS item.
     *
     * @comment Text for the comment.
     * @url The link address associated with the RSS item.
     */
    fun makeCommentWithRSSItemLink(comment: String, url: String) {

    }

    /**
     * Polls the current feed.
     */
    fun hasNewFeedItem(): Boolean {
        return reader!!.pollFeed()
    }

    /**
     * Returns the newest item in the RSS feed.
     */
    fun getNewFeedItem(): FeedItem? {
        return reader!!.getNewestItem()
    }

    /**
     * Fetches the item based on title search data.
     *
     * @searchText The episode title, or part of the episode title,
     * that begins with `!`
     */
    fun getSpecificFeedItem(searchText: String): FeedItem? {
        if (searchText.startsWith("!")) {
            return reader!!.getSpecificItem(searchText.removePrefix("!"))
        }
        return null
    }
}