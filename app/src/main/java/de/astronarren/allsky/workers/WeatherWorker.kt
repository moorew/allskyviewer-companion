package de.astronarren.allsky.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.astronarren.allsky.data.UserPreferences
import de.astronarren.allsky.data.WeatherService
import de.astronarren.allsky.utils.NotificationHelper
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userPreferences = UserPreferences(applicationContext)
        val apiKey = userPreferences.getApiKey()
        if (apiKey.isBlank()) return Result.failure()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val weatherService = retrofit.create(WeatherService::class.java)
        
        // Use station coordinates from preferences instead of device GPS
        val latStr = userPreferences.getLatitude()
        val lonStr = userPreferences.getLongitude()
        
        val lat = latStr.toDoubleOrNull()
        val lon = lonStr.toDoubleOrNull()

        if (lat == null || lon == null) {
            println("Debug: WeatherWorker - Station coordinates not set")
            return Result.failure()
        }
        
        return try {
            val response = weatherService.getForecast(
                lat = lat,
                lon = lon,
                apiKey = apiKey
            )
            
            // Check for Night Conditions Notification
            val currentTime = System.currentTimeMillis()
            val sunsetTime = response.city.sunset * 1000
            val threeHours = 3 * 60 * 60 * 1000L
            val isNearSunset = currentTime in (sunsetTime - threeHours)..(sunsetTime + 60 * 60 * 1000L)
            
            val currentDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val lastNotifiedDate = userPreferences.getLastNotificationDate()
            
            if (isNearSunset && currentDateStr != lastNotifiedDate) {
                val endOfNight = sunsetTime + 12 * 60 * 60 * 1000L
                val nightForecasts = response.list.filter {
                    val dt = it.dt * 1000
                    dt in sunsetTime..endOfNight
                }
                
                if (nightForecasts.isNotEmpty()) {
                    val avgClouds = nightForecasts.map { it.clouds.all }.average().toInt()
                    val minTemp = nightForecasts.minOf { it.main.temp }
                    
                    val notificationHelper = NotificationHelper(applicationContext)
                    notificationHelper.showNightConditionsNotification(avgClouds, minTemp)
                    
                    userPreferences.saveLastNotificationDate(currentDateStr)
                }
            }
            
            // Still check the immediate forecast for clear skies if needed (existing logic)
            val nextForecast = response.list.firstOrNull()
            if (nextForecast != null && nextForecast.clouds.all < 20) {
                val notificationHelper = NotificationHelper(applicationContext)
                notificationHelper.showClearSkyNotification(nextForecast.clouds.all, nextForecast.main.temp)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
