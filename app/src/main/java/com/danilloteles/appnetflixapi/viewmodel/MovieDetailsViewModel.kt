package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.utils.DetailsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieDetailsViewModel(
    private val movieId: Int
) : ViewModel() {

    private val filmeAPI = RetrofitHelper.filmeAPI

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState

    init {
        buscarDetalhesFilme()
    }

    private fun buscarDetalhesFilme() {
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            try {
                val response = filmeAPI.recuperarDetalhesFilme(movieId)
                if (response.isSuccessful) {
                    response.body()?.let { movie ->
                        _uiState.value = DetailsUiState.Success(movie)
                    } ?: run {
                        _uiState.value = DetailsUiState.Error("Nenhum filme encontrado.")
                    }
                } else {
                    _uiState.value = DetailsUiState.Error("Erro ao buscar detalhes do filme.")
                    Log.e("MovieDetailsViewModel", "Erro ao buscar detalhes do filme: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = DetailsUiState.Error("Erro ao buscar detalhes do filme.")
                Log.e("MovieDetailsViewModel", "Erro ao buscar detalhes do filme: ${e.message}")
            }
        }
    }

    class Factory(private val movieId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MovieDetailsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MovieDetailsViewModel(movieId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}