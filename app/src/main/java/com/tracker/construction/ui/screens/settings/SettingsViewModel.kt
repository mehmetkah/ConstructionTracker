package com.tracker.construction.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.construction.util.SettingsStore
import com.tracker.construction.util.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsStore: SettingsStore) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val autoBackup: StateFlow<Boolean> = settingsStore.autoBackup
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val exportFolder: StateFlow<String> = settingsStore.exportFolder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SiteTracker")

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsStore.setThemeMode(mode) }
    }

    fun setAutoBackup(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setAutoBackup(enabled) }
    }

    fun setExportFolder(name: String) {
        viewModelScope.launch { settingsStore.setExportFolder(name) }
    }
}
