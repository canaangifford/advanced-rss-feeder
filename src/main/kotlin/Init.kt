@file:JvmName("advanced-rss-feeder")

import models.RSSPost
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Invalid arguments.")
        return
    }

    var runtime = 0

    // Initialize Bot
    println("Initializing bot and feed listener.")
    val rssBot = RSSBot()

    // Bot script will execute every 5 minutes
    // Set custom actions for bot here based on criteria.
    val executor = Executors.newScheduledThreadPool(1)
    val botRunner = Runnable {
         run {
            try {
                // RSS Feed check.
                if(rssBot.hasNewFeedItem()) {
                    // Set your own values here for Post title and Text content.
                    val newRSSItem = rssBot.getNewFeedItem()
                    val post = RSSPost()
                    post.postTitle = newRSSItem!!.title + " Discussion Thread"
                    post.postText = "New Episode!"
                    rssBot.makeRSSTextPost(post)
                    println("Success!    " + post.postTitle)
                }

                if(false) {
                    // can set additional actions for the bot based on datetime criteria etc...
                }

                if(false) {
                    // ...
                }
            } catch(e: Exception) {
                println("Application thread encountered an error while running...")
                println(e.message)
            }

            runtime++
            if(runtime%30==0) {
                // Re-authenticate the session every 30 minutes.
                // This is probably excessive but the app will be unable to post without it.
                //rssBot.authenticateSelf()
            }
        }
    }

    try {
        executor.scheduleAtFixedRate(botRunner, 0, 5, TimeUnit.MINUTES)
    } catch (t: Throwable) {
        println("Application thread encountered an error while executing...")
        println("Uptime was: $runtime minutes")
        println("--------------------")
        println(t.message)
    }
}