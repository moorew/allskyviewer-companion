package de.astronarren.allsky.data

import org.jsoup.Jsoup
import org.jsoup.Connection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Base64

class AllskyRepository(private val userPreferences: UserPreferences) {
    suspend fun getAllContent(): AllskyContent {
        return withContext(Dispatchers.IO) {
            try {
                val baseUrl = userPreferences.getAllskyUrl()
                if (baseUrl.isEmpty()) {
                    return@withContext AllskyContent(emptyList(), emptyList(), emptyList())
                }

                if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
                    throw IllegalArgumentException("Invalid URL format")
                }

                val username = userPreferences.getUsername()
                val password = userPreferences.getPassword()
                
                // Base URL for Jsoup (without credentials to avoid parsing issues)
                val jsoupBaseUrl = baseUrl
                
                // Base URL with credentials for Coil AsyncImage and ExoPlayer
                val authBaseUrl = if (username.isNotEmpty() && password.isNotEmpty()) {
                    val uri = android.net.Uri.parse(baseUrl)
                    val builder = uri.buildUpon()
                    val authority = "${android.net.Uri.encode(username)}:${android.net.Uri.encode(password)}@${uri.authority}"
                    builder.encodedAuthority(authority).build().toString()
                } else {
                    baseUrl
                }
                
                fun createConnection(url: String): Connection {
                    val conn = Jsoup.connect(url)
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        val basicAuth = "Basic " + Base64.encodeToString("$username:$password".toByteArray(), Base64.NO_WRAP)
                        conn.header("Authorization", basicAuth)
                    }
                    return conn
                }

                println("Debug: Fetching content from Allsky: $jsoupBaseUrl")
                
                val timelapseDoc = createConnection("$jsoupBaseUrl/videos/").get()
                val keogramDoc = createConnection("$jsoupBaseUrl/keograms/").get()
                val startrailDoc = createConnection("$jsoupBaseUrl/startrails/").get()
                val imagesDoc = try { createConnection("$jsoupBaseUrl/images/").get() } catch (e: Exception) { null }

                println("Debug: Successfully fetched HTML documents")

                val keograms = parseKeograms(keogramDoc, authBaseUrl)
                println("Debug: Found ${keograms.size} keograms")
                
                val startrails = parseStartrails(startrailDoc, authBaseUrl)
                println("Debug: Found ${startrails.size} startrails")
                
                val timelapses = parseTimelapses(timelapseDoc, authBaseUrl)
                println("Debug: Found ${timelapses.size} timelapses")

                val images = imagesDoc?.let { parseImages(it, authBaseUrl) } ?: emptyList()
                println("Debug: Found ${images.size} images")

                AllskyContent(
                    timelapses = timelapses,
                    keograms = keograms,
                    startrails = startrails,
                    images = images
                )
            } catch (e: Exception) {
                println("Debug: Error fetching allsky content: ${e.message}")
                throw e
            }
        }
    }

    private fun parseImages(doc: org.jsoup.nodes.Document, baseUrl: String): List<AllskyMedia> {
        return doc.select("div.archived-files a").mapNotNull { element ->
            try {
                val href = element.attr("href")
                // Typically Allsky stores daily images in subfolders, 
                // but let's look for any .jpg that isn't a keogram/startrail if we are in images/
                if (href.endsWith(".jpg") || href.endsWith("/")) {
                    val dateText = element.select("div.day-text").text()
                    AllskyMedia(
                        date = dateText,
                        url = "$baseUrl/images/$href"
                    )
                } else null
            } catch (e: Exception) {
                println("Debug: Error parsing image element: ${e.message}")
                null
            }
        }
    }

    private fun parseTimelapses(doc: org.jsoup.nodes.Document, baseUrl: String): List<AllskyMedia> {
        return doc.select("div.archived-files a").mapNotNull { element ->
            try {
                val href = element.attr("href")
                if (href.endsWith(".mp4")) {
                    val dateText = element.select("div.day-text").text()
                    AllskyMedia(
                        date = dateText,
                        url = "$baseUrl/videos/$href"
                    )
                } else null
            } catch (e: Exception) {
                println("Debug: Error parsing timelapse element: ${e.message}")
                null
            }
        }
    }

    private fun parseKeograms(doc: org.jsoup.nodes.Document, baseUrl: String): List<AllskyMedia> {
        return doc.select("div.archived-files a").mapNotNull { element ->
            try {
                val href = element.attr("href")
                if (href.contains("keogram") && href.endsWith(".jpg")) {
                    val dateText = element.select("div.day-text").text()
                    AllskyMedia(
                        date = dateText,
                        url = "$baseUrl/keograms/$href"
                    )
                } else null
            } catch (e: Exception) {
                println("Debug: Error parsing keogram element: ${e.message}")
                null
            }
        }
    }

    private fun parseStartrails(doc: org.jsoup.nodes.Document, baseUrl: String): List<AllskyMedia> {
        return doc.select("div.archived-files a").mapNotNull { element ->
            try {
                val href = element.attr("href")
                if (href.contains("startrail") && href.endsWith(".jpg")) {
                    val dateText = element.select("div.day-text").text()
                    AllskyMedia(
                        date = dateText,
                        url = "$baseUrl/startrails/$href"
                    )
                } else null
            } catch (e: Exception) {
                println("Debug: Error parsing startrail element: ${e.message}")
                null
            }
        }
    }
} 