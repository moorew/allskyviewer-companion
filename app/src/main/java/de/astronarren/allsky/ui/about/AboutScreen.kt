package de.astronarren.allsky.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Brightness3
import androidx.compose.material.icons.outlined.ViewQuilt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.astronarren.allsky.R
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.clickable
import de.astronarren.allsky.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Title and Version
            Text(
                text = "ALLSKY",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Author Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Developed by Will Moore",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.clickable { uriHandler.openUri("https://github.com/moorew") },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GitHub: moorew",
                            style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.Underline)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.clickable { uriHandler.openUri("https://bsky.app/profile/clevercode.ca") },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bluesky: @clevercode",
                            style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.Underline)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Description
            Text(
                text = "Experience your Allsky camera station like never before. This companion app provides a modern, high-performance interface for monitoring your station in real-time, analyzing historical captures, and staying ahead of the weather with precision station-centric data.",
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Features
            Text(
                text = "ADVANCED CAPABILITIES",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            AboutFeatureItem(Icons.Outlined.Explore, "Intelligent Scraping", "Robust parsing of Allsky Portal galleries for seamless media access.")
            AboutFeatureItem(Icons.Outlined.Brightness3, "Observation Planning", "Automated 'Best Viewing' night detection based on station coordinates.")
            AboutFeatureItem(Icons.Outlined.ViewQuilt, "Modular Design", "Fully customizable dashboard layout tailored to your workflow.")
            AboutFeatureItem(Icons.Outlined.History, "Media Explorer", "Powerful calendar-based discovery for your entire capture history.")
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Footer Credit
            Text(
                text = "ACKNOWLEDGEMENTS",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "This project honors the original concept and logic established by Thomas Jacquin and the AllskyTeam community.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AboutFeatureItem(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
