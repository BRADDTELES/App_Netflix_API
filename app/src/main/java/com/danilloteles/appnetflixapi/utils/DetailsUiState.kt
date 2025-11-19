/* TODO: Classe por enquanto sem uso */
package com.danilloteles.appnetflixapi.utils

import com.danilloteles.appnetflixapi.model.FilmeDetalhes

sealed interface DetailsUiState {
    object Loading : DetailsUiState
    data class Success(val movie: FilmeDetalhes) : DetailsUiState
    data class Error(val message: String) : DetailsUiState
}