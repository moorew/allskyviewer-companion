package de.astronarren.allsky.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.widget.RemoteViews
import de.astronarren.allsky.MainActivity
import de.astronarren.allsky.R
import de.astronarren.allsky.data.UserPreferences
import de.astronarren.allsky.data.network.WeatherApiProvider
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class AllskyWidgetProvider : AppWidgetProvider() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == REFRESH_ACTION) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_allsky)
        
        // Open App on image click
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_image, openAppPendingIntent)

        // Set up refresh button click
        val refreshIntent = Intent(context, AllskyWidgetProvider::class.java).apply {
            action = REFRESH_ACTION
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)

        views.setTextViewText(
            R.id.widget_last_update,
            context.getString(R.string.widget_refreshing)
        )
        appWidgetManager.updateAppWidget(appWidgetId, views)

        scope.launch(Dispatchers.IO) {
            val userPrefs = UserPreferences(context)
            
            // 1. Fetch Weather Data
            try {
                val apiKey = userPrefs.getApiKey()
                val latStr = userPrefs.getLatitude()
                val lonStr = userPrefs.getLongitude()
                val lat = latStr.toDoubleOrNull()
                val lon = lonStr.toDoubleOrNull()
                
                if (lat != null && lon != null && apiKey.isNotBlank()) {
                    val weatherService = WeatherApiProvider.provideWeatherService()
                    val response = weatherService.getForecast(lat = lat, lon = lon, apiKey = apiKey)
                    
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val displaySdf = SimpleDateFormat("EEE", Locale.getDefault())
                    
                    val dailyForecasts = response.list
                        .groupBy { sdf.format(Date(it.dt * 1000)) }
                        .map { it.value.first() }
                        .take(3)
                        
                    if (dailyForecasts.size == 3) {
                        withContext(Dispatchers.Main) {
                            views.setViewVisibility(R.id.widget_weather_container, View.VISIBLE)
                            
                            views.setTextViewText(R.id.widget_weather_day1_date, displaySdf.format(Date(dailyForecasts[0].dt * 1000)).uppercase())
                            views.setTextViewText(R.id.widget_weather_day1_desc, "${Math.round(dailyForecasts[0].main.temp)}°C | ${dailyForecasts[0].clouds.all}%")
                            
                            views.setTextViewText(R.id.widget_weather_day2_date, displaySdf.format(Date(dailyForecasts[1].dt * 1000)).uppercase())
                            views.setTextViewText(R.id.widget_weather_day2_desc, "${Math.round(dailyForecasts[1].main.temp)}°C | ${dailyForecasts[1].clouds.all}%")
                            
                            views.setTextViewText(R.id.widget_weather_day3_date, displaySdf.format(Date(dailyForecasts[2].dt * 1000)).uppercase())
                            views.setTextViewText(R.id.widget_weather_day3_desc, "${Math.round(dailyForecasts[2].main.temp)}°C | ${dailyForecasts[2].clouds.all}%")
                            
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore weather fetch errors, let the image fetch proceed
            }

            // 2. Fetch Live Image Data
            try {
                var allskyUrl = userPrefs.getAllskyUrl()
                val username = userPrefs.getUsername()
                val password = userPrefs.getPassword()

                if (allskyUrl.isNotEmpty()) {
                    allskyUrl = allskyUrl.trimEnd('/')
                    
                    // Same resolution logic as LiveImageViewModel
                    fun testPath(path: String): Int {
                        val testUrl = URL(path)
                        val conn = testUrl.openConnection() as HttpURLConnection
                        conn.requestMethod = "HEAD"
                        if (username.isNotEmpty() && password.isNotEmpty()) {
                            val basicAuth = "Basic " + Base64.encodeToString("$username:$password".toByteArray(), Base64.NO_WRAP)
                            conn.setRequestProperty("Authorization", basicAuth)
                        }
                        conn.connectTimeout = 3000
                        conn.readTimeout = 3000
                        val code = conn.responseCode
                        conn.disconnect()
                        return code
                    }

                    var liveImagePath: String? = null
                    val rootUrl = if (allskyUrl.endsWith("/allsky")) allskyUrl.substring(0, allskyUrl.length - 7) else allskyUrl
                    
                    if (testPath("$rootUrl/current/tmp/image.jpg") == 200) {
                        liveImagePath = "$rootUrl/current/tmp/image.jpg"
                    } else if (testPath("$allskyUrl/image.jpg") == 200) {
                        liveImagePath = "$allskyUrl/image.jpg"
                    } else if (!allskyUrl.endsWith("/allsky") && testPath("$allskyUrl/allsky/image.jpg") == 200) {
                        liveImagePath = "$allskyUrl/allsky/image.jpg"
                    }

                    val finalUrl = liveImagePath ?: "$allskyUrl/image.jpg"
                    val imageUrl = "$finalUrl?t=${System.currentTimeMillis()}"

                    val url = URL(imageUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        val basicAuth = "Basic " + Base64.encodeToString("$username:$password".toByteArray(), Base64.NO_WRAP)
                        connection.setRequestProperty("Authorization", basicAuth)
                    }
                    connection.doInput = true
                    connection.connectTimeout = 5000
                    connection.readTimeout = 10000
                    connection.connect()

                    if (connection.responseCode == 200) {
                        val inputStream = connection.inputStream
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        
                        withContext(Dispatchers.Main) {
                            views.setImageViewBitmap(R.id.widget_image, bitmap)
                            views.setTextViewText(
                                R.id.widget_last_update,
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                            )
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        }
                        inputStream.close()
                    } else {
                        throw Exception("HTTP ${connection.responseCode}")
                    }
                    connection.disconnect()
                } else {
                    withContext(Dispatchers.Main) {
                        views.setTextViewText(
                            R.id.widget_last_update,
                            context.getString(R.string.widget_no_url)
                        )
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    views.setTextViewText(
                        R.id.widget_last_update,
                        context.getString(R.string.widget_error)
                    )
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    companion object {
        const val REFRESH_ACTION = "de.astronarren.allsky.widget.REFRESH"
    }
}