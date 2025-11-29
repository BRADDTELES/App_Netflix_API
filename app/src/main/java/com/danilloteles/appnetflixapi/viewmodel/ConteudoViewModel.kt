package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.v4.response.ItemDetailsV4Response
import com.danilloteles.appnetflixapi.model.v4.response.ListDetailsV4Response
import com.danilloteles.appnetflixapi.repository.v4.RepositoryV4
import com.danilloteles.appnetflixapi.utils.events.SortOrder
import com.danilloteles.appnetflixapi.utils.events.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ConteudoViewModel(
    private val listId: String,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val repositoryV4: RepositoryV4
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ListDetailsV4Response>>(UiState.Idle)
    val uiState: StateFlow<UiState<ListDetailsV4Response>> = _uiState.asStateFlow()

    private val _sortedItems = MutableStateFlow<List<ItemDetailsV4Response>>(emptyList())
    val sortedItems: StateFlow<List<ItemDetailsV4Response>> = _sortedItems.asStateFlow()

    private var currentSortOrder = SortOrder.DEFAULT

    init {
        buscarDetalhesDaLista()
    }

    fun definirOrdemDeClassificacao(sortOrder: SortOrder) {
        currentSortOrder = sortOrder
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            _sortedItems.value = sortList(currentState.data.results, sortOrder)
        }
    }

    private fun sortList(items: List<ItemDetailsV4Response>, sortOrder: SortOrder): List<ItemDetailsV4Response> {
        return when (sortOrder) {
            SortOrder.TITLE_ASC -> items.sortedBy { it.title ?: it.name }
            SortOrder.DEFAULT -> items // A ordem padrão da API
        }
    }

    fun buscarDetalhesDaLista() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val accessToken = userPreferencesRepository.accessTokenV4.first()
                if (accessToken == null) {
                    _uiState.value = UiState.Error("Token de acesso V4 não encontrado. Faça o login novamente.")
                    return@launch
                }

                when (val result = repositoryV4.getListDetails(accessToken, listId)) {
                    is Result.Sucesso -> {
                        _uiState.value = UiState.Success(result.data)
                        _sortedItems.value = sortList(result.data.results, currentSortOrder)
                        Log.d("TAG-ConteudoViewModel","${result.data.results.size} itens carregados com sucesso")
                    }
                    is Result.HttpError -> {
                        _uiState.value = UiState.Error("Erro HTTP: ${result.mensagem} - Code: ${result.code}")
                    }
                    is Result.NetworkError -> {
                        _uiState.value = UiState.Error("Erro de Network")
                        Log.e("TAG-ConteudoViewModel","Erro de Network: ${result.mensagem}")
                    }
                    is Result.UnknownError -> {
                        _uiState.value = UiState.Error("Erro desconhecido")
                        Log.e("TAG-ConteudoViewModel","Erro desconhecido: ${result.mensagem}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro ao buscar detalhes da lista: ${e.message}")
                Log.e("TAG-ConteudoViewModel", "Erro ao buscar detalhes da lista: ${e.message}", e)
            }
        }
    }

    class ConteudoViewModelFactory(
        private val listId: String,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val repositoryV4: RepositoryV4
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ConteudoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ConteudoViewModel(
                    listId,
                    userPreferencesRepository,
                    repositoryV4
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}