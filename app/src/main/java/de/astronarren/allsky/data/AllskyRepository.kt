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
                
                val timelapseDoc = try { createConnection("$jsoupBaseUrl/videos/").get() } catch (e: Exception) { null }
                val keogramDoc = try { createConnection("$jsoupBaseUrl/keograms/").get() } catch (e: Exception) { null }
                val startrailDoc = try { createConnection("$jsoupBaseUrl/startrails/").get() } catch (e: Exception) { null }
                val imagesDoc = try { createConnection("$jsoupBaseUrl/images/").get() } catch (e: Exception) { null }
                val meteorDoc = try { createConnection("$jsoupBaseUrl/meteors/").get() } catch (e: Exception) { null }

                println("Debug: Successfully fetched HTML documents")

                val keograms = keogramDoc?.let { parseKeograms(it, authBaseUrl) } ?: emptyList()
                println("Debug: Found ${keograms.size} keograms")
                
                val startrails = startrailDoc?.let { parseStartrails(it, authBaseUrl) } ?: emptyList()
                println("Debug: Found ${startrails.size} startrails")
                
                val timelapses = timelapseDoc?.let { parseTimelapses(it, authBaseUrl) } ?: emptyList()
                println("Debug: Found ${timelapses.size} timelapses")

                val images = imagesDoc?.let { parseImages(it, authBaseUrl) } ?: emptyList()
                println("Debug: Found ${images.size} images")

                val meteors = meteorDoc?.let { parseMeteors(it, authBaseUrl) } ?: emptyList()
                println("Debug: Found ${meteors.size} meteors")

                AllskyContent(
                    timelapses = timelapses,
                    keograms = keograms,
                    startrails = startrails,
                    images = images,
                    meteors = meteors
                )
            } catch (e: Exception) {
                println("Debug: Error fetching allsky content: ${e.message}")
                throw e
            }
        }
    }

    private fun extractDate(href: String, element: org.jsoup.nodes.Element): String {
        // First try the specific div if it exists (for certain portal versions)
        val specificDate = element.select("div.day-text").text()
        if (specificDate.isNotEmpty()) return specificDate

        // Clean href for matching
        val cleanedHref = href.substringAfterLast("/")

        // Try YYYY-MM-DD or YYYYMMDD patterns
        val datePattern = Regex("(\\d{4})[-_]?(\\d{2})[-_]?(\\d{2})")
        val match = datePattern.find(cleanedHref)
        if (match != null) {
            val (year, month, day) = match.destructured
            return "$year-$month-$day"
        }

        // If no date found, return the filename itself or a placeholder
        return cleanedHref.substringBeforeLast(".")
    }

    private fun parseImages(doc: org.jsoup.nodes.Document, baseUrl: String): List<AllskyMedia> {
        val links = doc.select("a[href]")
        return links.mapNotNull { element ->
            try {
                val rawHref = element.attr("href")
                if (rawHref.contains("?") || rawHref.startsWith("..")) return@mapNotNull null
                
                // For images, we also accept directories (daily folders)
                val isDirectory = rawHref.endsWith("/")
                val isImage = rawHref.lowercase().endsWith(".jpg") || rawHref.lowercase().endsWith(".png")
                
                if ((isImage || isDirectory) && 
                    !rawHref.contains("keogram", ignoreCase = true) && 
                    !rawHref.contains("startrail", ignoreCase = true) && 
                    !rawHref.contains("image.jpg")) {
                    
                    val url = if (rawHref.startsWith("http")) rawHref else "${baseUrl.trimEnd('/')}/images/${rawHref.removePrefix("/")}"
                    AllskyMedia(
                        date = extractDate(rawHref, element),
                        url = url
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.date }.distinctBy { it.url }.take(20)
    }

    private fun parseTimelapses(doc: org.jsoup.nodes.Document, baseUrl: String): List<AllskyMedia> {
        val links = doc.select("a[href]")
        return links.mapNotNull { element ->
            try {
                val rawHref = element.attr("href")
                if (rawHref.contains("?") || rawHref.startsWith("..")) return@mapNotNull null
                
                val lowerHref = rawHref.lowercase()
                if (lowerHref.endsWith(".mp4") || lowerHref.endsWith(".webm") || lowerHref.endsWith(".mkv") || lowerHref.endsWith(".mov")) {
                    val url = if (rawHref.startsWith("http")) rawHref else "${baseUrl.trimEnd('/')}/videos/${rawHref.removePrefix("/")}"
                    AllskyMedia(
                        date = extractDate(rawHref, element),
                        url = url
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.date }.distinctBy { it.url }.take(20)
    }

    private fun parseKeograms(doc: org.jsoup.nodes.Document, baseUrl: String): List<AllskyMedia> {
        val links = doc.select("a[href]")
        return links.mapNotNull { element ->
            try {
                val rawHref = element.attr("href")
                if (rawHref.contains("?") || rawHref.startsWith("..")) return@mapNotNull null
                
                if (rawHref.contains("keogram", ignoreCase = true) && (rawHref.lowercase().endsWith(".jpg") || rawHref.lowercase().endsWith(".png"))) {
                    val url = if (rawHref.startsWith("http")) rawHref else "${baseUrl.trimEnd('/')}/keograms/${rawHref.removePrefix("/")}"
                    AllskyMedia(
                        date = extractDate(rawHref, element),
                        url = url
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.date }.distinctBy { it.url }.take(20)
    }

    private fun parseStartrails(doc: org.jsoup.nodes.Document, baseUrl: String): List<AllskyMedia> {
        val links = doc.select("a[href]")
        return links.mapNotNull { element ->
            try {
                val rawHref = element.attr("href")
                if (rawHref.contains("?") || rawHref.startsWith("..")) return@mapNotNull null
                
                if (rawHref.contains("startrail", ignoreCase = true) && (rawHref.lowercase().endsWith(".jpg") || rawHref.lowercase().endsWith(".png"))) {
                    val url = if (rawHref.startsWith("http")) rawHref else "${baseUrl.trimEnd('/')}/startrails/${rawHref.removePrefix("/")}"
                    AllskyMedia(
                        date = extractDate(rawHref, element),
                        url = url
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.date }.distinctBy { it.url }.take(20)
    }

    private fun parseMeteors(doc: org.jsoup.nodes.Document, baseUrl: String): List<AllskyMedia> {
        val links = doc.select("a[href]")
        return links.mapNotNull { element ->
            try {
                val rawHref = element.attr("href")
                if (rawHref.contains("?") || rawHref.startsWith("..")) return@mapNotNull null
                
                val lowerHref = rawHref.lowercase()
                if (lowerHref.endsWith(".jpg") || lowerHref.endsWith(".png") || lowerHref.endsWith(".mp4")) {
                    val url = if (rawHref.startsWith("http")) rawHref else "${baseUrl.trimEnd('/')}/meteors/${rawHref.removePrefix("/")}"
                    AllskyMedia(
                        date = extractDate(rawHref, element),
                        url = url
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.date }.distinctBy { it.url }.take(20)
    }
} 