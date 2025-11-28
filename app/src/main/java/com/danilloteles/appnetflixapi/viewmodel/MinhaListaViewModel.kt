package com.danilloteles.appnetflixapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.v4.response.TmdbListV4
import com.danilloteles.appnetflixapi.repository.v4.RepositoryV4
import com.danilloteles.appnetflixapi.utils.events.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MinhaListaViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val repositoryV4: RepositoryV4
) : ViewModel() {

    private val _minhasListasState = MutableStateFlow<UiState<List<TmdbListV4>>>(UiState.Idle)
    val minhasListasState: StateFlow<UiState<List<TmdbListV4>>> = _minhasListasState

    private val _listaRemovidaState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val listaRemovidaState: StateFlow<UiState<Unit>> = _listaRemovidaState

    init {
        observeAuthenticationEBusqueListas()
    }

    fun observeAuthenticationEBusqueListas() {
        viewModelScope.launch {
            _minhasListasState.value = UiState.Loading
            val accessToken = userPreferencesRepository.accessTokenV4.first()
            val accountObjectId = userPreferencesRepository.accountId.first()
            if (accessToken.isNullOrEmpty() || accountObjectId.isNullOrEmpty()) {
                _minhasListasState.value = UiState.Error("Usuário não autenticado e não encontrado")
                return@launch
            }

            when(val result = repositoryV4.getAccountLits(accessToken, accountObjectId)){
                is Result.Sucesso -> {
                    val response = result.data.body()
                    if (response?.results != null) {
                        _minhasListasState.value = UiState.Success(response.results)
                    } else {
                        emptyList<TmdbListV4>()
                    }
                }
                is Result.HttpError -> {
                    _minhasListasState.value = UiState.Error("Erro HTTP: ${result.mensagem} - Code: ${result.code}")
                }
                is Result.NetworkError -> {
                    _minhasListasState.value = UiState.Error("Erro de Network: ${result.mensagem}")
                }
                is Result.UnknownError -> {
                    _minhasListasState.value = UiState.Error("Erro desconhecido: ${result.mensagem}")
                }
            }
        }
    }

    fun buscarMinhasListas() {
        // A lógica agora é reativa, mas podemos forçar uma atualização se necessário,
        // reiniciando a observação. A maneira mais simples é ter um "trigger".
        // Por enquanto, a lógica reativa no init deve ser suficiente.
        // Se o pull-to-refresh for necessário, podemos implementar um Flow de trigger.
        observeAuthenticationEBusqueListas() // Re-aciona o fluxo
    }


    fun removerLista(listId: Int) {
        viewModelScope.launch {
            _listaRemovidaState.value = UiState.Loading
            val accessToken = userPreferencesRepository.accessTokenV4.first()
            if (accessToken.isNullOrEmpty()) {
                _listaRemovidaState.value = UiState.Error("Usuário não autenticado.")
                return@launch
            }

            when(val result = repositoryV4.removeList(accessToken, listId.toString())){
                is Result.Sucesso -> {
                     _listaRemovidaState.value = UiState.Success(Unit)
                     buscarMinhasListas() // Atualiza a lista após remover
                }
                is Result.HttpError -> {
                     _listaRemovidaState.value = UiState.Error("Erro HTTP ao remover lista: ${result.mensagem}")
                }
                is Result.NetworkError -> {
                    _listaRemovidaState.value = UiState.Error("Erro de Network ao remover lista: ${result.mensagem}")
                }
                is Result.UnknownError -> {
                    _listaRemovidaState.value = UiState.Error("Erro desconhecido ao remover lista: ${result.mensagem}")
                }
            }
        }
    }

    fun resetRemoveState() {
        _listaRemovidaState.value = UiState.Idle
    }

    class MinhaListaViewModelFactory(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val repositoryV4: RepositoryV4
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MinhaListaViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MinhaListaViewModel(
                    userPreferencesRepository,
                    repositoryV4
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}