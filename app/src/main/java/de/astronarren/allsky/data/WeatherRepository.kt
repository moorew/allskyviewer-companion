package de.astronarren.allsky.data

import de.astronarren.allsky.utils.LocationManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository(
    private val locationManager: LocationManager,
    private val weatherService: WeatherService,
    private val userPreferences: UserPreferences
) {
    suspend fun getForecast(lat: Double? = null, lon: Double? = null): Result<WeatherResponse> {
        return try {
            val apiKey = userPreferences.getApiKey()
            if (apiKey.isEmpty()) {
                return Result.failure(Exception("API key not configured"))
            }

            val finalLat: Double
            val finalLon: Double

            if (lat != null && lon != null) {
                finalLat = lat
                finalLon = lon
            } else {
                if (!locationManager.isLocationPermissionGranted()) {
                    return Result.failure(Exception("Location permission required"))
                }
                
                val location = locationManager.getCurrentLocation() ?: 
                    return Result.failure(Exception("Location not available"))
                
                finalLat = location.latitude
                finalLon = location.longitude
            }

            val response = weatherService.getForecast(
                lat = finalLat,
                lon = finalLon,
                apiKey = apiKey
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 