package com.example.weathernow.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.weathernow.data.model.CityLocation
import com.example.weathernow.data.model.TemperatureUnit
import com.example.weathernow.data.model.ThemeMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_now_prefs")

class PreferencesManager(private val context: Context) {

    private val gson = Gson()

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_TEMP_UNIT = stringPreferencesKey("temp_unit")
        val KEY_FAVORITE_CITIES = stringPreferencesKey("favorite_cities")
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val modeName = preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeName)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    val temperatureUnitFlow: Flow<TemperatureUnit> = context.dataStore.data.map { preferences ->
        val unitName = preferences[KEY_TEMP_UNIT] ?: TemperatureUnit.CELSIUS.name
        try {
            TemperatureUnit.valueOf(unitName)
        } catch (_: Exception) {
            TemperatureUnit.CELSIUS
        }
    }

    val favoriteCitiesFlow: Flow<List<CityLocation>> = context.dataStore.data.map { preferences ->
        val json = preferences[KEY_FAVORITE_CITIES]
        if (json.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                val type = object : TypeToken<List<CityLocation>>() {}.type
                gson.fromJson<List<CityLocation>>(json, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TEMP_UNIT] = unit.name
        }
    }

    suspend fun addFavoriteCity(city: CityLocation) {
        context.dataStore.edit { preferences ->
            val json = preferences[KEY_FAVORITE_CITIES]
            val currentList = if (json.isNullOrBlank()) {
                mutableListOf()
            } else {
                try {
                    val type = object : TypeToken<List<CityLocation>>() {}.type
                    gson.fromJson<List<CityLocation>>(json, type)?.toMutableList() ?: mutableListOf()
                } catch (_: Exception) {
                    mutableListOf()
                }
            }

            // Prevent duplicates by ID or identical coordinates
            val alreadyExists = currentList.any { it.id == city.id || (it.name == city.name && it.country == city.country) }
            if (!alreadyExists) {
                currentList.add(city)
                preferences[KEY_FAVORITE_CITIES] = gson.toJson(currentList)
            }
        }
    }

    suspend fun removeFavoriteCity(cityId: Long) {
        context.dataStore.edit { preferences ->
            val json = preferences[KEY_FAVORITE_CITIES]
            if (!json.isNullOrBlank()) {
                try {
                    val type = object : TypeToken<List<CityLocation>>() {}.type
                    val currentList = gson.fromJson<List<CityLocation>>(json, type)?.toMutableList() ?: mutableListOf()
                    val filtered = currentList.filter { it.id != cityId }
                    preferences[KEY_FAVORITE_CITIES] = gson.toJson(filtered)
                } catch (_: Exception) {
                    // Ignored
                }
            }
        }
    }
}
