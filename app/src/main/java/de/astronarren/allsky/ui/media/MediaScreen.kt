package de.astronarren.allsky.ui.media

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import AsyncImage

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.astronarren.allsky.viewmodel.AllskyViewModel
import de.astronarren.allsky.viewmodel.AllskyMediaUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    title: String,
    mediaType: String,
    viewModel: AllskyViewModel,
    onMediaClick: (AllskyMediaUiState) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dateInput by remember { mutableStateOf("All") }

    val mediaItems = when (mediaType) {
        "timelapses" -> uiState.timelapses
        "keograms" -> uiState.keograms
        "startrails" -> uiState.startrails
        "meteors" -> uiState.meteors
        "images" -> uiState.images
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { Text("Date (e.g. 20260405 or All)") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        val query = if (dateInput.isBlank()) "All" else dateInput
                        viewModel.fetchContentForDate(query)
                    }
                ) {
                    Text("Load")
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
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(mediaItems) { item ->
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth(),
                            onClick = { onMediaClick(item) },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = item.url,
                                    contentDescription = item.date,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
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
    }
}
