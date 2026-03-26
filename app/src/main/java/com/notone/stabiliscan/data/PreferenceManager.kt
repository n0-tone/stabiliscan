package com.notone.stabiliscan.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class PreferenceManager(private val context: Context) {

    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SAVED_TEXTS = stringSetPreferencesKey("saved_texts")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    val savedTexts: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            preferences[SAVED_TEXTS]?.toList() ?: emptyList()
        }

    suspend fun saveText(text: String) {
        if (text.isBlank()) return
        context.dataStore.edit { preferences ->
            val currentTexts = preferences[SAVED_TEXTS] ?: emptySet()
            preferences[SAVED_TEXTS] = currentTexts + text
        }
    }
    
    suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
            preferences[SAVED_TEXTS] = emptySet()
        }
    }
}