package dev.codcow.kasirku.features.warung.ui.screens

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "warung_prefs")

class WarungPreferences(private val context: Context) {
    companion object {
        private val WARUNG_NAME_KEY = stringPreferencesKey("warung_name")
    }

    val warungName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[WARUNG_NAME_KEY]
    }

    suspend fun saveWarungName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[WARUNG_NAME_KEY] = name
        }
    }
}
