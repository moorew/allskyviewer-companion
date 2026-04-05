package de.astronarren.allsky.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class UserPreferences(private val context: Context) {
    companion object {
        private val ALLSKY_URL = stringPreferencesKey("allsky_url")
        private val API_KEY = stringPreferencesKey("api_key")
        private val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
        private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
        private val LAST_NOTIFICATION_DATE = stringPreferencesKey("last_notification_date")
        private val USERNAME = stringPreferencesKey("username")
        private val PASSWORD = stringPreferencesKey("password")
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME] = username
        }
    }

    fun getUsername(): String {
        return runBlocking {
            context.dataStore.data.first()[USERNAME] ?: ""
        }
    }

    suspend fun savePassword(password: String) {
        context.dataStore.edit { preferences ->
            preferences[PASSWORD] = password
        }
    }

    fun getPassword(): String {
        return runBlocking {
            context.dataStore.data.first()[PASSWORD] ?: ""
        }
    }

    suspend fun saveLastNotificationDate(dateStr: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_NOTIFICATION_DATE] = dateStr
        }
    }

    fun getLastNotificationDate(): String {
        return runBlocking {
            context.dataStore.data.first()[LAST_NOTIFICATION_DATE] ?: ""
        }
    }

    suspend fun saveAllskyUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[ALLSKY_URL] = url
        }
    }

    fun getAllskyUrl(): String {
        return runBlocking {
            context.dataStore.data.first()[ALLSKY_URL] ?: ""
        }
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

    fun getApiKey(): String {
        return runBlocking {
            context.dataStore.data.first()[API_KEY] ?: ""
        }
    }

    fun getApiKeyFlow(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[API_KEY] ?: ""
        }
    }

    suspend fun markSetupComplete() {
        context.dataStore.edit { preferences ->
            preferences[SETUP_COMPLETE] = true
        }
    }

    fun isSetupComplete(): Boolean {
        return runBlocking {
            context.dataStore.data.first()[SETUP_COMPLETE] ?: false
        }
    }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }

    fun getLanguage(): String {
        return runBlocking {
            context.dataStore.data.first()[LANGUAGE_KEY] ?: ""
        }
    }
} 