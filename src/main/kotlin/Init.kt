import models.RSSPost
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.time.DayOfWeek
import java.time.LocalTime


/**
 * Main application thread.
 */
fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Invalid arguments.")
        return
    }

    var runtime = 0

    // Initialize Bot
    println("Initializing bot and feed listener.")
    val rssBot = RSSBot()

    val executor = Executors.newScheduledThreadPool(2)

    // Posting bot script will execute every minute.
    // Set custom actions for bot here based on criteria.
    val botRunner = Runnable {
         run {
            try {
                val date = LocalDateTime.now()
                val time = date.toLocalTime()
                val today = date.dayOfWeek

                // RSS Feed check.
                if(rssBot.hasNewFeedItem()) {
                    // Set your own values here for Post title and Text content.
                    val newRSSItem = rssBot.getNewFeedItem()
                    val post = RSSPost()
                    post.postTitle = newRSSItem!!.title + " Discussion Thread"
                    post.postText = "New Episode!\nExcellent Test!"
                    rssBot.makeRSSTextPost(post)
                    println("Success!    " + post.postTitle)
                }

                if(today == DayOfWeek.SATURDAY && time == LocalTime.NOON) {
                    println("Saturday Post")
                }

                if(false) {
                    // can set additional post actions for the bot based on further datetime criteria etc...
                }
            } catch(e: Exception) {
                println("RSS Application thread encountered an error while running...")
                println(e.message)
            }
            runtime++
        }
    }

    // Episode fetch bot will execute every 10 seconds.
    // It will poll the subreddit for comments containing a given flag + search term.
    val episodeFetch = Runnable {
        run {
            try {

            } catch(e: Exception) {
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
        println("Uptime was: $runtime minutes")
        println("--------------------")
        println(t.message)
    }
}