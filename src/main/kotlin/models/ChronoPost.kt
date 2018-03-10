package models

/**
 * Representation data for a reddit post based on a scheduled event.
 */
data class ChronoPost(var postTitle: String = "",
                      var postText: String = "",
                      var postUrl: String = "")