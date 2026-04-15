package de.astronarren.allsky.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.astronarren.allsky.data.UserPreferences
import de.astronarren.allsky.ui.state.LiveImageUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
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
                    _uiState.update { it.copy(error = "Network drop: ${e.message}") }
                }
                // Adaptive delay: wait longer if there's an error
                val currentError = _uiState.value.error
                if (currentError != null) {
                    delay(60_000) // 1 minute retry if error
                } else {
                    delay(30_000) // 30 seconds normal refresh
                }
            }
        }
    }

    private fun observeUrlChanges() {
        viewModelScope.launch {
            userPreferences.getAllskyUrlFlow()
                .distinctUntilChanged()
                .collect { url ->
                    if (url.isNotEmpty()) {
                        updateImage(url)
                    }
                }
        }
    }

    private suspend fun updateImage(baseUrl: String? = null) {
        val url = baseUrl ?: userPreferences.getAllskyUrl()
        if (url.isEmpty()) {
            _uiState.update { it.copy(error = "Allsky URL not configured") }
            return
        }

        val username = userPreferences.getUsername()
        val password = userPreferences.getPassword()
        
        try {
            val cleanUrl = url.trimEnd('/')
            
            val liveImagePath = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    suspend fun testPath(path: String): Int = withContext(Dispatchers.IO) {
                        var conn: java.net.HttpURLConnection? = null
                        try {
                            val testUrl = java.net.URL(path)
                            conn = testUrl.openConnection() as java.net.HttpURLConnection
                            conn!!.requestMethod = "HEAD"
                            if (username.isNotEmpty() && password.isNotEmpty()) {
                                val basicAuth = "Basic " + android.util.Base64.encodeToString("$username:$password".toByteArray(), android.util.Base64.NO_WRAP)
                                conn!!.setRequestProperty("Authorization", basicAuth)
                            }
                            conn!!.connectTimeout = 5000
                            conn!!.readTimeout = 5000
                            conn!!.responseCode
                        } catch (e: Exception) {
                            -1
                        } finally {
                            conn?.disconnect()
                        }
                    }

                    // Priority 1: Check if /current/tmp/image.jpg exists (often the actual live feed)
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
                liveImagePath
            } else {
                "$cleanUrl/image.jpg"
            }
            
            _uiState.update { currentState ->
                currentState.copy(
                    imageUrl = "$finalImageUrl?t=${System.currentTimeMillis()}",
                    lastUpdate = System.currentTimeMillis(),
                    error = null
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Stream error: ${e.message}") }
        }
    }
} 