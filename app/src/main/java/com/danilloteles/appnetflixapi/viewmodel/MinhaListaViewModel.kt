package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.filme.TmdbList
import com.danilloteles.appnetflixapi.repository.MinhaListaRepository
import com.danilloteles.appnetflixapi.utils.events.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MinhaListaViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val minhaListaRepository: MinhaListaRepository,
) : ViewModel() {

    private val _minhasListasState = MutableStateFlow<UiState<List<TmdbList>>>(UiState.Idle)
    val minhasListasState: StateFlow<UiState<List<TmdbList>>> = _minhasListasState

    private val _listaRemovidaState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val listaRemovidaState: StateFlow<UiState<Unit>> = _listaRemovidaState

    init {
        observeAuthenticationAndFetchLists()
    }

    private fun observeAuthenticationAndFetchLists() {
        val accessTokenFlow = userPreferencesRepository.accessTokenV4
        val accountIdFlow = userPreferencesRepository.accountId

        combine(accessTokenFlow, accountIdFlow) { token, id ->
            if (!token.isNullOrEmpty() && !id.isNullOrEmpty()) {
                Log.d("MinhaListaViewModel", "Token e ID válidos. Buscando listas.")
                token to id
            } else {
                Log.d("MinhaListaViewModel", "Token ou ID nulos. Usuário não autenticado.")
                null
            }
        }.flatMapLatest { credentials ->
            _minhasListasState.value = UiState.Loading
            if (credentials != null) {
                val (accessToken, accountId) = credentials
                minhaListaRepository.obterListasDaContaV4(accountId, "Bearer $accessToken")
            } else {
                MutableStateFlow(Result.HttpError(401, "Usuário não autenticado."))
            }
        }.onEach { result ->
            when (result) {
                is Result.Sucesso -> {
                    _minhasListasState.value = UiState.Success(result.data.results)
                }
                is Result.HttpError -> {
                    _minhasListasState.value = UiState.Error("Erro ${result.code}: ${result.mensagem}")
                }
                is Result.NetworkError -> {
                    _minhasListasState.value = UiState.Error(result.mensagem)
                }
                is Result.UnknownError -> {
                    _minhasListasState.value = UiState.Error(result.mensagem)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun buscarMinhasListas() {
        // A lógica agora é reativa, mas podemos forçar uma atualização se necessário,
        // reiniciando a observação. A maneira mais simples é ter um "trigger".
        // Por enquanto, a lógica reativa no init deve ser suficiente.
        // Se o pull-to-refresh for necessário, podemos implementar um Flow de trigger.
        observeAuthenticationAndFetchLists() // Re-aciona o fluxo
    }


    fun removerLista(listId: Int) {
        viewModelScope.launch {
            _listaRemovidaState.value = UiState.Loading
            val accessToken = userPreferencesRepository.accessTokenV4.first()
            if (accessToken.isNullOrEmpty()) {
                _listaRemovidaState.value = UiState.Error("Usuário não autenticado.")
                return@launch
            }

            when(val result = minhaListaRepository.removerListaV4(listId.toString(), "Bearer $accessToken")){
                is Result.Sucesso -> {
                     _listaRemovidaState.value = UiState.Success(Unit)
                     buscarMinhasListas() // Atualiza a lista após remover
                }
                is Result.HttpError -> {
                     _listaRemovidaState.value = UiState.Error("Erro ao remover: ${result.mensagem}")
                }
                else -> {
                     _listaRemovidaState.value = UiState.Error("Erro desconhecido ao remover lista.")
                }
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