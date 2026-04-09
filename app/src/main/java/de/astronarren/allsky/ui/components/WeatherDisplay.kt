package de.astronarren.allsky.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.astronarren.allsky.data.WeatherData
import de.astronarren.allsky.data.City
import java.text.SimpleDateFormat
import java.util.*
import de.astronarren.allsky.ui.state.WeatherUiState
import androidx.compose.ui.res.stringResource
import de.astronarren.allsky.R

@Composable
fun WeatherDisplay(
    modifier: Modifier = Modifier,
    uiState: WeatherUiState
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            
            uiState.weatherData != null -> {
                val (city, forecasts) = uiState.weatherData
                Column {
                    // Location and Current Temp
                    Text(
                        text = city.name.uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    forecasts.firstOrNull()?.let { current ->
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "${Math.round(current.main.temp)}°",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 84.sp,
                                    letterSpacing = (-4).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            Column(modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)) {
                                Text(
                                    text = current.weather.firstOrNull()?.description?.capitalize() ?: "",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Feels like ${Math.round(current.main.feels_like)}°",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Tonight's Viewing Conditions
                    NightConditionsDisplay(city = city, forecasts = forecasts)

                    // 5-Day Forecast Bento Box
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth().padding(24.dp)
                        ) {
                            items(forecasts.take(5)) { dayWeather ->
                                DayForecast(dayWeather)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayForecast(weather: WeatherData) {
    Column(
        modifier = Modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatDay(weather.dt).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${Math.round(weather.main.temp)}°",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NightConditionsDisplay(city: City, forecasts: List<WeatherData>) {
    val currentTime = System.currentTimeMillis()
    // sunset/sunrise are for the current day. If it's already past sunset, we might be looking at the next day's sunrise.
    // For simplicity, just find the forecasts between sunset and next 12 hours.
    val sunsetTime = city.sunset * 1000
    // If we are well past sunset (e.g. next morning before new sunset is fetched), don't show it as "tonight"
    val endOfNight = sunsetTime + 12 * 60 * 60 * 1000L
    
    val nightForecasts = forecasts.filter { 
        val dt = it.dt * 1000
        dt in sunsetTime..endOfNight
    }
    
    if (nightForecasts.isNotEmpty()) {
        val avgClouds = nightForecasts.map { it.clouds.all }.average().toInt()
        val minTemp = nightForecasts.minOf { it.main.temp }
        val avgVisibility = nightForecasts.map { it.visibility }.average().toInt() / 1000 // to km
        
        val conditionText = when {
            avgClouds < 20 -> "Excellent"
            avgClouds < 50 -> "Fair"
            else -> "Poor"
        }
        
        val color = when {
            avgClouds < 20 -> Color(0xFF4CAF50)
            avgClouds < 50 -> Color(0xFFFFC107)
            else -> Color(0xFFF44336)
        }
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tonight's Viewing",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        color = color.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = conditionText,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = color,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Cloud Cover", style = MaterialTheme.typography.labelMedium)
                        Text("$avgClouds%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                    Column {
                        Text("Min Temp", style = MaterialTheme.typography.labelMedium)
                        Text("${Math.round(minTemp)}°", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                    Column {
                        Text("Visibility", style = MaterialTheme.typography.labelMedium)
                        Text("${avgVisibility}km", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

private fun formatDay(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEE", Locale.getDefault())
    return sdf.format(Date(timestamp * 1000))
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
