package de.astronarren.allsky.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import coil.compose.AsyncImage
import de.astronarren.allsky.ui.components.*
import de.astronarren.allsky.data.UserPreferences
import de.astronarren.allsky.viewmodel.*
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import de.astronarren.allsky.R
import de.astronarren.allsky.utils.LanguageManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import de.astronarren.allsky.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userPreferences: UserPreferences,
    weatherViewModel: WeatherViewModel,
    allskyViewModel: AllskyViewModel,
    imageViewerViewModel: ImageViewerViewModel,
    liveImageViewModel: LiveImageViewModel,
    languageManager: LanguageManager,
    onNavigateToAbout: () -> Unit,
    onRequestLocationPermission: () -> Unit
) {
    var isSettingsOpen by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var apiKey by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    
    val weatherUiState by weatherViewModel.uiState.collectAsStateWithLifecycle()
    val allskyUiState by allskyViewModel.uiState.collectAsStateWithLifecycle()
    val imageViewerState by imageViewerViewModel.uiState.collectAsStateWithLifecycle()
    val liveImageState by liveImageViewModel.uiState.collectAsStateWithLifecycle()
    
    var allskyUrl by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val updateViewModel: UpdateViewModel = viewModel(
        factory = UpdateViewModelFactory(context.applicationContext as android.app.Application)
    )
    val updateState by updateViewModel.uiState.collectAsState()
    val showUpdateDialog by updateViewModel.showDialog.collectAsState()
    
    var currentVideo by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        apiKey = userPreferences.getApiKey()
        allskyUrl = userPreferences.getAllskyUrl()
    }

    LaunchedEffect(apiKey) {
        if (apiKey.isNotEmpty()) {
            weatherViewModel.updateWeather()
        }
    }

    LaunchedEffect(Unit) {
        userPreferences.getAllskyUrlFlow()
            .collect { url ->
                allskyUrl = url
            }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SettingsPanel(
                isOpen = isSettingsOpen,
                onDismiss = {
                    scope.launch {
                        drawerState.close()
                        isSettingsOpen = false
                    }
                },
                onAboutClick = onNavigateToAbout,
                userPreferences = userPreferences,
                languageManager = languageManager,
                updateViewModel = updateViewModel
            )
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "ALLSKY", 
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp
                            )
                        ) 
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    actions = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isSettingsOpen = true
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                DeepNavy,
                                NightPurple,
                                ClearNight
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = padding.calculateTopPadding())
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    // Live Image Section - Full Width & Heroic
                    if (allskyUrl.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .padding(20.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { 
                                        imageViewerViewModel.showImage(liveImageState.imageUrl)
                                    },
                                shape = RoundedCornerShape(40.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = liveImageState.imageUrl,
                                        contentDescription = stringResource(R.string.live_allsky_image),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    // Status Badge
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(20.dp),
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(Color.Green, RoundedCornerShape(4.dp))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "LIVE",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(20.dp),
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = formatTime(liveImageState.lastUpdate),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Weather Section
                    if (apiKey.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Weather Forecast",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                val uriHandler = LocalUriHandler.current
                                Button(
                                    onClick = { uriHandler.openUri("https://home.openweathermap.org/api_keys") },
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Get API Key")
                                }
                            }
                        }
                    } else {
                        WeatherDisplay(
                            uiState = weatherUiState,
                            onRequestPermission = onRequestLocationPermission
                        )
                    }
                    
                    // Moon Phase Section (Integrated into Bento layout)
                    Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                        MoonPhaseDisplay()
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Media Sections
                    if (allskyUiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                    } else if (allskyUiState.error != null) {
                        Text(
                            text = allskyUiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(20.dp)
                        )
                    } else {
                        AllskyMediaSection(
                            title = "Recent Timelapses",
                            media = allskyUiState.timelapses,
                            onMediaClick = { media -> currentVideo = media.url }
                        )

                        AllskyMediaSection(
                            title = "Meteor Recordings",
                            media = allskyUiState.meteors,
                            onMediaClick = { media -> 
                                if (media.url.lowercase().contains(".mp4") || 
                                    media.url.lowercase().contains(".webm")) {
                                    currentVideo = media.url
                                } else {
                                    imageViewerViewModel.showImage(media.url)
                                }
                            }
                        )

                        AllskyMediaSection(
                            title = "Daily Raw Images",
                            media = allskyUiState.images,
                            onMediaClick = { media -> imageViewerViewModel.showImage(media.url) }
                        )

                        AllskyMediaSection(
                            title = "Keograms",
                            media = allskyUiState.keograms,
                            onMediaClick = { media -> imageViewerViewModel.showImage(media.url) }
                        )

                        AllskyMediaSection(
                            title = "Startrails",
                            media = allskyUiState.startrails,
                            onMediaClick = { media -> imageViewerViewModel.showImage(media.url) }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }

                // Overlay components
                if (imageViewerState.isFullScreen && imageViewerState.currentImageUrl != null) {
                    FullScreenImageViewer(
                        imageUrl = imageViewerState.currentImageUrl!!,
                        onDismiss = { imageViewerViewModel.dismissImage() }
                    )
                }

                if (currentVideo != null) {
                    VideoPlayer(
                        videoUrl = currentVideo!!,
                        onDismiss = { currentVideo = null }
                    )
                }
            }
        }
    }

    if (updateState is UpdateUiState.UpdateAvailable && showUpdateDialog) {
        val state = updateState as UpdateUiState.UpdateAvailable
        UpdateDialog(
            showDialog = true,
            onDismiss = { updateViewModel.dismissUpdate() },
            onDownload = { updateViewModel.downloadUpdate() },
            version = state.updateInfo.latestVersion,
            changelog = state.updateInfo.releaseNotes
        )
    }
}

private fun formatTime(timestamp: Long): String {
    return if (timestamp == 0L) {
        ""
    } else {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    }
}
