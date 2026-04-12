package de.astronarren.allsky.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.astronarren.allsky.R
import de.astronarren.allsky.data.UserPreferences
import de.astronarren.allsky.utils.LanguageManager
import de.astronarren.allsky.viewmodel.UpdateUiState
import de.astronarren.allsky.viewmodel.UpdateViewModel
import de.astronarren.allsky.ui.components.LanguageSelector
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userPreferences: UserPreferences,
    languageManager: LanguageManager,
    updateViewModel: UpdateViewModel,
    onNavigateBack: () -> Unit
) {
    var apiKeyInput by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf("") }
    var latInput by remember { mutableStateOf("") }
    var lonInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf(de.astronarren.allsky.utils.AppLanguage.SYSTEM) }
    val updateState by updateViewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        currentLanguage = languageManager.getCurrentLanguage()
        apiKeyInput = userPreferences.getApiKey()
        urlInput = userPreferences.getAllskyUrl()
        latInput = userPreferences.getLatitude()
        lonInput = userPreferences.getLongitude()
        usernameInput = userPreferences.getUsername()
        passwordInput = userPreferences.getPassword()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Version info with update badge
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (updateState is UpdateUiState.UpdateAvailable) {
                                Modifier.clickable { updateViewModel.showUpdateDialog() }
                            } else {
                                Modifier
                            }
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.app_status),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = when (val state = updateState) {
                                is UpdateUiState.UpdateAvailable -> stringResource(
                                    R.string.update_available_status, 
                                    state.updateInfo.latestVersion
                                )
                                is UpdateUiState.Checking -> stringResource(R.string.checking_for_updates)
                                is UpdateUiState.Downloading -> stringResource(
                                    R.string.downloading_update
                                )
                                is UpdateUiState.NoUpdate -> stringResource(R.string.up_to_date)
                                else -> stringResource(R.string.up_to_date)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    val currentState = updateState
                    if (currentState is UpdateUiState.UpdateAvailable) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = stringResource(R.string.update_available_badge),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // URL Input
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text(stringResource(R.string.allsky_url_input)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Lat/Lon Inputs
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = latInput,
                    onValueChange = { latInput = it },
                    label = { Text("Station Latitude") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = lonInput,
                    onValueChange = { lonInput = it },
                    label = { Text("Station Longitude") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username Input
            OutlinedTextField(
                value = usernameInput,
                onValueChange = { usernameInput = it },
                label = { Text("Username (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text("Password (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // API Key Input
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text(stringResource(R.string.openweather_api_key)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Language Selection
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { showLanguageDialog = true },
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.language_settings),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(currentLanguage.nameResId),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open settings item",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Save Button
            Button(
                onClick = {
                    scope.launch {
                        userPreferences.saveAllskyUrl(urlInput)
                        userPreferences.saveLatitude(latInput)
                        userPreferences.saveLongitude(lonInput)
                        userPreferences.saveUsername(usernameInput)
                        userPreferences.savePassword(passwordInput)
                        userPreferences.saveApiKey(apiKeyInput)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(R.string.save),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLanguageDialog) {
        LanguageSelector(
            currentLanguage = currentLanguage,
            onLanguageSelected = { language ->
                currentLanguage = language
                languageManager.setLanguage(language)
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}
