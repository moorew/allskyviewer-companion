package de.astronarren.allsky.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.astronarren.allsky.data.AllskyRepository
import de.astronarren.allsky.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AllskyUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val timelapses: List<AllskyMediaUiState> = emptyList(),
    val keograms: List<AllskyMediaUiState> = emptyList(),
    val startrails: List<AllskyMediaUiState> = emptyList(),
    val images: List<AllskyMediaUiState> = emptyList(),
    val meteors: List<AllskyMediaUiState> = emptyList(),
    val systemInfo: Map<String, String> = emptyMap()
)

data class AllskyMediaUiState(
    val date: String,
    val url: String
)

class AllskyViewModel(
    private val allskyRepository: AllskyRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(AllskyUiState())
    val uiState: StateFlow<AllskyUiState> = _uiState.asStateFlow()

    init {
        loadContent()
    }

    fun fetchContentForDate(date: String? = null) {
        loadContent(date)
    }

    private fun loadContent(date: String? = null) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(isLoading = true, error = null)
            }
            try {
                allskyRepository.getAllContent(date).collect { content ->
                    val isCacheEmpty = content.timelapses.isEmpty() && content.keograms.isEmpty() && 
                                       content.startrails.isEmpty() && content.images.isEmpty() && 
                                       content.meteors.isEmpty()
                    
                    _uiState.value = AllskyUiState(
                        isLoading = if (content.isFromCache) isCacheEmpty else false,
                        timelapses = content.timelapses.map {
                            AllskyMediaUiState(it.date, it.url)
                        },
                        keograms = content.keograms.map {
                            AllskyMediaUiState(it.date, it.url)
                        },
                        startrails = content.startrails.map {
                            AllskyMediaUiState(it.date, it.url)
                        },
                        images = content.images.map {
                            AllskyMediaUiState(it.date, it.url)
                        },
                        meteors = content.meteors.map {
                            AllskyMediaUiState(it.date, it.url)
                        },
                        systemInfo = content.systemInfo
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AllskyUiState(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
