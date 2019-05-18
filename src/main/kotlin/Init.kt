import MD.newLine
import models.ChronoPost
import models.RSSComment
import models.RSSPost
import models.Feed
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.time.DayOfWeek

/**
 * Main application thread. The top most level code for the bot. Contains two threads, one for performing checks on the
 * [Feed] and updating the subreddit, and another for handling search requests made to the [Feed]. This function is
 * intentionally customizable.
 */
fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Invalid arguments.")
        return
    }

    // Initialize Bot
    println("Initializing bot and feed listener. " + LocalDateTime.now().toString())
    val rssBot = RSSBot()
    val executor = Executors.newScheduledThreadPool(2)

    // Posting bot script will execute every minute.
    // Set custom actions for bot here based on criteria.
    val botRunner = Runnable {
        run {
            try {
                // Keep track of time vars
                val date = LocalDateTime.now()
                val time = date.toLocalTime()
                val today = date.dayOfWeek

                // RSS Feed check.
                if (rssBot.hasNewFeedItem()) {
                    // Set your own values here for Post title and Text content.
                    val newRSSItem = rssBot.getNewestFeedItem()
                    val post = RSSPost()
                    post.postTitle = newRSSItem!!.title + " | Discussion Thread"
                    post.postText = "On [MaxFun](http://www.maximumfun.org/shows/adventure-zone).$newLine" +
                            "The [show's RSS feed](http://adventurezone.libsyn.com/rss).$newLine" +
                            "[TAZ in iTunes/Apple Podcasts](https://itunes.apple.com/us/podcast/the-adventure-zone/" +
                            "id947899573?mt=2).$newLine" +
                            newRSSItem.description.replace(Regex("<.*?>"),"")
                            
                    rssBot.makeRSSSelfPost(post, true, true)
                }

                if (today == DayOfWeek.SUNDAY && time.hour == 12 && time.minute == 0) {
                    val post = ChronoPost()
                    post.postTitle = "Sunday Off-Topic Discussion"
                    post.postText = "This thread is for all those posts you want to make during the week that break " +
                            "the relevancy rules. Go hog wild.$newLine" +
                            "The thread is not flagged for spoilers so that " +
                            "everyone can enjoy it, please keep that in mind and use spoiler tags if you want to " +
                            "discuss anything pertaining to the recent arc."
                    rssBot.makeChronoPost(post)
                }

            } catch (e: Exception) {
                println("RSS Application thread encountered an error while running...")
                println(e.message)
            }
        }
    }

    // Episode fetch bot will execute every 10 seconds.
    // It will check the associated reddit account for user mentions.
    val episodeFetch = Runnable {
        run {
            try {
                // Check for Incoming Requests
                for (m in rssBot.getUsernameMentions()) {
                    // Process request to see if search is valid
                    val request = rssBot.processFeedItemRequest(m)
                    if (request != null) {
                        if (request.title != "") {
                            // A valid request was made and a match has been found
                            val comment = RSSComment()
                            comment.parentId = m.id
                            comment.commentText = "[${request.title}](${request.link})"
                            rssBot.makeCommentReply(comment)
                        } else {
                            // A valid request was made and a match has NOT been found
                            val comment = RSSComment()
                            comment.parentId = m.id
                            comment.commentText = "I could not find that."
                            rssBot.makeCommentReply(comment)
                        }
                    }
                }
            } catch (e: Exception) {
                println("Episode search thread encountered an error while running...")
                println(e.message)
            }
        }
    }

    try {
        executor.scheduleAtFixedRate(botRunner, 0, 1, TimeUnit.MINUTES)
        executor.scheduleAtFixedRate(episodeFetch, 0, 10, TimeUnit.SECONDS)
    } catch (t: Throwable) {
        println("Application thread encountered an error while executing...")
        println(LocalDateTime.now().toString())
        println("--------------------")
        println(t.message)
    }
}

/**
 * Optional function for parsing the RSS feed links into a customized link.
 */
private fun customLinkBuilder(rssLink: String): String {
    val customUrl = "https://www.google.com/"
    // Parse the link as needed...
    return customUrl
}

/**
 * Markdown to Url encoder. Collection of global string values. These are used to allow for proper comment formatting.
 */
object MD {
    const val newLine = "\n\n "
}