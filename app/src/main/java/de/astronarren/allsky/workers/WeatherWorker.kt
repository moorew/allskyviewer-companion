package de.astronarren.allsky.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.astronarren.allsky.data.UserPreferences
import de.astronarren.allsky.data.WeatherService
import de.astronarren.allsky.utils.LocationManager
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
        
        val locationManager = LocationManager(applicationContext)
        
        return try {
            val location = locationManager.getCurrentLocation() ?: return Result.retry()
            
            val response = weatherService.getForecast(
                lat = location.latitude,
                lon = location.longitude,
                apiKey = apiKey
            )
            
            // Check first forecast in the list (usually nearest 3-hour window)
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
