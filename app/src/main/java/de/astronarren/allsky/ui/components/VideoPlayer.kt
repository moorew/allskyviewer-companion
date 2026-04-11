package de.astronarren.allsky.ui.components

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import de.astronarren.allsky.utils.DownloadHelper
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun VideoPlayer(
    videoUrl: String,
    userPreferences: de.astronarren.allsky.data.UserPreferences,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val downloadHelper = remember { DownloadHelper(context, userPreferences) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }

    val fileName = remember(videoUrl) {
        val lastPathSegment = videoUrl.substringAfterLast("/").substringBefore("?")
        if (lastPathSegment.endsWith(".mp4") || lastPathSegment.endsWith(".webm") || 
            lastPathSegment.endsWith(".mov") || lastPathSegment.endsWith(".mkv")) {
            lastPathSegment
        } else {
            "allsky_video_${System.currentTimeMillis()}.mp4"
        }
    }

    BackHandler(enabled = true) {
        onDismiss()
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Consume clicks to prevent background interaction */ }
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Control Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(24.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer { shadowElevation = 100f }, // Ensure it's on top
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = {
                    downloadHelper.downloadMedia(videoUrl, fileName, isVideo = true)
                },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Surface(
                onClick = { onDismiss() },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
