package models

/**
 * Representation data for a reddit post based on an RSS feed item.
 */
data class RSSPost(var postTitle: String = "",
                   var postText: String = "",
                   var postUrl: String = "",
                   var id: String = "")
