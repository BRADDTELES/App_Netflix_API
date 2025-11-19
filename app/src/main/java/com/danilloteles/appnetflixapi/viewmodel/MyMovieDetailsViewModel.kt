package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.model.AddRemoveListItemRequest
import com.danilloteles.appnetflixapi.model.CreateListRequest
import com.danilloteles.appnetflixapi.model.FilmeDetalhes
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.utils.UiState
import com.danilloteles.appnetflixapi.utils.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyMovieDetailsViewModel(
    private val movieId: Int,
    private val filmeAPI: FilmeAPI,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<FilmeDetalhes>>(UiState.Loading)
    val uiState: StateFlow<UiState<FilmeDetalhes>> = _uiState

    private val _isInMyList = MutableStateFlow(false)
    val isInMyList: StateFlow<Boolean> = _isInMyList

    private val _myListActionUiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val myListActionUiState: StateFlow<UiState<Unit>> = _myListActionUiState

    private var primaryListId: String? = null
    private var accountId: Int? = null

    init {
        loadMovieDetails()
        viewModelScope.launch {
            userPreferencesRepository.accountId.first()?.toIntOrNull()?.let {
                accountId = it
            }
            userPreferencesRepository.primaryListId.first()?.let {
                primaryListId = it
            }
            checkIfMovieInMyList()
        }
    }

    private fun loadMovieDetails() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = filmeAPI.recuperarDetalhesFilme(movieId)
                if (response.isSuccessful) {
                    response.body()?.let { details ->
                        _uiState.value = UiState.Success(details)
                    } ?: run {
                        _uiState.value = UiState.Error("Detalhes do filme não encontrados.")
                    }
                } else {
                    _uiState.value =
                        UiState.Error("Erro ao carregar detalhes do filme: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro de conexão ao carregar detalhes: ${e.message}")
                Log.e("MyMovieDetailsViewModel", "Erro: ${e.message}", e)
            }
        }
    }

    private suspend fun getOrCreatePrimaryListId(sessionId: String): String? {
        // Tenta obter o primaryListId do DataStore
        userPreferencesRepository.primaryListId.first()?.let {
            primaryListId = it
            return it
        }

        // Se não estiver no DataStore, tenta buscar as listas do usuário
        val accId = accountId ?: userPreferencesRepository.accountId.first()?.toIntOrNull()
        if (accId == null) {
            // Se o accountId não estiver disponível, tenta buscar os detalhes da conta
            val accountDetailsResponse = filmeAPI.getAccountDetails(sessionId)
            if (accountDetailsResponse.isSuccessful) {
                accountDetailsResponse.body()?.let { details ->
                    accountId = details.id
                    userPreferencesRepository.saveAccountId(details.id.toString())
                }
            }
        }

        accountId?.let { id ->
            try {
                val listsResponse = filmeAPI.getAccountLists(id, sessionId)
                if (listsResponse.isSuccessful) {
                    listsResponse.body()?.results?.firstOrNull { it.name == "Minha Lista" }?.let { list ->
                        primaryListId = list.id.toString()
                        userPreferencesRepository.savePrimaryListId(list.id.toString())
                        return list.id.toString()
                    }
                }
            } catch (e: Exception) {
                Log.e("MyMovieDetailsViewModel", "Erro ao buscar listas da conta: ${e.message}", e)
            }
        }

        // Se a lista não foi encontrada, cria uma nova
        return try {
            val createListRequest = CreateListRequest(
                name = "Minha Lista",
                description = "Minha lista de filmes e séries favoritas",
                iso_639_1 = "pt-BR"
            )
            val createResponse = filmeAPI.createList(sessionId, createListRequest)
            if (createResponse.isSuccessful) {
                createResponse.body()?.let { response ->
                    if (response.success) {
                        primaryListId = response.list_id.toString()
                        userPreferencesRepository.savePrimaryListId(response.list_id.toString())
                        response.list_id.toString()
                    } else {
                        Log.e("MyMovieDetailsViewModel", "Falha ao criar lista: ${response.status_message}")
                        null
                    }
                }
            } else {
                Log.e("MyMovieDetailsViewModel", "Erro HTTP ao criar lista: ${createResponse.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MyMovieDetailsViewModel", "Erro de conexão ao criar lista: ${e.message}", e)
            null
        }
    }


    private fun checkIfMovieInMyList() {
        viewModelScope.launch {
            userPreferencesRepository.sessionId.first()?.let { sessionId ->
                val listId = getOrCreatePrimaryListId(sessionId)
                listId?.let { id ->
                    try {
                        val response = filmeAPI.getListDetails(id, sessionId)
                        if (response.isSuccessful) {
                            val listDetails = response.body()
                            _isInMyList.value = listDetails?.items?.any { it.id == movieId } ?: false
                        } else {
                            Log.e("MyMovieDetailsViewModel", "Erro ao verificar filme na lista: ${response.code()}")
                            _isInMyList.value = false
                        }
                    } catch (e: Exception) {
                        Log.e("MyMovieDetailsViewModel", "Erro de conexão ao verificar filme na lista: ${e.message}", e)
                        _isInMyList.value = false
                    }
                } ?: run {
                    _isInMyList.value = false // Não há list_id primário definido
                }
            } ?: run {
                _isInMyList.value = false // Usuário não autenticado
            }
        }
    }


    fun addOrRemoveMovie() {
        viewModelScope.launch {
            _myListActionUiState.value = UiState.Loading
            userPreferencesRepository.sessionId.first()?.let { sessionId ->
                val listId = getOrCreatePrimaryListId(sessionId)
                listId?.let { id ->
                    val request = AddRemoveListItemRequest(media_id = movieId)
                    try {
                        val response = if (_isInMyList.value) {
                            filmeAPI.removeMovieFromList(id, sessionId, request)
                        } else {
                            filmeAPI.addMovieToList(id, sessionId, request)
                        }

                        if (response.isSuccessful && response.body()?.status_code == 1 || response.body()?.status_code == 12 || response.body()?.status_code == 13) {
                            _isInMyList.value = !_isInMyList.value
                            _myListActionUiState.value = UiState.Success(Unit)
                        } else {
                            val errorMessage = response.body()?.status_message ?: "Falha desconhecida."
                            _myListActionUiState.value = UiState.Error(errorMessage)
                            Log.e("MyMovieDetailsViewModel", "Falha ao adicionar/remover filme: ${response.code()} - $errorMessage")
                        }
                    } catch (e: Exception) {
                        _myListActionUiState.value = UiState.Error("Erro de conexão ao adicionar/remover filme.")
                        Log.e("MyMovieDetailsViewModel", "Erro de conexão ao adicionar/remover filme: ${e.message}", e)
                    }
                } ?: run {
                    _myListActionUiState.value = UiState.Error("Erro: ID da lista não disponível. Tente novamente.")
                    Log.e("MyMovieDetailsViewModel", "List ID não disponível para adicionar/remover filme.")
                }
            } ?: run {
                _myListActionUiState.value = UiState.Error("Erro: Usuário não autenticado. Faça login para gerenciar sua lista.")
                Log.e("MyMovieDetailsViewModel", "Usuário não autenticado para adicionar/remover filme.")
            }
        }
    }


    class Factory(
        private val movieId: Int,
        private val userPreferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyMovieDetailsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MyMovieDetailsViewModel(
                    movieId,
                    RetrofitHelper.filmeAPI,
                    userPreferencesRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

