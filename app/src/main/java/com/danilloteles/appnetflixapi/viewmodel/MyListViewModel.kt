package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.model.CreateListRequest
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

class MyListViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val myListPreferencesRepository: MyListPreferencesRepository
) : ViewModel() {

    private val filmeAPI: FilmeAPI = RetrofitHelper.filmeAPI

    private val _uiState = MutableStateFlow<UiState<List<Filme>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<Filme>>> = _uiState

    private val _userListsUiState = MutableStateFlow<UiState<List<TmdbList>>>(UiState.Idle)
    val userListsUiState: StateFlow<UiState<List<TmdbList>>> = _userListsUiState

    private var originalMovies: List<Filme> = emptyList()
    private var primaryListId: String? = null
    private var accountId: Int? = null

    init {
        loadUserLists()
    }

    fun applyFilter(filterIndex: Int) {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            val sortedList = when (filterIndex) {
                // Padrão (ordem da API)
                0 -> originalMovies
                // Melhores Avaliados
                1 -> originalMovies.sortedByDescending { it.vote_average }
                // Ordem Alfabética (A-Z)
                2 -> originalMovies.sortedBy { it.title }
                else -> originalMovies
            }
            _uiState.value = UiState.Success(sortedList)
        }
    }

    fun loadUserLists() {
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

    // loadMyListMovies agora aceita um listId opcional
    fun loadMyListMovies(selectedListId: String? = null) {
        Log.d("TAG-MyListViewModel", "loadMyListMovies iniciado. selectedListId: $selectedListId")
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val targetListId: String? = selectedListId ?: userPreferencesRepository.primaryListId.first()
            Log.d("TAG-MyListViewModel", "targetListId determinado como: $targetListId")

            if (targetListId == null) {
                _uiState.value = UiState.Error("Nenhuma lista selecionada ou lista principal não definida.")
                Log.e("TAG-MyListViewModel", "Nenhuma lista selecionada ou lista principal não definida.")
                return@launch
            }

            // 1. Tenta carregar do cache primeiro (para a lista específica)
            // TODO: Precisa de uma forma de armazenar cache por listId
            // Por enquanto, o cache é genérico, então só carrega se for a "Minha Lista" primária,
            // ou se a lista selecionada for a mesma que foi cached
            val cachedMovies = myListPreferencesRepository.myMovieList.first()
            if (cachedMovies.isNotEmpty()) { // Simplesmente verifica se há algum cache genérico
                originalMovies = cachedMovies
                _uiState.value = UiState.Success(cachedMovies)
                Log.d("TAG-MyListViewModel", "Carregando lista do cache. ${cachedMovies.size} filmes encontrados.")
            } else {
                Log.d("TAG-MyListViewModel", "Cache da lista vazio. Carregando da API.")
            }

            // 2. Sempre tenta buscar a lista atualizada da API em segundo plano
            userPreferencesRepository.sessionId.first()?.let { sessionId ->
                Log.d("TAG-MyListViewModel", "SessionId obtido: $sessionId")
                if (targetListId != null) {
                    try {
                        Log.d("TAG-MyListViewModel", "Chamando API para getListDetails para listId: $targetListId")
                        val response = filmeAPI.getListDetails(targetListId, sessionId)
                        if (response.isSuccessful) {
                            response.body()?.let { listDetails ->
                                Log.d("TAG-MyListViewModel", "API retornou ${listDetails.items.size} itens para listId: $targetListId")
                                if (listDetails.items.isNotEmpty()) {
                                    val movies = listDetails.items.map { item ->
                                        Filme(
                                            adult = item.adult,
                                            backdrop_path = item.backdrop_path ?: "",
                                            genre_ids = item.genre_ids,
                                            id = item.id,
                                            original_language = item.original_language,
                                            original_title = item.original_title,
                                            overview = item.overview,
                                            popularity = item.popularity,
                                            poster_path = item.poster_path ?: "",
                                            release_date = item.release_date,
                                            title = item.title,
                                            video = item.video,
                                            vote_average = item.vote_average,
                                            vote_count = item.vote_count
                                        )
                                    }
                                    originalMovies = movies
                                    _uiState.value = UiState.Success(movies)
                                    // Salvar no cache, mas precisaríamos de um cache por listId
                                    // Por enquanto, sobrescreve o cache genérico
                                    myListPreferencesRepository.saveMyMovieList(movies)
                                    Log.d("TAG-MyListViewModel", "Lista atualizada da API e salva no cache. ${movies.size} filmes.")
                                } else {
                                    originalMovies = emptyList()
                                    _uiState.value = UiState.Success(emptyList())
                                    myListPreferencesRepository.clearMyMovieList() // Limpa o cache se a lista estiver vazia
                                    Log.d("TAG-MyListViewModel", "Lista da API vazia para listId: $targetListId. Cache limpo.")
                                }
                            } ?: run {
                                _uiState.value = UiState.Error("Não foi possível carregar os detalhes da lista.")
                                Log.e("TAG-MyListViewModel", "Corpo da resposta da API nulo ao carregar lista para listId: $targetListId.")
                            }
                        } else {
                            if (_uiState.value !is UiState.Success) {
                                _uiState.value = UiState.Error("Erro ao carregar lista da API: ${response.code()}")
                            }
                            Log.e("TAG-MyListViewModel", "Erro HTTP ao carregar lista para listId: $targetListId: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        if (_uiState.value !is UiState.Success) {
                            _uiState.value = UiState.Error("Erro de conexão ao carregar lista da API: ${e.message}")
                        }
                        Log.e("TAG-MyListViewModel", "Erro de conexão ao carregar lista para listId: $targetListId: ${e.message}", e)
                    }
                }
            } ?: run {
                if (_uiState.value !is UiState.Success) {
                    _uiState.value = UiState.Error("Usuário não autenticado. Faça login para ver sua lista.")
                }
                Log.e("TAG-MyListViewModel", "Sessão ID nula. Usuário não autenticado. Não foi possível carregar filmes.")
            }
        }
    }

    class MyListViewModelFactory(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val myListPreferencesRepository: MyListPreferencesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MyListViewModel(userPreferencesRepository, myListPreferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}