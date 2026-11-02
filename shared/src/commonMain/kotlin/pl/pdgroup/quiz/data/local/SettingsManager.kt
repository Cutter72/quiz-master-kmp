package pl.pdgroup.quiz.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsManager(
    private val dataStore: DataStore<Preferences>
) {
    private val isDarkModeKey = booleanPreferencesKey("is_dark_mode")
    private val isHighContrastKey = booleanPreferencesKey("is_high_contrast")

    val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[isDarkModeKey] ?: false
    }

    val isHighContrast: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[isHighContrastKey] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[isDarkModeKey] = enabled
        }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[isHighContrastKey] = enabled
        }
    }
}
