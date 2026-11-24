package com.aradium.aradium

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

data class Settings(
    val userName: String = "",
    val darkMode: Boolean = true,
    val sortOrder: String = "date"
)

class SettingsStore(private val context: Context) {

    private val nameKey = stringPreferencesKey("user_name")
    private val darkKey = booleanPreferencesKey("dark_mode")
    private val sortKey = stringPreferencesKey("sort_order")

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            userName = prefs[nameKey] ?: "",
            darkMode = prefs[darkKey] ?: true,
            sortOrder = prefs[sortKey] ?: "date"
        )
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[nameKey] = name }
    }

    suspend fun setDarkMode(dark: Boolean) {
        context.dataStore.edit { it[darkKey] = dark }
    }

    suspend fun setSortOrder(order: String) {
        context.dataStore.edit { it[sortKey] = order }
    }
}
