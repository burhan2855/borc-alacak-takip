package com.burhan2855.borctakip.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val currencyCodeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CURRENCY_CODE] ?: "TRY"
        }

    val currencySymbolFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CURRENCY_SYMBOL] ?: "₺"
        }

    suspend fun setCurrency(code: String, symbol: String) {
        context.dataStore.edit {
            it[PreferencesKeys.CURRENCY_CODE] = code
            it[PreferencesKeys.CURRENCY_SYMBOL] = symbol
        }
    }

    val languageFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LANGUAGE] ?: "tr" // Default to Turkish
        }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit {
            it[PreferencesKeys.LANGUAGE] = language
        }
    }
}
