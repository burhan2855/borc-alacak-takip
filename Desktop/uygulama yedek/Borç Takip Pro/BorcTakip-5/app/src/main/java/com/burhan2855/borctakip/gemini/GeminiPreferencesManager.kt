package com.burhan2855.borctakip.gemini

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.geminiDataStore by preferencesDataStore(name = "gemini_settings")

class GeminiPreferencesManager(private val context: Context) {
    companion object {
        private val API_KEY = stringPreferencesKey("gemini_api_key")
    }

    val apiKeyFlow: Flow<String> = context.geminiDataStore.data.map { preferences ->
        preferences[API_KEY] ?: ""
    }

    suspend fun saveApiKey(apiKey: String) {
        context.geminiDataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }

    suspend fun clearApiKey() {
        context.geminiDataStore.edit { preferences ->
            preferences[API_KEY] = ""
        }
    }
}
