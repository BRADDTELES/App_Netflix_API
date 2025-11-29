/* TODO: Classe por enquanto sem uso */
package com.danilloteles.appnetflixapi.utils.events

import com.danilloteles.appnetflixapi.model.v3.filme.FilmeDetalhes

sealed interface DetailsUiState {
    object Loading : DetailsUiState
    data class Success(val movie: FilmeDetalhes) : DetailsUiState
    data class Error(val message: String) : DetailsUiState
}