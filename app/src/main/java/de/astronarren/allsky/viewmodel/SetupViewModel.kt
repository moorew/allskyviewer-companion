package de.astronarren.allsky.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.astronarren.allsky.data.UserPreferences
import de.astronarren.allsky.ui.state.SetupUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SetupViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val isComplete = userPreferences.isSetupComplete()
            val url = userPreferences.getAllskyUrl()
            val apiKey = userPreferences.getApiKey()
            val username = userPreferences.getUsername()
            val password = userPreferences.getPassword()
            val latitude = userPreferences.getLatitude()
            val longitude = userPreferences.getLongitude()
            
            _uiState.update { state ->
                state.copy(
                    isComplete = isComplete,
                    allskyUrl = url,
                    apiKey = apiKey,
                    username = username,
                    password = password,
                    latitude = latitude,
                    longitude = longitude
                )
            }
        }
    }

    fun nextStep() {
        _uiState.update { state ->
            state.copy(currentStep = state.currentStep + 1)
        }
    }

    fun updateAllskyUrl(url: String) {
        viewModelScope.launch {
            userPreferences.saveAllskyUrl(url)
            _uiState.update { state ->
                state.copy(allskyUrl = url)
            }
        }
    }

    fun updateUsername(username: String) {
        viewModelScope.launch {
            userPreferences.saveUsername(username)
            _uiState.update { state ->
                state.copy(username = username)
            }
        }
    }

    fun updatePassword(password: String) {
        viewModelScope.launch {
            userPreferences.savePassword(password)
            _uiState.update { state ->
                state.copy(password = password)
            }
        }
    }

    fun updateLatitude(latitude: String) {
        viewModelScope.launch {
            userPreferences.saveLatitude(latitude)
            _uiState.update { state ->
                state.copy(latitude = latitude)
            }
        }
    }

    fun updateLongitude(longitude: String) {
        viewModelScope.launch {
            userPreferences.saveLongitude(longitude)
            _uiState.update { state ->
                state.copy(longitude = longitude)
            }
        }
    }

    fun updateApiKey(key: String) {
        viewModelScope.launch {
            userPreferences.saveApiKey(key)
            _uiState.update { state ->
                state.copy(apiKey = key)
            }
        }
    }

    fun completeSetup() {
        viewModelScope.launch {
            userPreferences.markSetupComplete()
            _uiState.update { state ->
                state.copy(isComplete = true)
            }
        }
    }
} 