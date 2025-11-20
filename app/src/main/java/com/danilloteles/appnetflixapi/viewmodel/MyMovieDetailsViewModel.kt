package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.model.AddRemoveListItemRequest
import com.danilloteles.appnetflixapi.model.FilmeDetalhes
import com.danilloteles.appnetflixapi.model.TmdbList
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.datasource.MyListPreferencesRepository
import com.danilloteles.appnetflixapi.utils.events.UiState
import com.danilloteles.appnetflixapi.datasource.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyMovieDetailsViewModel(
    private val movieId: Int,
    private val filmeAPI: FilmeAPI,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val myListPreferencesRepository: MyListPreferencesRepository,
    private val listId: String? // listId passed from navigation
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<FilmeDetalhes>>(UiState.Loading)
    val uiState: StateFlow<UiState<FilmeDetalhes>> = _uiState

    private val _isInMyList = MutableStateFlow(false)
    val isInMyList: StateFlow<Boolean> = _isInMyList

    private val _myListActionUiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val myListActionUiState: StateFlow<UiState<Unit>> = _myListActionUiState

    private val _userListsUiState = MutableStateFlow<UiState<List<TmdbList>>>(UiState.Idle)
    val userListsUiState: StateFlow<UiState<List<TmdbList>>> = _userListsUiState

    private var primaryListId: String? = null
    private var accountId: Int? = null

    init {
        loadMovieDetails()
        viewModelScope.launch {
            userPreferencesRepository.accountId.first()?.toIntOrNull()?.let {
                accountId = it
            }
            // primaryListId from repo might be needed if not passed via navigation.
            // For now, it's safer to load it here as a fallback.
            primaryListId = userPreferencesRepository.primaryListId.first()

            loadUserLists() // Load all user lists for selection
            checkIfMovieInMyList() // Check if movie is in the *relevant* list
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
                Log.e("TAG-MyMovieDetailsViewModel", "Erro: ${e.message}", e)
            }
        }
    }

    private fun loadUserLists() {
        viewModelScope.launch {
            _userListsUiState.value = UiState.Loading
            userPreferencesRepository.sessionId.first()?.let { sessionId ->
                val currentAccountId = getOrCreateAccountId(sessionId)
                if (currentAccountId != null) {
                    try {
                        val response = filmeAPI.getAccountLists(currentAccountId, sessionId)
                        if (response.isSuccessful) {
                            response.body()?.let { accountListsResponse ->
                                _userListsUiState.value = UiState.Success(accountListsResponse.results)
                                // Optionally set primaryListId if needed here, but it's loaded in init
                            } ?: run {
                                _userListsUiState.value = UiState.Error("Não foi possível carregar as listas do usuário.")
                            }
                        } else {
                            _userListsUiState.value = UiState.Error("Erro ao carregar listas do usuário: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        _userListsUiState.value = UiState.Error("Erro de conexão ao carregar listas: ${e.message}")
                    }
                } else {
                    _userListsUiState.value = UiState.Error("Usuário não autenticado ou Account ID não disponível.")
                }
            } ?: run {
                _userListsUiState.value = UiState.Error("Usuário não autenticado. Faça login para ver suas listas.")
            }
        }
    }

    // Função auxiliar para obter ou criar o accountId
    private suspend fun getOrCreateAccountId(sessionId: String): Int? {
        // Tenta obter do DataStore
        accountId ?: userPreferencesRepository.accountId.first()?.toIntOrNull()?.let {
            accountId = it
            return it
        }

        // Se não estiver no DataStore, busca da API
        val accountDetailsResponse = filmeAPI.getAccountDetails(sessionId)
        if (accountDetailsResponse.isSuccessful) {
            accountDetailsResponse.body()?.let { details ->
                accountId = details.id
                userPreferencesRepository.saveAccountId(details.id.toString())
                return details.id
            }
        }
        return null
    }

    private fun checkIfMovieInMyList(listIdToCheck: String? = null) {
        Log.d("TAG-MyMovieDetailsViewModel", "checkIfMovieInMyList iniciado para movieId: $movieId")
        viewModelScope.launch {
            Log.d("TAG-MyMovieDetailsViewModel", "listIdToCheck (param): $listIdToCheck, ViewModel listId (constructor): $listId, ViewModel primaryListId: $primaryListId")
            userPreferencesRepository.sessionId.first()?.let { sessionId ->
                Log.d("TAG-MyMovieDetailsViewModel", "SessionId obtido: $sessionId")
                val repoPrimaryListId = userPreferencesRepository.primaryListId.first()
                Log.d("TAG-MyMovieDetailsViewModel", "Repo primaryListId: $repoPrimaryListId")

                val finalTargetListId = listIdToCheck ?: listId ?: primaryListId ?: repoPrimaryListId
                Log.d("TAG-MyMovieDetailsViewModel", "TargetListId final para verificação: $finalTargetListId")

                finalTargetListId?.let { id ->
                    try {
                        Log.d("TAG-MyMovieDetailsViewModel", "Chamando API para getListDetails para listId: $id")
                        val response = filmeAPI.getListDetails(id, sessionId)
                        if (response.isSuccessful) {
                            val listDetails = response.body()
                            val containsMovie = listDetails?.items?.any { it.id == movieId } ?: false
                            _isInMyList.value = containsMovie
                            Log.d("TAG-MyMovieDetailsViewModel", "API getListDetails para listId $id retornou ${listDetails?.items?.size ?: 0} itens. Contém movie $movieId: $containsMovie. _isInMyList atualizado para: ${_isInMyList.value}")
                        } else {
                            Log.e("TAG-MyMovieDetailsViewModel", "Erro ao verificar filme na lista: ${response.code()}")
                            _isInMyList.value = false
                        }
                    } catch (e: Exception) {
                        Log.e("TAG-MyMovieDetailsViewModel", "Erro de conexão ao verificar filme na lista: ${e.message}", e)
                        _isInMyList.value = false
                    }
                } ?: run {
                    Log.d("TAG-MyMovieDetailsViewModel", "Nenhum targetListId disponível para verificar filme na lista.")
                    _isInMyList.value = false // Não há list_id primário definido
                }
            } ?: run {
                Log.d("TAG-MyMovieDetailsViewModel", "Usuário não autenticado. Não foi possível verificar filme na lista.")
                _isInMyList.value = false // Usuário não autenticado
            }
        }
    }

    fun addOrRemoveMovie(targetListIdForAction: String? = null) {
        Log.d("TAG-MyMovieDetailsViewModel", "addOrRemoveMovie iniciado. MovieId: $movieId, targetListIdForAction: $targetListIdForAction")
        viewModelScope.launch {
            _myListActionUiState.value = UiState.Loading
            userPreferencesRepository.sessionId.first()?.let { sessionId ->
                Log.d("TAG-MyMovieDetailsViewModel", "SessionId obtido: $sessionId")
                
                val finalListToModifyId = targetListIdForAction ?: listId ?: primaryListId ?: userPreferencesRepository.primaryListId.first()
                
                if (finalListToModifyId == null) {
                    _myListActionUiState.value = UiState.Error("Erro: ID da lista não disponível. Tente novamente.")
                    Log.e("TAG-MyMovieDetailsViewModel", "List ID não disponível para adicionar/remover filme.")
                    return@launch
                }

                val request = AddRemoveListItemRequest(media_id = movieId)
                Log.d("TAG-MyMovieDetailsViewModel", "Requisição API: $request para listId: $finalListToModifyId")
                try {
                    val response = if (_isInMyList.value) {
                        Log.d("TAG-MyMovieDetailsViewModel", "Tentando remover filme (ID: $movieId) da lista (ID: $finalListToModifyId).")
                        filmeAPI.removeMovieFromList(finalListToModifyId, sessionId, request)
                    } else {
                        Log.d("TAG-MyMovieDetailsViewModel", "Tentando adicionar filme (ID: $movieId) à lista (ID: $finalListToModifyId).")
                        filmeAPI.addMovieToList(finalListToModifyId, sessionId, request)
                    }

                    Log.d("TAG-MyMovieDetailsViewModel", "Resposta da API - isSuccessful: ${response.isSuccessful}, Code: ${response.code()}, Body: ${response.body()}")

                    if (response.isSuccessful && (response.body()?.status_code == 1 || response.body()?.status_code == 12 || response.body()?.status_code == 13)) {
                        _isInMyList.value = !_isInMyList.value
                        _myListActionUiState.value = UiState.Success(Unit)

                        // TODO: Atualizar o cache do DataStore de forma mais granular (por listId)
                        // Por enquanto, o cache é genérico, então forçamos um refresh na MyListScreen ao voltar
                        // Forçamos o refresh chamando checkIfMovieInMyList novamente para a lista original
                        // Assim, MyListScreen deve ser atualizada.
                        checkIfMovieInMyList(finalListToModifyId) // Re-verifica o estado para a lista modificada
                        Log.d("TAG-MyMovieDetailsViewModel", "Cache da lista atualizado. Filme ID: $movieId, _isInMyList: ${_isInMyList.value}")
                    } else {
                        val errorMessage = response.body()?.status_message ?: "Falha desconhecida."
                        _myListActionUiState.value = UiState.Error(errorMessage)
                        Log.e("TAG-MyMovieDetailsViewModel", "Falha ao adicionar/remover filme: ${response.code()} - $errorMessage")
                    }
                } catch (e: Exception) {
                    _myListActionUiState.value = UiState.Error("Erro de conexão ao adicionar/remover filme.")
                    Log.e("TAG-MyMovieDetailsViewModel", "Erro de conexão ao adicionar/remover filme: ${e.message}", e)
                }
            } ?: run {
                _myListActionUiState.value = UiState.Error("Erro: Usuário não autenticado. Faça login para gerenciar sua lista.")
                Log.e("TAG-MyMovieDetailsViewModel", "Usuário não autenticado para adicionar/remover filme.")
            }
        }
    }

    fun resetMyListActionUiState() {
        _myListActionUiState.value = UiState.Idle
    }

    class Factory(
        private val movieId: Int,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val myListPreferencesRepository: MyListPreferencesRepository,
        private val listId: String? // listId from navigation
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyMovieDetailsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MyMovieDetailsViewModel(
                    movieId,
                    RetrofitHelper.filmeAPI,
                    userPreferencesRepository,
                    myListPreferencesRepository,
                    listId
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}