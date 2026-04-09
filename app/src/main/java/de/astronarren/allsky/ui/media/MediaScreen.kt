package de.astronarren.allsky.ui.media

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.astronarren.allsky.viewmodel.AllskyViewModel
import de.astronarren.allsky.viewmodel.AllskyMediaUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History

import de.astronarren.allsky.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    title: String,
    mediaType: String,
    viewModel: AllskyViewModel,
    userPreferences: de.astronarren.allsky.data.UserPreferences,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dateInput by remember { mutableStateOf("All") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    var currentVideo by remember { mutableStateOf<String?>(null) }
    var currentImage by remember { mutableStateOf<String?>(null) }

    val mediaItems = when (mediaType) {
        "timelapses" -> uiState.timelapses
        "keograms" -> uiState.keograms
        "startrails" -> uiState.startrails
        "meteors" -> uiState.meteors
        "images" -> uiState.images
        else -> emptyList()
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                        dateInput = sdf.format(Date(millis))
                        viewModel.fetchContentForDate(dateInput)
                    }
                }) { Text("Select") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Modern Date Selection Trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = dateInput != "All",
                        onClick = { showDatePicker = true },
                        label = { 
                            Text(
                                if (dateInput == "All") "Select Date" 
                                else {
                                    // Format for display: 20240101 -> Jan 01, 2024
                                    try {
                                        val inputSdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                                        val displaySdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                        displaySdf.format(inputSdf.parse(dateInput)!!)
                                    } catch (e: Exception) {
                                        dateInput
                                    }
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (dateInput != "All") {
                        FilterChip(
                            selected = false,
                            onClick = {
                                dateInput = "All"
                                viewModel.fetchContentForDate("All")
                            },
                            label = { Text("Show All") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                    }
                } else if (mediaItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No content available for this date.")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(mediaItems) { item ->
                            val isVideo = item.url.lowercase().run { contains(".mp4") || contains(".webm") || contains(".mov") || contains(".mkv") }
                            Card(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .fillMaxWidth(),
                                onClick = { 
                                    if (isVideo) {
                                        currentVideo = item.url
                                    } else {
                                        currentImage = item.url
                                    }
                                },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                val placeholderGradient = Brush.verticalGradient(
                                    colors = listOf(DeepNavy, NightPurple)
                                )
                                Box(modifier = Modifier.fillMaxSize().background(placeholderGradient)) {
                                    AsyncImage(
                                        model = item.url,
                                        contentDescription = item.date,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(if (isVideo) Icons.Default.PlayCircle else Icons.Default.Image),
                                        error = androidx.compose.ui.graphics.vector.rememberVectorPainter(if (isVideo) Icons.Default.PlayCircle else Icons.Default.Image)
                                    )
                                    if (isVideo) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(48.dp)
                                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(32.dp),
                                                tint = Color.White
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .fillMaxWidth()
                                            .background(
                                                Color.Black.copy(alpha = 0.5f)
                                            )
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = item.date,
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Overlays
            if (currentImage != null) {
                de.astronarren.allsky.ui.components.FullScreenImageViewer(
                    imageUrl = currentImage!!,
                    userPreferences = userPreferences,
                    onDismiss = { currentImage = null }
                )
            }

            if (currentVideo != null) {
                de.astronarren.allsky.ui.components.VideoPlayer(
                    videoUrl = currentVideo!!,
                    userPreferences = userPreferences,
                    onDismiss = { currentVideo = null }
                )
            }
        }
    }
}
