package de.astronarren.allsky.data

import org.jsoup.Jsoup
import org.jsoup.Connection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Base64

class AllskyRepository(private val userPreferences: UserPreferences) {
    suspend fun getAllContent(date: String? = null): AllskyContent {
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
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(15000)
                        .followRedirects(true)
                    
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        val basicAuth = "Basic " + Base64.encodeToString("$username:$password".toByteArray(), Base64.NO_WRAP)
                        conn.header("Authorization", basicAuth)
                    }
                    return conn
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
                
                // Strategy: Try portal page first, fallback to direct directory
                suspend fun fetchDoc(path: String, portalPage: String): org.jsoup.nodes.Document? {
                    var doc: org.jsoup.nodes.Document? = null
                    try {
                        val portalUrl = "$jsoupBaseUrl/index.php?page=$portalPage"
                        println("Debug: Trying portal page: $portalUrl")
                        doc = createConnection(portalUrl).get()
                    } catch (e: org.jsoup.HttpStatusException) {
                        if (e.statusCode == 401 || e.statusCode == 403) throw e
                        println("Debug: Portal page returned ${e.statusCode}")
                    } catch (e: Exception) {
                        println("Debug: Portal page fetch failed: ${e.message}")
                    }

                    // Simple heuristic: if the document contains list_ or media elements, it's likely the right page
                    val hasPortalContent = doc != null && (
                        doc.select("a[href*=.mp4], a[href*=.jpg], img[src*=.jpg], source[src*=.mp4]").isNotEmpty() ||
                        doc.select("div.functionsListFileType, div.functionsListTypeImg").isNotEmpty()
                    )
                    
                    if (doc != null && hasPortalContent) {
                        println("Debug: Portal page has valid content")
                        return doc
                    }

                    return try {
                        val dirUrl = "$jsoupBaseUrl/$path"
                        println("Debug: Falling back to direct directory: $dirUrl")
                        createConnection(dirUrl).get()
                    } catch (e: org.jsoup.HttpStatusException) {
                        if (e.statusCode == 401 || e.statusCode == 403) throw e
                        doc // Return the portal doc even if it didn't have obvious content, as a last resort
                    } catch (e: Exception) {
                        doc
                    }
                }

                val dayParam = date ?: "All"
                val timelapseDoc = fetchDoc("videos/", "list_videos&day=$dayParam")
                val keogramDoc = fetchDoc("keograms/", "list_keograms&day=$dayParam")
                val startrailDoc = fetchDoc("startrails/", "list_startrails&day=$dayParam")
                val imagesDoc = if (date != null && date != "All") fetchDoc("images/", "list_images&day=$date") else fetchDoc("images/", "list_days")
                val meteorDoc = fetchDoc("meteors/", "list_meteors&day=$dayParam")

                val keograms = keogramDoc?.let { parseKeograms(it, authBaseUrl) } ?: emptyList()
                val startrails = startrailDoc?.let { parseStartrails(it, authBaseUrl) } ?: emptyList()
                val timelapses = timelapseDoc?.let { parseTimelapses(it, authBaseUrl) } ?: emptyList()

                var images = imagesDoc?.let { parseImages(it, authBaseUrl) } ?: emptyList()
                
