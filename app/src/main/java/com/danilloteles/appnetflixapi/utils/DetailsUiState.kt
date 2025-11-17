package com.danilloteles.appnetflixapi.utils

import com.danilloteles.appnetflixapi.model.Filme

sealed interface DetailsUiState {
    object Loading : DetailsUiState
    data class Success(val movie: Filme) : DetailsUiState
    data class Error(val message: String) : DetailsUiState
}