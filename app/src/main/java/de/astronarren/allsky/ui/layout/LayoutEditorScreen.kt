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
    "SYSTEM",
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
    "SYSTEM" -> "System Monitoring"
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
    var layout by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        layout = userPreferences.getMainLayout()
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
                // Ensure all modules exist in our list so we can toggle them
                val displayList = layout.toMutableList()
                ALL_MODULES.forEach { if (!displayList.contains(it)) displayList.add(it) }

                itemsIndexed(displayList) { index, module ->
                    val isVisible = layout.contains(module)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isVisible,
                            onCheckedChange = { checked ->
                                val newLayout = layout.toMutableList()
                                if (checked) {
                                    if (!newLayout.contains(module)) newLayout.add(module)
                                } else {
                                    newLayout.remove(module)
                                }
                                layout = newLayout
                            }
                        )
                        Text(text = getModuleLabel(module), modifier = Modifier.weight(1f))
                        
                        if (isVisible) {
                            val activeIndex = layout.indexOf(module)
                            IconButton(
                                onClick = {
                                    if (activeIndex > 0) {
                                        val newLayout = layout.toMutableList()
                                        val temp = newLayout[activeIndex - 1]
                                        newLayout[activeIndex - 1] = newLayout[activeIndex]
                                        newLayout[activeIndex] = temp
                                        layout = newLayout
                                    }
                                },
                                enabled = activeIndex > 0
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Up")
                            }
                            IconButton(
                                onClick = {
                                    if (activeIndex < layout.size - 1) {
                                        val newLayout = layout.toMutableList()
                                        val temp = newLayout[activeIndex + 1]
                                        newLayout[activeIndex + 1] = newLayout[activeIndex]
                                        newLayout[activeIndex] = temp
                                        layout = newLayout
                                    }
                                },
                                enabled = activeIndex < layout.size - 1 && activeIndex != -1
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
                    layout = ALL_MODULES
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
                        userPreferences.saveMainLayout(layout)
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
