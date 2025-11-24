package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.filme.ListaResposta
import com.danilloteles.appnetflixapi.model.filme.TmdbList
import com.danilloteles.appnetflixapi.repository.MinhaListaRepository
import com.danilloteles.appnetflixapi.utils.events.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MinhaListaViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val minhaListaRepository: MinhaListaRepository,
) : ViewModel() {

    private val _minhasListasState = MutableStateFlow<UiState<List<TmdbList>>>(UiState.Idle)
    val minhasListasState: StateFlow<UiState<List<TmdbList>>> = _minhasListasState

    private val _listaRemovidaState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val listaRemovidaState: StateFlow<UiState<Unit>> = _listaRemovidaState

    init {
        buscarMinhasListas()
    }

    fun removerLista(listId: Int) {
        viewModelScope.launch {
            _listaRemovidaState.value = UiState.Loading
            try {
                userPreferencesRepository.sessionId.first()?.let { sessionId ->
                    val response = minhaListaRepository.removerLista(listId.toString(), sessionId)
                    // Um código de sucesso para remoção de lista geralmente é 204 (No Content),
                    // mas a API do TMDB pode ter um comportamento específico.
                    // A documentação indica um status_code 13 para sucesso na remoção.
                    // Vamos considerar a resposta bem-sucedida (código 2xx) como suficiente.
                    if (response.isSuccessful) {
                        _listaRemovidaState.value = UiState.Success(Unit)
                    } else {
                        val errorMsg = "Erro ao remover lista: ${response.code()} - ${response.message()}"
                        _listaRemovidaState.value = UiState.Error(errorMsg)
                        Log.e("TAG-MinhaListaViewModel", errorMsg)
                    }
                } ?: run {
                    val errorMsg = "Usuário não autenticado."
                    _listaRemovidaState.value = UiState.Error(errorMsg)
                    Log.e("TAG-MinhaListaViewModel", errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Exceção ao remover lista: ${e.message}"
                _listaRemovidaState.value = UiState.Error(errorMsg)
                Log.e("TAG-MinhaListaViewModel", errorMsg, e)
            }
        }
    }

    fun buscarMinhasListas() {
        viewModelScope.launch {
            _minhasListasState.value = UiState.Loading
            try {
                val sesssionId = userPreferencesRepository.sessionId.first()
                val accountId = userPreferencesRepository.accountId.first()

                if (sesssionId == null || accountId == null) {
                    _minhasListasState.value = UiState.Error("Usuário não autenticado.")
                    return@launch
                }

                val response = minhaListaRepository.obterListasDaConta(
                    accountId = accountId.toInt(),
                    sessionId = sesssionId
                )

                if (response.isSuccessful && response.body() != null) {
                    val listas = response.body()!!.results
                    _minhasListasState.value = UiState.Success(listas)
                } else {
                    val errorMsg = "Erro ao buscar listas: ${response.code()} - ${response.message()}"
                    _minhasListasState.value = UiState.Error(errorMsg)
                    Log.e("TAG-MinhaListaViewModel", errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Exceção ao buscar listas: ${e.message}"
                _minhasListasState.value = UiState.Error(errorMsg)
                Log.e("TAG-MinhaListaViewModel", errorMsg, e)
            }
        }
    }

    fun resetRemoveState() {
        _listaRemovidaState.value = UiState.Idle
    }

    class MinhaListaViewModelFactory(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val minhaListaRepository: MinhaListaRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MinhaListaViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MinhaListaViewModel(
                    userPreferencesRepository,
                    minhaListaRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}