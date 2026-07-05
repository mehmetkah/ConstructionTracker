package com.tracker.construction.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode { LIGHT, DARK, SYSTEM }

class SettingsStore(private val context: Context) {

    private object Keys {
        val THEME = intPreferencesKey("theme_mode")
        val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
        val EXPORT_FOLDER = stringPreferencesKey("export_folder")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.THEME] ?: 2) {
            0 -> ThemeMode.LIGHT
            1 -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    val autoBackup: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTO_BACKUP] ?: false }

    val exportFolder: Flow<String> = context.dataStore.data.map { it[Keys.EXPORT_FOLDER] ?: "SiteTracker" }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.ordinal }
    }

    suspend fun setAutoBackup(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_BACKUP] = enabled }
    }

    suspend fun setExportFolder(name: String) {
        context.dataStore.edit { it[Keys.EXPORT_FOLDER] = name }
    }
}
