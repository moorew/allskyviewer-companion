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
        val url = baseUrl ?: userPreferences.getAllskyUrl()
        val username = userPreferences.getUsername()
        val password = userPreferences.getPassword()
        
        if (url.isNotEmpty()) {
            try {
                val cleanUrl = url.trimEnd('/')
                val authUrl = if (username.isNotEmpty() && password.isNotEmpty()) {
                    val uri = android.net.Uri.parse(cleanUrl)
                    val authority = "${android.net.Uri.encode(username)}:${android.net.Uri.encode(password)}@${uri.authority}"
                    uri.buildUpon().encodedAuthority(authority).build().toString()
                } else {
                    cleanUrl
                }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        imageUrl = "$authUrl/image.jpg?t=${System.currentTimeMillis()}",
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