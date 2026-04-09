package de.astronarren.allsky.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import de.astronarren.allsky.viewmodel.AllskyMediaUiState
import de.astronarren.allsky.R

import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import de.astronarren.allsky.ui.theme.*

@Composable
fun AllskyMediaSection(
    title: String,
    media: List<AllskyMediaUiState>,
    onMediaClick: (AllskyMediaUiState) -> Unit,
    isVideo: Boolean? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
            )
            
            if (media.isNotEmpty()) {
                Text(
                    text = "${media.size} ITEMS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }

        if (media.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_content_available),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                items(media) { item ->
                    val isItemVideo = isVideo ?: (item.url.lowercase().contains(".mp4") || 
                                              item.url.lowercase().contains(".webm") || 
                                              item.url.lowercase().contains(".mov") || 
                                              item.url.lowercase().contains(".mkv"))
                    MediaCard(
                        media = item,
                        onClick = { onMediaClick(item) },
                        isVideo = isItemVideo,
                        isMeteor = title.contains("Meteor", ignoreCase = true)
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaCard(
    media: AllskyMediaUiState,
    onClick: () -> Unit,
    isVideo: Boolean = false,
    isMeteor: Boolean = false
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(200.dp),
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        )
    ) {
        val placeholderGradient = Brush.verticalGradient(
            colors = listOf(DeepNavy, NightPurple)
        )
        
        Box(modifier = Modifier.fillMaxSize().background(placeholderGradient)) {
            AsyncImage(
                model = media.url,
                contentDescription = stringResource(R.string.media_from_date, media.date),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = rememberVectorPainter(if (isVideo || isMeteor) Icons.Default.PlayCircle else Icons.Default.Image),
                error = rememberVectorPainter(if (isVideo || isMeteor) Icons.Default.PlayCircle else Icons.Default.Image)
            )
            
            // Gradient Overlay for readability
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        ),
                        startY = size.height * 0.4f
                    )
                )
            }

            if (isVideo) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = media.date,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = when {
                        isMeteor -> "METEOR"
                        isVideo -> "TIMELAPSE"
                        else -> "ARCHIVE"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
