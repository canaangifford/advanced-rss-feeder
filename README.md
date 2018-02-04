# advanced-rss-feeder

A framework for a reddit bot that includes a basic RSS reader and extended moderation utility.
Perfect for posting regular updates and moderating a subreddit that follows media content from RSS feeds.

Uses [JRAW](https://github.com/mattbdean/JRAW) as a reddit API wrapper.

---

## How to use

1. Create a file called app-settings.json in the ../src/main/resources/ directory.

2. Add the following json to the file, entering the appropriate data.

        {
          "id":"",
          "secret":"",
          "username":"",
          "password":"",
          "redirectUri":"",
          "subreddit":"",
          "rssFeedUrl":"",
          "platform":"desktop",
          "appId":"advanced-rss-feeder",
          "version":"v1.0",
          "devRedditUser":""
        }

3. Open Init.kt and add any actions you would like the bot to perform.

4. Run the application with:

    > ./gradlew run

