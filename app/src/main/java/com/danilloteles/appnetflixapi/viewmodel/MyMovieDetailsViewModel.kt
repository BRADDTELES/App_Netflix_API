package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.model.AddRemoveListItemRequest
import com.danilloteles.appnetflixapi.model.CreateListRequest
import com.danilloteles.appnetflixapi.model.FilmeDetalhes
import com.danilloteles.appnetflixapi.model.Filme
import com.danilloteles.appnetflixapi.model.TmdbList
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.utils.MyListPreferencesRepository
import com.danilloteles.appnetflixapi.utils.UiState
import com.danilloteles.appnetflixapi.utils.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyMovieDetailsViewModel(
    private val movieId: Int,
    private val filmeAPI: FilmeAPI,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val myListPreferencesRepository: MyListPreferencesRepository
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
            userPreferencesRepository.primaryListId.first()?.let {
                primaryListId = it
            }
            loadUserLists() // Carrega as listas do usuário
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
        viewModelScope.launch {
            userPreferencesRepository.sessionId.first()?.let { sessionId ->
                val targetListId = listIdToCheck ?: primaryListId ?: userPreferencesRepository.primaryListId.first()
                targetListId?.let { id ->
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


    fun addOrRemoveMovie(targetListId: String? = null) {
        viewModelScope.launch {
            _myListActionUiState.value = UiState.Loading
            userPreferencesRepository.sessionId.first()?.let { sessionId ->
                val listToModifyId = targetListId ?: userPreferencesRepository.primaryListId.first()
                if (listToModifyId == null) {
                    _myListActionUiState.value = UiState.Error("Erro: ID da lista não disponível. Tente novamente.")
                    Log.e("MyMovieDetailsViewModel", "List ID não disponível para adicionar/remover filme.")
                    return@launch
                }

                val request = AddRemoveListItemRequest(media_id = movieId)
                try {
                    val response = if (_isInMyList.value) {
                        filmeAPI.removeMovieFromList(listToModifyId, sessionId, request)
                    } else {
                        filmeAPI.addMovieToList(listToModifyId, sessionId, request)
                    }

                    if (response.isSuccessful && response.body()?.status_code == 1 || response.body()?.status_code == 12 || response.body()?.status_code == 13) {
                        _isInMyList.value = !_isInMyList.value
                        _myListActionUiState.value = UiState.Success(Unit)

                        // Atualizar o cache do DataStore (ainda genérico para a "Minha Lista" principal)
                        val currentCachedList = myListPreferencesRepository.myMovieList.first().toMutableList()
                        if (_isInMyList.value) { // Se o filme foi adicionado (agora _isInMyList é true)
                            (_uiState.value as? UiState.Success)?.data?.let { filmeDetalhes ->
                                val filmeToAdd = Filme(
                                    adult = filmeDetalhes.adult,
                                    backdrop_path = filmeDetalhes.backdrop_path ?: "",
                                    genre_ids = filmeDetalhes.genres.map { it.id },
                                    id = filmeDetalhes.id,
                                    original_language = filmeDetalhes.original_language,
                                    original_title = filmeDetalhes.original_title,
                                    overview = filmeDetalhes.overview,
                                    popularity = filmeDetalhes.popularity,
                                    poster_path = filmeDetalhes.poster_path ?: "",
                                    release_date = filmeDetalhes.release_date,
                                    title = filmeDetalhes.title,
                                    video = filmeDetalhes.video,
                                    vote_average = filmeDetalhes.vote_average,
                                    vote_count = filmeDetalhes.vote_count
                                )
                                if (!currentCachedList.any { it.id == filmeToAdd.id }) {
                                    currentCachedList.add(filmeToAdd)
                                }
                            }
                        } else { // Se o filme foi removido (agora _isInMyList é false)
                            currentCachedList.removeAll { it.id == movieId }
                        }
                        myListPreferencesRepository.saveMyMovieList(currentCachedList)
                        Log.d("MyMovieDetailsVM", "Cache da lista atualizado. Filme ID: $movieId, Adicionado: ${_isInMyList.value}")
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
                _myListActionUiState.value = UiState.Error("Erro: Usuário não autenticado. Faça login para gerenciar sua lista.")
                Log.e("MyMovieDetailsViewModel", "Usuário não autenticado para adicionar/remover filme.")
            }
        }
    }

    fun resetMyListActionUiState() {
        _myListActionUiState.value = UiState.Idle
    }


    class Factory(
        private val movieId: Int,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val myListPreferencesRepository: MyListPreferencesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyMovieDetailsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MyMovieDetailsViewModel(
                    movieId,
                    RetrofitHelper.filmeAPI,
                    userPreferencesRepository,
                    myListPreferencesRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

