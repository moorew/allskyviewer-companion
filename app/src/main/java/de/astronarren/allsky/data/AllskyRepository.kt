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
                var baseUrl = userPreferences.getAllskyUrl()
                if (baseUrl.isEmpty()) {
                    return@withContext AllskyContent(emptyList(), emptyList(), emptyList())
                }

                if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
                    throw IllegalArgumentException("Invalid URL format")
                }

                baseUrl = baseUrl.trimEnd('/')

                val username = userPreferences.getUsername()
                val password = userPreferences.getPassword()
                
                fun createConnection(url: String): Connection {
                    val conn = Jsoup.connect(url)
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        val basicAuth = "Basic " + Base64.encodeToString("$username:$password".toByteArray(), Base64.NO_WRAP)
                        conn.header("Authorization", basicAuth)
                    }
                    return conn
                }

                // Check if we need to append /allsky
                try {
                    val testConn = createConnection("$baseUrl/videos/")
                    testConn.execute()
                } catch (e: org.jsoup.HttpStatusException) {
                    if (e.statusCode == 404) {
                        try {
                            val altConn = createConnection("$baseUrl/allsky/videos/")
                            if (altConn.execute().statusCode() == 200) {
                                baseUrl = "$baseUrl/allsky"
                                userPreferences.saveAllskyUrl(baseUrl)
                                println("Debug: Auto-corrected baseUrl to $baseUrl")
                            }
                        } catch (e2: Exception) {
                            println("Debug: Auto-correct failed: ${e2.message}")
                        }
                    }
                } catch (e: Exception) {
                    println("Debug: BaseUrl test failed: ${e.message}")
                }
                
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