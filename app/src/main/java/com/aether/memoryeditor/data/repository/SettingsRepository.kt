package com.aether.memoryeditor.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aether.memoryeditor.data.model.DataType
import com.aether.memoryeditor.data.model.MemoryRegion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    enum class Theme {
        LIGHT, DARK, SYSTEM
    }

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val DEFAULT_DATA_TYPE = stringPreferencesKey("default_data_type")
        val DEFAULT_REGIONS = stringPreferencesKey("default_regions")
        val THREAD_COUNT = intPreferencesKey("thread_count")
        val BUFFER_SIZE_MB = intPreferencesKey("buffer_size_mb")
        val AUTO_FREEZE = booleanPreferencesKey("auto_freeze")
        val SHOW_HEX_VALUES = booleanPreferencesKey("show_hex_values")
        val STEALTH_MODE = booleanPreferencesKey("stealth_mode")
        val HIDE_OVERLAY = booleanPreferencesKey("hide_overlay")
        val OVERLAY_OPACITY = floatPreferencesKey("overlay_opacity")
        val OVERLAY_SIZE = floatPreferencesKey("overlay_size")
    }

    private val _theme = MutableStateFlow(Theme.SYSTEM)
    val theme: StateFlow<Theme> = _theme.asStateFlow()

    private val _defaultDataType = MutableStateFlow(DataType.DWORD)
    val defaultDataType: StateFlow<DataType> = _defaultDataType.asStateFlow()

    private val _defaultRegions = MutableStateFlow(setOf(
        MemoryRegion.C_HEAP,
        MemoryRegion.C_ALLOC,
        MemoryRegion.ANONYMOUS
    ))
    val defaultRegions: StateFlow<Set<MemoryRegion>> = _defaultRegions.asStateFlow()

    private val _threadCount = MutableStateFlow(4)
    val threadCount: StateFlow<Int> = _threadCount.asStateFlow()

    private val _bufferSizeMb = MutableStateFlow(64)
    val bufferSizeMb: StateFlow<Int> = _bufferSizeMb.asStateFlow()

    private val _autoFreeze = MutableStateFlow(false)
    val autoFreeze: StateFlow<Boolean> = _autoFreeze.asStateFlow()

    private val _showHexValues = MutableStateFlow(false)
    val showHexValues: StateFlow<Boolean> = _showHexValues.asStateFlow()

    private val _stealthMode = MutableStateFlow(false)
    val stealthMode: StateFlow<Boolean> = _stealthMode.asStateFlow()

    private val _hideOverlay = MutableStateFlow(false)
    val hideOverlay: StateFlow<Boolean> = _hideOverlay.asStateFlow()

    private val _overlayOpacity = MutableStateFlow(0.8f)
    val overlayOpacity: StateFlow<Float> = _overlayOpacity.asStateFlow()

    private val _overlaySize = MutableStateFlow(1.0f)
    val overlaySize: StateFlow<Float> = _overlaySize.asStateFlow()

    suspend fun setTheme(theme: Theme) {
        _theme.value = theme
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    suspend fun setDefaultDataType(type: DataType) {
        _defaultDataType.value = type
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_DATA_TYPE] = type.name
        }
    }

    suspend fun setDefaultRegions(regions: Set<MemoryRegion>) {
        _defaultRegions.value = regions
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REGIONS] = regions.joinToString(",") { it.name }
        }
    }

    suspend fun setThreadCount(count: Int) {
        _threadCount.value = count
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THREAD_COUNT] = count
        }
    }

    suspend fun setBufferSizeMb(size: Int) {
        _bufferSizeMb.value = size
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUFFER_SIZE_MB] = size
        }
    }

    suspend fun setAutoFreeze(autoFreeze: Boolean) {
        _autoFreeze.value = autoFreeze
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_FREEZE] = autoFreeze
        }
    }

    suspend fun setShowHexValues(show: Boolean) {
        _showHexValues.value = show
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_HEX_VALUES] = show
        }
    }

    suspend fun setStealthMode(enabled: Boolean) {
        _stealthMode.value = enabled
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STEALTH_MODE] = enabled
        }
    }

    suspend fun setHideOverlay(hide: Boolean) {
        _hideOverlay.value = hide
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_OVERLAY] = hide
        }
    }

    suspend fun setOverlayOpacity(opacity: Float) {
        _overlayOpacity.value = opacity
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OVERLAY_OPACITY] = opacity
        }
    }

    suspend fun setOverlaySize(size: Float) {
        _overlaySize.value = size
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OVERLAY_SIZE] = size
        }
    }
}
