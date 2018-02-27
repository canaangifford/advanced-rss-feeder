package utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.InputStream

/**
 * Builds a list of App Properties from the settings json file.
 */
class PropertiesReader {

    // Data object for the list of App Properties
    data class AppProperties(val id: String, val secret: String, val username: String, val password: String,
                             val redirectUri: String, val subreddit: String, val rssFeedUrl: String,
                             val platform: String, val appId: String, val version: String, val devRedditUser: String)

    /**
     * Reads the settings file and returns the AppProperties data object.
     */
    fun parseProperties(): AppProperties {
        val propertiesFile = PropertiesReader::class.java.getResourceAsStream("/app-settings.json")
        return convert(propertiesFile)
    }

    private fun convert(jsonFile: InputStream): AppProperties {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)

        return mapper.readValue(jsonFile)
    }
}