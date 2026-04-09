package de.astronarren.allsky.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import de.astronarren.allsky.data.WeatherData
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
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    userPreferences: UserPreferences,
    weatherViewModel: WeatherViewModel,
    allskyViewModel: AllskyViewModel,
    imageViewerViewModel: ImageViewerViewModel,
    liveImageViewModel: LiveImageViewModel,
    languageManager: LanguageManager,
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

    val mainLayout by userPreferences.getMainLayoutFlow().collectAsStateWithLifecycle(
        initialValue = listOf("LIVE_VIEW", "BEST_VIEWING", "WEATHER", "MOON", "TIMELAPSES", "METEORS", "IMAGES", "KEOGRAMS", "STARTRAILS")
    )
    
    var allskyUrl by remember { mutableStateOf("") }
    
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
                onNavigate = { route ->
                    scope.launch {
                        drawerState.close()
                        isSettingsOpen = false
                    }
                    if (route == "home") {
                        navController.navigate(route) {
                            popUpTo("home") { inclusive = true }
                        }
                    } else {
                        navController.navigate(route)
                    }
                }
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
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isSettingsOpen = true
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            
            // Dynamic Background based on Weather
            val weatherCondition = weatherUiState.weatherData?.second?.firstOrNull()?.weather?.firstOrNull()?.main ?: "Clear"
            val backgroundColors = remember(weatherCondition) {
                when (weatherCondition) {
                    "Clear" -> listOf(DeepNavy, NightPurple, ClearNight)
                    "Clouds" -> listOf(Color(0xFF37474F), Color(0xFF455A64), Color(0xFF607D8B)) // Grey/Blue-Grey
                    "Rain", "Drizzle", "Thunderstorm" -> listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3949AB)) // Dark Rain Blue
                    "Snow" -> listOf(Color(0xFF78909C), Color(0xFF90A4AE), Color(0xFFB0BEC5)) // Cool bright
                    else -> listOf(DeepNavy, NightPurple, ClearNight)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = Brush.verticalGradient(colors = backgroundColors))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = padding.calculateTopPadding())
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    mainLayout.forEach { moduleName ->
                        when (moduleName) {
                            "LIVE_VIEW" -> {
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
                            }
                            "BEST_VIEWING" -> {
                                val bestNight = weatherViewModel.getBestViewingNight()
                                if (bestNight != null) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        shape = RoundedCornerShape(32.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White.copy(alpha = 0.1f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "BEST VIEWING NIGHT",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 2.sp
                                                ),
                                                color = Color.Yellow
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date(bestNight.dt * 1000L)).uppercase(),
                                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                                            )
                                            Text(
                                                text = "${bestNight.weather.firstOrNull()?.description?.uppercase() ?: ""} • ${bestNight.clouds.all}% CLOUDS",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                            "WEATHER" -> {
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
                                        )
                                }
                            }
                            "MOON" -> {
                                Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                                    MoonPhaseDisplay()
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            "TIMELAPSES" -> {
                                AllskyMediaSection(
                                    title = "Recent Timelapses",
                                    media = allskyUiState.timelapses,
                                    onMediaClick = { media -> currentVideo = media.url }
                                )
                            }
                            "METEORS" -> {
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
                            }
                            "IMAGES" -> {
                                AllskyMediaSection(
                                    title = "Daily Raw Images",
                                    media = allskyUiState.images,
                                    onMediaClick = { media -> imageViewerViewModel.showImage(media.url) }
                                )
                            }
                            "KEOGRAMS" -> {
                                AllskyMediaSection(
                                    title = "Keograms",
                                    media = allskyUiState.keograms,
                                    onMediaClick = { media -> imageViewerViewModel.showImage(media.url) }
                                )
                            }
                            "STARTRAILS" -> {
                                AllskyMediaSection(
                                    title = "Startrails",
                                    media = allskyUiState.startrails,
                                    onMediaClick = { media -> imageViewerViewModel.showImage(media.url) }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }

                // Overlay components
                if (imageViewerState.isFullScreen && imageViewerState.currentImageUrl != null) {
                    FullScreenImageViewer(
                        imageUrl = imageViewerState.currentImageUrl!!,
                        userPreferences = userPreferences,
                        onDismiss = { imageViewerViewModel.dismissImage() }
                    )
                }

                if (currentVideo != null) {
                    VideoPlayer(
                        videoUrl = currentVideo!!,
                        userPreferences = userPreferences,
                        onDismiss = { currentVideo = null }
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    return if (timestamp == 0L) {
        ""
    } else {
        // Assume timestamp is in millis if it's large, otherwise seconds
        val millis = if (timestamp < 1000000000000L) timestamp * 1000L else timestamp
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(millis))
    }
}
