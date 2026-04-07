package de.astronarren.allsky.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.astronarren.allsky.data.UserPreferences
import de.astronarren.allsky.ui.state.LiveImageUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class LiveImageViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(LiveImageUiState())
    val uiState: StateFlow<LiveImageUiState> = _uiState.asStateFlow()

    init {
        startImageRefresh()
        observeUrlChanges()
    }

    private fun startImageRefresh() {
        viewModelScope.launch {
            while (true) {
                try {
                    updateImage()
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message) }
                }
                delay(30_000) // 30 seconds
            }
        }
    }

    private fun observeUrlChanges() {
        viewModelScope.launch {
            userPreferences.getAllskyUrlFlow().collect { url ->
                updateImage(url)
            }
        }
    }

    private suspend fun updateImage(baseUrl: String? = null) {
        var url = baseUrl ?: userPreferences.getAllskyUrl()
        val username = userPreferences.getUsername()
        val password = userPreferences.getPassword()
        
        if (url.isNotEmpty()) {
            try {
                var cleanUrl = url.trimEnd('/')
                
                // Perform a quick check to see if /allsky is needed for the live image
                // or if we should use the direct /current/tmp/image.jpg path
                val liveImagePath = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        fun testPath(path: String): Int {
                            val testUrl = java.net.URL(path)
                            val conn = testUrl.openConnection() as java.net.HttpURLConnection
                            conn.requestMethod = "HEAD"
                            if (username.isNotEmpty() && password.isNotEmpty()) {
                                val basicAuth = "Basic " + android.util.Base64.encodeToString("$username:$password".toByteArray(), android.util.Base64.NO_WRAP)
                                conn.setRequestProperty("Authorization", basicAuth)
                            }
                            conn.connectTimeout = 3000
                            conn.readTimeout = 3000
                            return conn.responseCode
                        }

                        // Priority 1: Check if /current/tmp/image.jpg exists (often the actual live feed)
                        // If the base URL is already /allsky, we try to go up one level first
                        val rootUrl = if (cleanUrl.endsWith("/allsky")) cleanUrl.substring(0, cleanUrl.length - 7) else cleanUrl
                        if (testPath("$rootUrl/current/tmp/image.jpg") == 200) {
                            return@withContext "$rootUrl/current/tmp/image.jpg"
                        }

                        // Priority 2: Check current base URL /image.jpg
                        if (testPath("$cleanUrl/image.jpg") == 200) {
                            return@withContext "$cleanUrl/image.jpg"
                        }

                        // Priority 3: Check /allsky/image.jpg
                        if (!cleanUrl.endsWith("/allsky") && testPath("$cleanUrl/allsky/image.jpg") == 200) {
                            return@withContext "$cleanUrl/allsky/image.jpg"
                        }

                        null
                    } catch (e: Exception) {
                        null
                    }
                }

                val finalImageUrl = if (liveImagePath != null) {
                    val uri = android.net.Uri.parse(liveImagePath)
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        val authority = "${android.net.Uri.encode(username)}:${android.net.Uri.encode(password)}@${uri.authority}"
                        uri.buildUpon().encodedAuthority(authority).build().toString()
                    } else {
                        liveImagePath
                    }
                } else {
                    // Fallback to previous logic if discovery fails
                    val authUrl = if (username.isNotEmpty() && password.isNotEmpty()) {
                        val uri = android.net.Uri.parse(cleanUrl)
                        val authority = "${android.net.Uri.encode(username)}:${android.net.Uri.encode(password)}@${uri.authority}"
                        uri.buildUpon().encodedAuthority(authority).build().toString()
                    } else {
                        cleanUrl
                    }
                    "$authUrl/image.jpg"
                }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        imageUrl = "$finalImageUrl?t=${System.currentTimeMillis()}",
                        lastUpdate = System.currentTimeMillis(),
                        error = null // Clear any previous errors
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
} 