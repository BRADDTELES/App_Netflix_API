package com.danilloteles.appnetflixapi.utils

import com.danilloteles.appnetflixapi.model.Filme

sealed interface UiState {
    object Loading : UiState
    data class Success(val movies: List<Filme>) : UiState
    data class Error(val message: String) : UiState
}