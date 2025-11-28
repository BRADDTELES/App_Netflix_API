package com.danilloteles.appnetflixapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.utils.events.UiState
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.v4.response.CreateListResponse
import com.danilloteles.appnetflixapi.repository.MinhaListaRepository
import com.danilloteles.appnetflixapi.repository.v4.RepositoryV4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FormularioViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val repositoryV4: RepositoryV4
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<CreateListResponse>>(UiState.Idle)
    val uiState: StateFlow<UiState<CreateListResponse>> = _uiState

    fun createList(name: String, description: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val accessToken = userPreferencesRepository.accessTokenV4.first()
            if (accessToken == null) {
                _uiState.value = UiState.Error("Usuário não autenticado.")
                return@launch
            }

            when (val result = repositoryV4.createList(accessToken, name, description)){
                is Result.Sucesso -> {
                    _uiState.value = UiState.Success(result.data)
                }
                is Result.HttpError -> {
                    _uiState.value = UiState.Error("Erro HTTP: ${result.mensagem} - Code: ${result.code}")
                }
                is Result.NetworkError -> {
                    _uiState.value = UiState.Error("Erro de Network: ${result.mensagem}")
                }
                is Result.UnknownError -> {
                    _uiState.value = UiState.Error("Erro desconhecido: ${result.mensagem}")
                }
            }
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    class Factory(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val repositoryV4: RepositoryV4
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FormularioViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FormularioViewModel(
                    userPreferencesRepository,
                    repositoryV4
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
