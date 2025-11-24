package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.model.ListDetailsResponse
import com.danilloteles.appnetflixapi.repository.MinhaListaRepository
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.utils.events.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ConteudoViewModel(
    private val listId: String,
    private val minhaListaRepository: MinhaListaRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _conteudoState = MutableStateFlow<UiState<ListDetailsResponse>>(UiState.Idle)
    val conteudoState: StateFlow<UiState<ListDetailsResponse>> = _conteudoState

    init {
        buscarConteudo()
    }

    fun buscarConteudo() {
        viewModelScope.launch {
            _conteudoState.value = UiState.Loading
            try {
                val sessionId = userPreferencesRepository.sessionId.first()
                if (sessionId == null) {
                    _conteudoState.value = UiState.Error("Usuário não autenticado.")
                    return@launch
                }

                val response = minhaListaRepository.obterDetalhesDaLista(listId, sessionId)
                if (response.isSuccessful && response.body() != null) {
                    _conteudoState.value = UiState.Success(response.body()!!)
                } else {
                    val errorMsg = "Erro ao buscar conteúdo da lista: ${response.code()} - ${response.message()}"
                    _conteudoState.value = UiState.Error(errorMsg)
                    Log.e("TAG-ConteudoViewModel", errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Exceção ao buscar conteúdo da lista: ${e.message}"
                _conteudoState.value = UiState.Error(errorMsg)
                Log.e("TAG-ConteudoViewModel", errorMsg, e)
            }
        }
    }

    class ConteudoViewModelFactory(
        private val listId: String,
        private val minhaListaRepository: MinhaListaRepository,
        private val userPreferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ConteudoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ConteudoViewModel(listId, minhaListaRepository, userPreferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
