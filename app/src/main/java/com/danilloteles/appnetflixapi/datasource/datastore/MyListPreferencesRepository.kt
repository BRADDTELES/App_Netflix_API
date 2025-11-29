package com.danilloteles.appnetflixapi.datasource.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.danilloteles.appnetflixapi.model.v3.filme.Filme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensão para DataStore para preferências específicas da Minha Lista
val Context.myListStore: DataStore<Preferences> by preferencesDataStore(name = "my_list_preferences")

class MyListPreferencesRepository(private val context: Context) {

    private val gson = Gson()

    companion object {
        val MY_MOVIE_LIST_KEY = stringPreferencesKey("my_movie_list")
    }

    // Salva a lista de filmes no DataStore
    suspend fun saveMyMovieList(movies: List<Filme>) {
        context.myListStore.edit { preferences ->
            val jsonString = gson.toJson(movies)
            preferences[MY_MOVIE_LIST_KEY] = jsonString
        }
    }

    // Carrega a lista de filmes do DataStore
    val myMovieList: Flow<List<Filme>> = context.myListStore.data
        .map { preferences ->
            val jsonString = preferences[MY_MOVIE_LIST_KEY] ?: "[]"
            val type = object : TypeToken<List<Filme>>() {}.type
            gson.fromJson(jsonString, type)
        }

    // Limpa a lista de filmes do DataStore
    suspend fun clearMyMovieList() {
        context.myListStore.edit { preferences ->
            preferences.remove(MY_MOVIE_LIST_KEY)
        }
    }
}
