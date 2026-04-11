package de.astronarren.allsky.ui.setup

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import de.astronarren.allsky.R
import de.astronarren.allsky.ui.theme.DeepNavy
import de.astronarren.allsky.ui.theme.NightPurple
import de.astronarren.allsky.viewmodel.SetupViewModel

@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    onSetupComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isComplete) {
        LaunchedEffect(Unit) {
            onSetupComplete()
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepNavy, NightPurple)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "ALLSKY",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp,
                    color = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "COMPANION",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (uiState.currentStep) {
                        1 -> WelcomeStep(onNext = { viewModel.nextStep() })
                        2 -> UrlStep(
                            currentUrl = uiState.allskyUrl,
                            currentUsername = uiState.username,
                            currentPassword = uiState.password,
                            onUrlChange = { viewModel.updateAllskyUrl(it) },
                            onUsernameChange = { viewModel.updateUsername(it) },
                            onPasswordChange = { viewModel.updatePassword(it) },
                            onNext = { viewModel.nextStep() }
                        )
                        3 -> ApiKeyStep(
                            currentApiKey = uiState.apiKey,
                            currentLat = uiState.latitude,
                            currentLon = uiState.longitude,
                            onApiKeyChange = { viewModel.updateApiKey(it) },
                            onLatChange = { viewModel.updateLatitude(it) },
                            onLonChange = { viewModel.updateLongitude(it) },
                            onComplete = { viewModel.completeSetup() }
                        )
                    }
                }
            }
            
            // Step indicator
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (uiState.currentStep == index + 1) 24.dp else 12.dp, 6.dp)
                            .background(
                                if (uiState.currentStep == index + 1) Color.White else Color.White.copy(alpha = 0.3f),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Yellow
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) {
            Text(stringResource(R.string.start_setup).uppercase(), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.learn_more),
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = TextDecoration.Underline,
                color = Color.White.copy(alpha = 0.5f)
            ),
            modifier = Modifier.clickable {
                uriHandler.openUri("https://github.com/AllskyTeam/allsky")
            }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun UrlStep(
    currentUsername: String,
    currentPassword: String,
    currentUrl: String,
    onUrlChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNext: () -> Unit
) {
    var urlInput by remember { mutableStateOf(currentUrl) }
    var usernameInput by remember { mutableStateOf(currentUsername) }
    var passwordInput by remember { mutableStateOf(currentPassword) }

    val autofill = LocalAutofill.current
    val autofillTree = LocalAutofillTree.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "CONNECT TO SERVER",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = urlInput,
            onValueChange = { 
                urlInput = it
                onUrlChange(it)
            },
            label = { Text("Allsky URL") },
            placeholder = { Text("https://myallsky.local") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedBorderColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                focusedLabelColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val usernameAutofillNode = remember {
            AutofillNode(
                autofillTypes = listOf(AutofillType.Username),
                onFill = { usernameInput = it; onUsernameChange(it) }
            )
        }
        
        OutlinedTextField(
            value = usernameInput,
            onValueChange = { 
                usernameInput = it
                onUsernameChange(it)
            },
            label = { Text("Username (Optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { usernameAutofillNode.boundingBox = it.localBoundingBoxOf(it) }
                .onFocusChanged { focusState ->
                    autofill?.run {
                        if (focusState.isFocused) {
                            requestAutofillForNode(usernameAutofillNode)
                        } else {
                            cancelAutofillForNode(usernameAutofillNode)
                        }
                    }
                },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedBorderColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                focusedLabelColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        
        SideEffect {
            autofillTree += usernameAutofillNode
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val passwordAutofillNode = remember {
            AutofillNode(
                autofillTypes = listOf(AutofillType.Password),
                onFill = { passwordInput = it; onPasswordChange(it) }
            )
        }

        OutlinedTextField(
            value = passwordInput,
            onValueChange = { 
                passwordInput = it
                onPasswordChange(it)
            },
            label = { Text("Password (Optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { passwordAutofillNode.boundingBox = it.localBoundingBoxOf(it) }
                .onFocusChanged { focusState ->
                    autofill?.run {
                        if (focusState.isFocused) {
                            requestAutofillForNode(passwordAutofillNode)
                        } else {
                            cancelAutofillForNode(passwordAutofillNode)
                        }
                    }
                },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedBorderColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                focusedLabelColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        
        SideEffect {
            autofillTree += passwordAutofillNode
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNext,
            enabled = urlInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) {
            Text("CONTINUE", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun ApiKeyStep(
    currentApiKey: String,
    currentLat: String,
    currentLon: String,
    onApiKeyChange: (String) -> Unit,
    onLatChange: (String) -> Unit,
    onLonChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var apiKeyInput by remember { mutableStateOf(currentApiKey) }
    var latInput by remember { mutableStateOf(currentLat) }
    var lonInput by remember { mutableStateOf(currentLon) }
    
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        latInput = it.latitude.toString()
                        lonInput = it.longitude.toString()
                        onLatChange(latInput)
                        onLonChange(lonInput)
                    }
                }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "LOCATION & WEATHER",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = apiKeyInput,
            onValueChange = { 
                apiKeyInput = it
                onApiKeyChange(it)
            },
            label = { Text("OpenWeather API Key (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedBorderColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                focusedLabelColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = latInput,
                onValueChange = { 
                    latInput = it
                    onLatChange(it)
                },
                label = { Text("Latitude") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedBorderColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                    focusedLabelColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )
            OutlinedTextField(
                value = lonInput,
                onValueChange = { 
                    lonInput = it
                    onLonChange(it)
                },
                label = { Text("Longitude") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedBorderColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                    focusedLabelColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        TextButton(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            location?.let {
                                latInput = it.latitude.toString()
                                lonInput = it.longitude.toString()
                                onLatChange(latInput)
                                onLonChange(lonInput)
                            }
                        }
                } else {
                    permissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                }
            },
            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("GET CURRENT LOCATION")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) {
            Text("FINISH", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
        }
    }
}