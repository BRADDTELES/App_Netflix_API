package com.danilloteles.appnetflixapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.utils.UiState
import com.danilloteles.appnetflixapi.utils.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyListViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val filmeAPI: FilmeAPI = RetrofitHelper.filmeAPI

    private val _uiState =
        MutableStateFlow<UiState<List<com.danilloteles.appnetflixapi.model.Filme>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<com.danilloteles.appnetflixapi.model.Filme>>> = _uiState

    // TODO: Implementar lógica para carregar filmes da lista do usuário
    // TODO: Implementar lógica para verificar se um filme está na lista
    // TODO: Implementar lógica para adicionar/remover filme da lista

    // Função para carregar os filmes da lista do usuário
    fun loadMyListMovies() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            userPreferencesRepository.sessionId.collect { sessionId ->
                if (sessionId != null) {
                    // TODO: Obter o list_id do usuário (pode ser salvo no DataStore ou obtido via API)
                    // Por enquanto, vamos simular uma lista vazia ou carregar filmes populares para teste
                    try {
                        // Exemplo: carregar filmes populares como placeholder
                        val response = filmeAPI.recuperarFilmesPopulares()
                        if (response.isSuccessful) {
                            response.body()?.let { filmeResposta ->
                                _uiState.value = UiState.Success(filmeResposta.results)
                            } ?: run {
                                _uiState.value = UiState.Error("Nenhum filme na lista.")
                            }
                        } else {
                            _uiState.value =
                                UiState.Error("Erro ao carregar filmes da lista: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        _uiState.value =
                            UiState.Error("Erro de conexão ao carregar lista: ${e.message}")
                    }
                } else {
                    _uiState.value =
                        UiState.Error("Usuário não autenticado. Faça login para ver sua lista.")
                }
            }
        }
    }

    // Factory para o MyListViewModel (similar ao LoginViewModel)

    class MyListViewModelFactory(
        private val userPreferencesRepository: UserPreferencesRepository
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MyListViewModel(userPreferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
