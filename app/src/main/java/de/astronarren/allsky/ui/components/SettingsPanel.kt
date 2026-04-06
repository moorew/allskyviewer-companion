package de.astronarren.allsky.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storm
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.astronarren.allsky.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanel(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    if (isOpen) {
        ModalDrawerSheet(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxHeight(),
            drawerContainerColor = MaterialTheme.colorScheme.surface,
            drawerContentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                DrawerItem("Home", Icons.Default.Home) { onNavigate("home") }
                DrawerItem("Layout Editor", Icons.Default.ViewModule) { onNavigate("layout_editor") }
                DrawerItem("Timelapses", Icons.Default.Videocam) { onNavigate("media/timelapses") }
                DrawerItem("Keograms", Icons.Default.FilterHdr) { onNavigate("media/keograms") }
                DrawerItem("Startrails", Icons.Default.Star) { onNavigate("media/startrails") }
                DrawerItem("Meteors", Icons.Default.Storm) { onNavigate("media/meteors") }
                DrawerItem("Images", Icons.Default.Image) { onNavigate("media/images") }
                DrawerItem("Settings", Icons.Default.Settings) { onNavigate("settings") }
                
                Spacer(modifier = Modifier.weight(1f))
                
                DrawerItem("About", Icons.Default.Info) { onNavigate("about") }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(text = title) },
        selected = false,
        onClick = onClick,
        icon = { Icon(imageVector = icon, contentDescription = title) },
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
