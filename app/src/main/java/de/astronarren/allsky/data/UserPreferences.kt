package de.astronarren.allsky.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserPreferences(private val context: Context) {
    companion object {
        private val ALLSKY_URL = stringPreferencesKey("allsky_url")
        private val API_KEY = stringPreferencesKey("api_key")
        private val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
        private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
        private val LAST_NOTIFICATION_DATE = stringPreferencesKey("last_notification_date")
        private val USERNAME = stringPreferencesKey("username")
        private val PASSWORD = stringPreferencesKey("password")
        private val MAIN_LAYOUT = stringPreferencesKey("main_layout")
        private val LATITUDE = stringPreferencesKey("latitude")
        private val LONGITUDE = stringPreferencesKey("longitude")
        
        private const val DEFAULT_LAYOUT = "LIVE_VIEW,BEST_VIEWING,WEATHER,MOON,TIMELAPSES,METEORS,IMAGES,KEOGRAMS,STARTRAILS"
        private const val DEFAULT_API_KEY = "9908d92979873f12ec6eaecc05335284"
    }

    suspend fun saveLatitude(lat: String) {
        context.dataStore.edit { preferences ->
            preferences[LATITUDE] = lat
        }
    }

    suspend fun getLatitude(): String = withContext(Dispatchers.IO) {
        context.dataStore.data.first()[LATITUDE] ?: ""
    }

    fun getLatitudeFlow(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[LATITUDE] ?: ""
        }
    }

    suspend fun saveLongitude(lon: String) {
        context.dataStore.edit { preferences ->
            preferences[LONGITUDE] = lon
        }
    }

    suspend fun getLongitude(): String = withContext(Dispatchers.IO) {
        context.dataStore.data.first()[LONGITUDE] ?: ""
    }

    fun getLongitudeFlow(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[LONGITUDE] ?: ""
        }
    }

    suspend fun saveMainLayout(layout: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[MAIN_LAYOUT] = layout.joinToString(",")
        }
    }

    suspend fun getMainLayout(): List<String> = withContext(Dispatchers.IO) {
        val saved = context.dataStore.data.first()[MAIN_LAYOUT] ?: DEFAULT_LAYOUT
        val list = saved.split(",").filter { it.isNotBlank() }.toMutableList()
        DEFAULT_LAYOUT.split(",").forEach { module ->
            if (!list.contains(module)) {
                if (module == "BEST_VIEWING") {
                    val index = list.indexOf("LIVE_VIEW")
                    if (index != -1) list.add(index + 1, module) else list.add(0, module)
                } else {
                    list.add(module)
                }
            }
        }
        list
    }

    fun getMainLayoutFlow(): Flow<List<String>> {
        return context.dataStore.data.map { preferences ->
            val saved = preferences[MAIN_LAYOUT] ?: DEFAULT_LAYOUT
            val list = saved.split(",").filter { it.isNotBlank() }.toMutableList()
            DEFAULT_LAYOUT.split(",").forEach { module ->
                if (!list.contains(module)) {
                    if (module == "BEST_VIEWING") {
                        val index = list.indexOf("LIVE_VIEW")
                        if (index != -1) list.add(index + 1, module) else list.add(0, module)
                    } else {
                        list.add(module)
                    }
                }
            }
            list
        }
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME] = username
        }
    }

    suspend fun getUsername(): String = withContext(Dispatchers.IO) {
        context.dataStore.data.first()[USERNAME] ?: ""
    }

    fun getUsernameFlow(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[USERNAME] ?: ""
        }
    }

    suspend fun savePassword(password: String) {
        context.dataStore.edit { preferences ->
            preferences[PASSWORD] = password
        }
    }

    suspend fun getPassword(): String = withContext(Dispatchers.IO) {
        context.dataStore.data.first()[PASSWORD] ?: ""
    }

    fun getPasswordFlow(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[PASSWORD] ?: ""
        }
    }

    suspend fun saveLastNotificationDate(dateStr: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_NOTIFICATION_DATE] = dateStr
        }
    }

    suspend fun getLastNotificationDate(): String = withContext(Dispatchers.IO) {
        context.dataStore.data.first()[LAST_NOTIFICATION_DATE] ?: ""
    }

    suspend fun saveAllskyUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[ALLSKY_URL] = url
        }
    }

    suspend fun getAllskyUrl(): String = withContext(Dispatchers.IO) {
        context.dataStore.data.first()[ALLSKY_URL] ?: ""
    }

    fun getAllskyUrlFlow(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[ALLSKY_URL] ?: ""
        }
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }

    suspend fun getApiKey(): String = withContext(Dispatchers.IO) {
        context.dataStore.data.first()[API_KEY] ?: DEFAULT_API_KEY
    }

    fun getApiKeyFlow(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[API_KEY] ?: DEFAULT_API_KEY
        }
    }

    suspend fun markSetupComplete() {
        context.dataStore.edit { preferences ->
            preferences[SETUP_COMPLETE] = true
        }
    }

    suspend fun isSetupComplete(): Boolean = withContext(Dispatchers.IO) {
        context.dataStore.data.first()[SETUP_COMPLETE] ?: false
    }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }

    suspend fun getLanguage(): String = withContext(Dispatchers.IO) {
        context.dataStore.data.first()[LANGUAGE_KEY] ?: ""
    }
}