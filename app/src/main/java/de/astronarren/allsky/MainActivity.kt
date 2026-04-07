package de.astronarren.allsky

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.astronarren.allsky.data.UserPreferences
import de.astronarren.allsky.data.network.WeatherApiProvider
import de.astronarren.allsky.ui.MainScreen
import de.astronarren.allsky.ui.about.AboutScreen
import de.astronarren.allsky.ui.setup.SetupScreen
import de.astronarren.allsky.ui.theme.AllskyTheme
import de.astronarren.allsky.utils.LocationManager
import de.astronarren.allsky.viewmodel.*
import de.astronarren.allsky.data.WeatherRepository
import de.astronarren.allsky.data.AllskyRepository
import de.astronarren.allsky.utils.LanguageManager
import de.astronarren.allsky.workers.WeatherWorker
import java.util.concurrent.TimeUnit

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.astronarren.allsky.ui.layout.LayoutEditorScreen
import de.astronarren.allsky.ui.media.MediaScreen
import de.astronarren.allsky.ui.settings.SettingsScreen

class MainActivity : ComponentActivity() {
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                weatherViewModel.updateWeather()
            }
        }
    }

    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var allskyViewModel: AllskyViewModel
    private lateinit var imageViewerViewModel: ImageViewerViewModel
    private lateinit var setupViewModel: SetupViewModel
    private lateinit var liveImageViewModel: LiveImageViewModel
    private lateinit var languageViewModel: LanguageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize LanguageManager and ViewModel
        val languageManager = LanguageManager(this) {
            // Recreate activity when language changes
            recreate()
        }
        languageViewModel = LanguageViewModel(languageManager)
        
        val userPreferences = UserPreferences(applicationContext)
        val locationManager = LocationManager(applicationContext)
        
        // Get WeatherService from provider
        val weatherService = WeatherApiProvider.provideWeatherService()
        
        val weatherRepository = WeatherRepository(
            locationManager = locationManager,
            weatherService = weatherService,
            userPreferences = userPreferences
        )
        
        val allskyRepository = AllskyRepository(userPreferences)
        
        weatherViewModel = WeatherViewModel(
            weatherRepository = weatherRepository,
            userPreferences = userPreferences
        )
        allskyViewModel = AllskyViewModel(allskyRepository, userPreferences)
        imageViewerViewModel = ImageViewerViewModel()
        setupViewModel = SetupViewModel(userPreferences)
        liveImageViewModel = LiveImageViewModel(userPreferences)
        
        checkAndRequestLocationPermissions()
        scheduleWeatherWorker()
        
        enableEdgeToEdge()
        setContent {
            AllskyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var showSetup by remember { mutableStateOf(true) }
                    
                    LaunchedEffect(Unit) {
                        showSetup = !userPreferences.isSetupComplete()
                        if (!showSetup) {
                            navController.navigate("home") {
                                popUpTo("setup") { inclusive = true }
                            }
                        }
                    }
                    
                    NavHost(
                        navController = navController,
                        startDestination = if (showSetup) "setup" else "home"
                    ) {
                        composable("setup") {
                            SetupScreen(
                                viewModel = setupViewModel,
                                onSetupComplete = { 
                                    showSetup = false
                                    navController.navigate("home") {
                                        popUpTo("setup") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home") {
                            MainScreen(
                                navController = navController,
                                userPreferences = userPreferences,
                                weatherViewModel = weatherViewModel,
                                allskyViewModel = allskyViewModel,
                                imageViewerViewModel = imageViewerViewModel,
                                liveImageViewModel = liveImageViewModel,
                                languageManager = languageManager,
                                onRequestLocationPermission = {
                                    locationPermissionRequest.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            )
                        }
                        composable("layout_editor") {
                            LayoutEditorScreen(
                                userPreferences = userPreferences,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("media/{type}") { backStackEntry ->
                            val type = backStackEntry.arguments?.getString("type") ?: "timelapses"
                            val title = type.replaceFirstChar { it.uppercase() }
                            MediaScreen(
                                title = title,
                                mediaType = type,
                                viewModel = allskyViewModel,
                                userPreferences = userPreferences,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            val updateViewModel: de.astronarren.allsky.viewmodel.UpdateViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                                factory = de.astronarren.allsky.viewmodel.UpdateViewModelFactory(applicationContext as android.app.Application)
                            )
                            SettingsScreen(
                                userPreferences = userPreferences,
                                languageManager = languageManager,
                                updateViewModel = updateViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("about") {
                            AboutScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                weatherViewModel.updateWeather()
            }
            else -> {
                // Request permissions
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun scheduleWeatherWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val weatherWorkRequest = PeriodicWorkRequestBuilder<WeatherWorker>(3, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "WeatherAlerts",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            weatherWorkRequest
        )
    }
}
