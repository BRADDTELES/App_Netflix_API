package com.danilloteles.appnetflixapi.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(
    private val context: Context
) {

    companion object {
        val SESSION_ID_KEY = stringPreferencesKey("session_id")
        val ACCOUNT_ID_KEY = stringPreferencesKey("account_id") // Nova chave
        val PRIMARY_LIST_ID_KEY = stringPreferencesKey("primary_list_id") // Nova chave para o ID da lista principal
    }

    suspend fun saveSessionId(sessionId: String) {
        context.dataStore.edit { preferences ->
            preferences[SESSION_ID_KEY] = sessionId
        }
    }

    val sessionId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SESSION_ID_KEY]
        }

    // Novas funções para account_id
    suspend fun saveAccountId(accountId: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCOUNT_ID_KEY] = accountId
        }
    }

    val accountId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[ACCOUNT_ID_KEY]
        }

    // Novas funções para primary_list_id
    suspend fun savePrimaryListId(listId: String) {
        context.dataStore.edit { preferences ->
            preferences[PRIMARY_LIST_ID_KEY] = listId
        }
    }

    val primaryListId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PRIMARY_LIST_ID_KEY]
        }


    // Função para limpar todas as preferências
    suspend fun clearPreferences() {
        context.dataStore.edit { it.clear() }
    }
}

