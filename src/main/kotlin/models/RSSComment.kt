package models

/**
 * Representation data for a reddit comment based on an RSS feed item.
 */
data class RSSComment(var commentText: String = "",
                      var parentId: String = "")