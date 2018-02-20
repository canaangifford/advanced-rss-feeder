import models.ChronoPost
import models.RSSPost
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.time.DayOfWeek

/**
 * Main application thread.
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
                    val post = RSSPost()
                    val newRSSItem = rssBot.getNewestFeedItem()
                    post.postTitle = newRSSItem!!.title + " Discussion Thread"
                    post.postUrl = newRSSItem.link
                    rssBot.makeRSSLinkPost(post)

                    println("Success!    " + post.postTitle)
                }

                if (today == DayOfWeek.SATURDAY && time.hour == 12 && time.minute == 0) {
                    val post = ChronoPost()
                    post.postTitle = "Saturday Post"
                    post.postText = "Saturday Post Content!"
                    rssBot.makeChronoPost(post)
                }

                if (false) {
                    // can set additional post actions for the bot based on further datetime criteria etc...
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
                rssBot.processEpisodeRequests()
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