                // If we found day-links in images, try to fetch the most recent day to get actual images
                // Use jsoupBaseUrl for fetching, not authBaseUrl!
                val dayLink = images.firstOrNull { it.url.contains("day=") || it.url.endsWith("/") }
                if (dayLink != null && (images.isEmpty() || images.all { it.url.contains("page=list_images") || it.url.endsWith("/") })) {
                    try {
                        // Extract the query part or path from the dayLink.url
                        val fetchUrl = if (dayLink.url.contains("?")) {
                            "$jsoupBaseUrl/index.php?" + dayLink.url.substringAfter("?")
                        } else if (dayLink.url.startsWith("http")) {
                            // If it's a full URL, we need to strip credentials if they exist
                            val uri = android.net.Uri.parse(dayLink.url)
                            uri.buildUpon().encodedAuthority(uri.authority?.substringAfter("@")).build().toString()
                        } else {
                            "$jsoupBaseUrl/${dayLink.url.removePrefix("/")}"
                        }

                        println("Debug: Fetching nested day content: $fetchUrl")
                        val dayDoc = createConnection(fetchUrl).get()
                        val dailyImages = parseImages(dayDoc, authBaseUrl)
                        if (dailyImages.isNotEmpty()) {
                            images = dailyImages
                        }
                    } catch (e: org.jsoup.HttpStatusException) {
                        if (e.statusCode == 401 || e.statusCode == 403) throw e
                        println("Debug: Failed to fetch daily images: ${e.message}")
                    } catch (e: Exception) {
                        println("Debug: Failed to fetch daily images: ${e.message}")
                    }
                }
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
            } catch (e: org.jsoup.HttpStatusException) {
                println("Debug: HTTP Error fetching allsky content: ${e.statusCode}")
                if (e.statusCode == 401 || e.statusCode == 403) {
                    throw Exception("Authentication Required (401). Please enter your Username and Password in Settings.")
                }
                throw Exception("Server returned error: ${e.statusCode}")
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

        // Try to find a date in the href (e.g., day=20240101 or allsky-20240101.mp4)
        val datePattern = Regex("(\\d{4})[-_]?(\\d{2})[-_]?(\\d{2})")
        val match = datePattern.find(cleanedHref)
        if (match != null) {
            val (year, month, day) = match.destructured
            return "$year-$month-$day"
        }

        // Try link text if it looks like a date
        val text = element.text().trim()
        val textMatch = datePattern.find(text)
        if (textMatch != null) {
            val (year, month, day) = textMatch.destructured
            return "$year-$month-$day"
        }

        // If no date found, return the filename itself or a placeholder
        return cleanedHref.substringBeforeLast(".").ifEmpty { cleanedHref }
    }

    private fun normalizeUrl(rawHref: String, baseUrl: String, subDir: String): String? {
        if (rawHref.contains("javascript:") || rawHref.startsWith("#")) return null
        
        // Handle Portal links like index.php?page=list_images&day=20240101
        if (rawHref.contains("page=list_")) {
            return if (rawHref.startsWith("http")) rawHref else "${baseUrl.trimEnd('/')}/${rawHref.removePrefix("/")}"
        }

        val lowerHref = rawHref.lowercase()
        val isMedia = lowerHref.endsWith(".jpg") || lowerHref.endsWith(".png") || 
                      lowerHref.endsWith(".mp4") || lowerHref.endsWith(".webm") || 
                      lowerHref.endsWith(".mov") || lowerHref.endsWith(".mkv") ||
                      rawHref.endsWith("/")

        if (!isMedia) return null

        return when {
            rawHref.startsWith("http") -> rawHref
            rawHref.startsWith("/") -> "${baseUrl.trimEnd('/')}$rawHref"
            else -> "${baseUrl.trimEnd('/')}/$subDir/${rawHref.removePrefix("./")}"
        }
    }

    private fun parseImages(doc: org.jsoup.nodes.Document, baseUrl: String): List<AllskyMedia> {
        val elements = doc.select("a[href], img[src]")
        return elements.mapNotNull { element ->
            try {
                var rawHref = element.attr("href").ifEmpty { element.attr("src") }
                if (rawHref.contains("?") && !rawHref.contains("page=list_")) return@mapNotNull null
                if (rawHref.startsWith("..") || rawHref.contains("delete") || rawHref.contains("edit")) return@mapNotNull null
                
                // Portal uses javascript to strip /thumbnails, so we do it here
                rawHref = rawHref.replace("/thumbnails", "")
                
                val url = normalizeUrl(rawHref, baseUrl, "images") ?: return@mapNotNull null
                
                val lowerUrl = url.lowercase()
                if (lowerUrl.contains("page=list_images") || lowerUrl.endsWith("/") || 
                    ((lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".png")) && 
                     !lowerUrl.contains("keogram") && !lowerUrl.contains("startrail") && !lowerUrl.contains("image.jpg") && !lowerUrl.contains("logo"))) {
                    
                    AllskyMedia(
                        date = extractDate(rawHref, element),
                        url = url
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.date }.distinctBy { it.url }.take(40)
    }

    private fun parseTimelapses(doc: org.jsoup.nodes.Document, baseUrl: String): List<AllskyMedia> {
        val elements = doc.select("a[href], source[src]")
        return elements.mapNotNull { element ->
            try {
                val rawHref = element.attr("href").ifEmpty { element.attr("src") }
                if (rawHref.contains("?") && !rawHref.contains("page=list_")) return@mapNotNull null
                if (rawHref.startsWith("..") || rawHref.contains("delete") || rawHref.contains("edit")) return@mapNotNull null
                
                val url = normalizeUrl(rawHref, baseUrl, "videos") ?: return@mapNotNull null
                val lowerUrl = url.lowercase()
                
                if ((lowerUrl.contains(".mp4") || lowerUrl.contains(".webm") || lowerUrl.contains(".mkv") || lowerUrl.contains(".mov")) &&
                    !lowerUrl.contains("allsky-logo")) {
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
        val elements = doc.select("a[href], img[src]")
        return elements.mapNotNull { element ->
            try {
                val rawHref = element.attr("href").ifEmpty { element.attr("src") }
                if (rawHref.contains("?") && !rawHref.contains("page=list_")) return@mapNotNull null
                if (rawHref.startsWith("..") || rawHref.contains("delete") || rawHref.contains("edit")) return@mapNotNull null
                
                val url = normalizeUrl(rawHref, baseUrl, "keograms") ?: return@mapNotNull null
                val lowerUrl = url.lowercase()
                
                if (lowerUrl.contains("keogram") && (lowerUrl.contains(".jpg") || lowerUrl.contains(".png")) &&
                    !lowerUrl.contains("allsky-logo") && !lowerUrl.contains("image.jpg") && !lowerUrl.contains("image-resize")) {
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
        val elements = doc.select("a[href], img[src]")
        return elements.mapNotNull { element ->
            try {
                val rawHref = element.attr("href").ifEmpty { element.attr("src") }
                if (rawHref.contains("?") && !rawHref.contains("page=list_")) return@mapNotNull null
                if (rawHref.startsWith("..") || rawHref.contains("delete") || rawHref.contains("edit")) return@mapNotNull null
                
                val url = normalizeUrl(rawHref, baseUrl, "startrails") ?: return@mapNotNull null
                val lowerUrl = url.lowercase()
                
                if (lowerUrl.contains("startrail") && (lowerUrl.contains(".jpg") || lowerUrl.contains(".png"))) {
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
        val elements = doc.select("a[href], img[src], source[src]")
        return elements.mapNotNull { element ->
            try {
                val rawHref = element.attr("href").ifEmpty { element.attr("src") }
                if (rawHref.contains("?") && !rawHref.contains("page=list_")) return@mapNotNull null
                if (rawHref.startsWith("..") || rawHref.contains("delete") || rawHref.contains("edit")) return@mapNotNull null
                
                val url = normalizeUrl(rawHref, baseUrl, "meteors") ?: return@mapNotNull null
                val lowerUrl = url.lowercase()
                
                if (lowerUrl.contains(".jpg") || lowerUrl.contains(".png") || lowerUrl.contains(".mp4")) {
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