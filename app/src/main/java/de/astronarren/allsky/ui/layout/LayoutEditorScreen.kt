package de.astronarren.allsky.ui.layout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.astronarren.allsky.data.UserPreferences
import kotlinx.coroutines.launch

val ALL_MODULES = listOf(
    "LIVE_VIEW",
    "BEST_VIEWING",
    "WEATHER",
    "MOON",
    "TIMELAPSES",
    "METEORS",
    "IMAGES",
    "KEOGRAMS",
    "STARTRAILS"
)

private fun getModuleLabel(key: String): String = when (key) {
    "LIVE_VIEW" -> "Live View"
    "BEST_VIEWING" -> "Best Viewing Night"
    "WEATHER" -> "Weather Forecast"
    "MOON" -> "Moon Phase"
    "TIMELAPSES" -> "Timelapses"
    "METEORS" -> "Meteor Recordings"
    "IMAGES" -> "Raw Images"
    "KEOGRAMS" -> "Keograms"
    "STARTRAILS" -> "Startrails"
    else -> key
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutEditorScreen(
    userPreferences: UserPreferences,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentLayout by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Maintain a list of all possible modules, preserving current order where possible
    val fullList = remember(currentLayout) {
        val list = currentLayout.toMutableList()
        ALL_MODULES.forEach { if (!list.contains(it)) list.add(it) }
        list
    }

    LaunchedEffect(Unit) {
        currentLayout = userPreferences.getMainLayout()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Layout Editor") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(fullList) { index, module ->
                    val isVisible = currentLayout.contains(module)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isVisible,
                            onCheckedChange = { checked ->
                                val newLayout = currentLayout.toMutableList()
                                if (checked) {
                                    if (!newLayout.contains(module)) {
                                        // Insert it at its position in fullList among other visible items
                                        newLayout.add(module) 
                                    }
                                } else {
                                    newLayout.remove(module)
                                }
                                currentLayout = newLayout
                            }
                        )
                        Text(text = getModuleLabel(module), modifier = Modifier.weight(1f))
                        
                        if (isVisible) {
                            val activeIndex = currentLayout.indexOf(module)
                            IconButton(
                                onClick = {
                                    if (activeIndex > 0) {
                                        val newLayout = currentLayout.toMutableList()
                                        val temp = newLayout[activeIndex - 1]
                                        newLayout[activeIndex - 1] = newLayout[activeIndex]
                                        newLayout[activeIndex] = temp
                                        currentLayout = newLayout
                                    }
                                },
                                enabled = activeIndex > 0
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Up")
                            }
                            IconButton(
                                onClick = {
                                    if (activeIndex < currentLayout.size - 1) {
                                        val newLayout = currentLayout.toMutableList()
                                        val temp = newLayout[activeIndex + 1]
                                        newLayout[activeIndex + 1] = newLayout[activeIndex]
                                        newLayout[activeIndex] = temp
                                        currentLayout = newLayout
                                    }
                                },
                                enabled = activeIndex < currentLayout.size - 1 && activeIndex != -1
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Down")
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    currentLayout = ALL_MODULES
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Restore Defaults")
            }

            Button(
                onClick = {
                    scope.launch {
                        userPreferences.saveMainLayout(currentLayout)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Save Layout")
            }
        }
    }
}
