package com.danilloteles.appnetflixapi.utils

import com.danilloteles.appnetflixapi.model.Filme

sealed interface PopularMoviesUiState {
    object Loading : PopularMoviesUiState
    data class Success(val movies: List<Filme>) : PopularMoviesUiState
    data class Error(val message: String) : PopularMoviesUiState
}