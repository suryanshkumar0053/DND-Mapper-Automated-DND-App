package com.example.dndapp

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension function for DataStore
//preferencesDataStore("settings") initializes a DataStore<Preferences> with the name "settings".
private val Context.dataStore by preferencesDataStore("settings")

class PreferencesRepository(private val context: Context) {

    private val TOGGLE_KEY = booleanPreferencesKey("toggle_state")

    // 🔹 Read Toggle State
    val toggleState: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TOGGLE_KEY] ?: false // Default = false
    }

    // 🔹 Save Toggle State
    suspend fun saveToggleState(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TOGGLE_KEY] = isEnabled
        }
    }
}
