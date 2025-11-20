package com.danilloteles.appnetflixapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.model.CreateListRequest
import com.danilloteles.appnetflixapi.model.CreateListResponse
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.utils.UiState
import com.danilloteles.appnetflixapi.utils.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ListFormViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val filmeAPI: FilmeAPI
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<CreateListResponse>>(UiState.Idle)
    val uiState: StateFlow<UiState<CreateListResponse>> = _uiState

    fun createList(name: String, description: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val sessionId = userPreferencesRepository.sessionId.first()
            if (sessionId == null) {
                _uiState.value = UiState.Error("Usuário não autenticado.")
                return@launch
            }

            try {
                val request = CreateListRequest(name = name, description = description, iso_639_1 = "pt-BR")
                val response = filmeAPI.createList(sessionId, request)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = UiState.Success(response.body()!!)
                } else {
                    _uiState.value = UiState.Error("Falha ao criar a lista: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro de conexão: ${e.message}")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    class Factory(
        private val userPreferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ListFormViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ListFormViewModel(
                    userPreferencesRepository,
                    RetrofitHelper.filmeAPI
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
