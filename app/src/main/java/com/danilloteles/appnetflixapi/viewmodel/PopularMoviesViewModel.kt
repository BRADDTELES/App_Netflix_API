package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.model.Filme
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PopularMoviesViewModel : ViewModel() {

    private val filmeAPI = RetrofitHelper.filmeAPI

    private val _uiState = MutableStateFlow<UiState<List<Filme>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Filme>>> = _uiState

    init {
        buscarFilmesPopulares()
    }

    private fun buscarFilmesPopulares() {

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = filmeAPI.recuperarFilmesPopulares()
                if (  response.isSuccessful  ) {
                    response.body()?.let { filmeResposta ->
                        _uiState.value = UiState.Success(filmeResposta.results)
                    } ?: run {
                        _uiState.value = UiState.Error("Nenhum filme encontrado.")
                    }
                } else {
                    _uiState.value = UiState.Error("Erro ao carregar filmes")
                    Log.e("PopularMoviesViewModel", "Erro ao carregar filmes: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro de conexão")
                Log.e("PopularMoviesViewModel", "Erro de conexão: ${e.message}")
            }
        }
    }
}