package com.danilloteles.appnetflixapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.v4.response.CreateListV4Response
import com.danilloteles.appnetflixapi.model.v4.response.EditListV4Response
import com.danilloteles.appnetflixapi.repository.v4.RepositoryV4
import com.danilloteles.appnetflixapi.utils.events.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FormularioViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val repositoryV4: RepositoryV4
) : ViewModel() {

    private val _createUiState = MutableStateFlow<UiState<CreateListV4Response>>(UiState.Idle)
    val createUiState: StateFlow<UiState<CreateListV4Response>> = _createUiState

    private val _editUiState = MutableStateFlow<UiState<EditListV4Response>>(UiState.Idle)
    val editUiState: StateFlow<UiState<EditListV4Response>> = _editUiState

    private val _uiState = MutableStateFlow<UiState<Any>>(UiState.Idle)
    val uiState: StateFlow<UiState<Any>> = _uiState

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
                    userPreferencesRepository.savePrimaryListId(result.data.id.toString())
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

    fun editList(listId: String, name: String, description: String?) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val accessToken = userPreferencesRepository.accessTokenV4.first()
            if (accessToken == null) {
                _uiState.value = UiState.Error("Usuário não autenticado.")
                return@launch
            }

            when (val result = repositoryV4.editList(accessToken, listId, name, description)) {
                is Result.Sucesso -> {
                    // Verifica se a resposta indica sucesso
                    if (result.data.success) {
                        _uiState.value = UiState.Success(result.data)
                    } else {
                        _uiState.value = UiState.Error("Falha ao editar lista: ${result.data.status_message}")
                    }
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
