package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.model.Filme
import com.danilloteles.appnetflixapi.model.TmdbList
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.datasource.MyListPreferencesRepository
import com.danilloteles.appnetflixapi.utils.events.UiState
import com.danilloteles.appnetflixapi.datasource.UserPreferencesRepository
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
        viewModelScope.launch {
            primaryListId = userPreferencesRepository.primaryListId.first()
        }
    }

    fun applyFilter(filterIndex: Int, currentSelectedListId: String? = null) {
        viewModelScope.launch {
            when (filterIndex) {
                0 -> { // Minha Lista
                    Log.d("TAG-MyListViewModel", "Filtro 'Minha Lista' selecionado. currentSelectedListId: $currentSelectedListId")
                    loadMyListMovies(currentSelectedListId)
                }
                1 -> { // Populares
                    Log.d("TAG-MyListViewModel", "Filtro 'Populares' selecionado.")
                    loadPopularMovies() // Chama a função para carregar filmes populares
                }
                2 -> { // Melhor Avaliados
                    Log.d("TAG-MyListViewModel", "Filtro 'Melhor Avaliados' selecionado.")
                    loadTopRatedMovies() // Chama a função para carregar filmes melhor avaliados
                }
                3 -> { // A-Z (Ordenação da lista atual)
                    Log.d("TAG-MyListViewModel", "Filtro 'A-Z' selecionado. Ordenando lista atual.")
                    val currentState = _uiState.value
                    if (currentState is UiState.Success) {
                        val sortedList = originalMovies.sortedBy { it.title }
                        _uiState.value = UiState.Success(sortedList)
                    }
                }
            }
        }
    }

    fun loadUserLists() {
        Log.d("TAG-MyListViewModel", "loadUserLists iniciado.")
        viewModelScope.launch {
            _userListsUiState.value = UiState.Loading
            userPreferencesRepository.sessionId.first()?.let { sessionId ->
                Log.d("TAG-MyListViewModel", "SessionId obtido para loadUserLists: $sessionId")
                val currentAccountId = getOrCreateAccountId(sessionId)
                if (currentAccountId != null) {
                    Log.d("TAG-MyListViewModel", "AccountId obtido para loadUserLists: $currentAccountId")
                    try {
                        val response = filmeAPI.getAccountLists(currentAccountId, sessionId)
                        if (response.isSuccessful) {
                            response.body()?.let { accountListsResponse ->
                                Log.d("TAG-MyListViewModel", "API getAccountLists retornou ${accountListsResponse.results.size} listas.")
                                _userListsUiState.value = UiState.Success(accountListsResponse.results)
                                // Tenta identificar e salvar o primaryListId se for "Minha Lista"
                                val foundPrimaryList = accountListsResponse.results.firstOrNull { it.name == "Minha Lista" }
                                if (foundPrimaryList != null) {
                                    primaryListId = foundPrimaryList.id.toString()
                                    userPreferencesRepository.savePrimaryListId(foundPrimaryList.id.toString())
                                    Log.d("TAG-MyListViewModel", "PrimaryListId identificado e salvo: $primaryListId")
                                } else if (accountListsResponse.results.isNotEmpty()) {
                                    // Se "Minha Lista" não foi encontrada, usa a primeira lista disponível como padrão
                                    val firstList = accountListsResponse.results.first()
                                    primaryListId = firstList.id.toString()
                                    userPreferencesRepository.savePrimaryListId(firstList.id.toString())
                                    Log.d("TAG-MyListViewModel", "Nenhuma lista 'Minha Lista' encontrada, usando a primeira lista disponível como PrimaryListId: $primaryListId")
                                } else {
                                    Log.d("TAG-MyListViewModel", "Nenhuma lista 'Minha Lista' encontrada e nenhuma outra lista disponível.")
                                }
                            } ?: run {
                                _userListsUiState.value = UiState.Error("Não foi possível carregar as listas do usuário.")
                                Log.e("TAG-MyListViewModel", "Corpo da resposta da API nulo ao carregar listas do usuário.")
                            }
                        } else {
                            _userListsUiState.value = UiState.Error("Erro ao carregar listas do usuário: ${response.code()}")
                            Log.e("TAG-MyListViewModel", "Erro HTTP ao carregar listas do usuário: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        _userListsUiState.value = UiState.Error("Erro de conexão ao carregar listas: ${e.message}")
                        Log.e("TAG-MyListViewModel", "Erro de conexão ao carregar listas do usuário: ${e.message}", e)
                    }
                } else {
                    _userListsUiState.value = UiState.Error("Usuário não autenticado ou Account ID não disponível.")
                    Log.e("TAG-MyListViewModel", "Usuário não autenticado ou Account ID não disponível para loadUserLists.")
                }
            } ?: run {
                _userListsUiState.value = UiState.Error("Usuário não autenticado. Faça login para ver suas listas.")
                Log.e("TAG-MyListViewModel", "Sessão ID nula. Usuário não autenticado para loadUserLists.")
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
    fun loadNowPlayingMovies() {
        Log.d("TAG-MyListViewModel", "loadNowPlayingMovies iniciado.")
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = filmeAPI.recuperarFilmes() // Chama o endpoint now_playing
                if (response.isSuccessful) {
                    response.body()?.let { filmeResposta ->
                        if (filmeResposta.results.isNotEmpty()) {
                            originalMovies = filmeResposta.results
                            _uiState.value = UiState.Success(filmeResposta.results)
                            Log.d("TAG-MyListViewModel", "Filmes em cartaz carregados com sucesso. ${filmeResposta.results.size} filmes.")
                        } else {
                            _uiState.value = UiState.Error("Nenhum filme em cartaz encontrado.")
                            Log.d("TAG-MyListViewModel", "API retornou nenhum filme em cartaz.")
                        }
                    } ?: run {
                        _uiState.value = UiState.Error("Resposta vazia ao carregar filmes em cartaz.")
                        Log.e("TAG-MyListViewModel", "Corpo da resposta da API nulo ao carregar filmes em cartaz.")
                    }
                } else {
                    _uiState.value = UiState.Error("Erro HTTP ao carregar filmes em cartaz: ${response.code()}")
                    Log.e("TAG-MyListViewModel", "Erro HTTP ao carregar filmes em cartaz: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro de conexão ao carregar filmes em cartaz: ${e.message}")
                Log.e("TAG-MyListViewModel", "Erro de conexão ao carregar filmes em cartaz: ${e.message}", e)
            }
        }
    }

    fun loadPopularMovies() {
        Log.d("TAG-MyListViewModel", "loadPopularMovies iniciado.")
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = filmeAPI.recuperarFilmesPopulares()
                if (response.isSuccessful) {
                    response.body()?.let { filmeResposta ->
                        if (filmeResposta.results.isNotEmpty()) {
                            originalMovies = filmeResposta.results
                            _uiState.value = UiState.Success(filmeResposta.results)
                            Log.d("TAG-MyListViewModel", "Filmes populares carregados com sucesso. ${filmeResposta.results.size} filmes.")
                        } else {
                            _uiState.value = UiState.Error("Nenhum filme popular encontrado.")
                            Log.d("TAG-MyListViewModel", "API retornou nenhum filme popular.")
                        }
                    } ?: run {
                        _uiState.value = UiState.Error("Resposta vazia ao carregar filmes populares.")
                        Log.e("TAG-MyListViewModel", "Corpo da resposta da API nulo ao carregar filmes populares.")
                    }
                } else {
                    _uiState.value = UiState.Error("Erro HTTP ao carregar filmes populares: ${response.code()}")
                    Log.e("TAG-MyListViewModel", "Erro HTTP ao carregar filmes populares: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro de conexão ao carregar filmes populares: ${e.message}")
                Log.e("TAG-MyListViewModel", "Erro de conexão ao carregar filmes populares: ${e.message}", e)
            }
        }
    }

    fun loadTopRatedMovies() {
        Log.d("TAG-MyListViewModel", "loadTopRatedMovies iniciado.")
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = filmeAPI.recuperarFilmesMelhorAvaliados()
                if (response.isSuccessful) {
                    response.body()?.let { filmeResposta ->
                        if (filmeResposta.results.isNotEmpty()) {
                            originalMovies = filmeResposta.results
                            _uiState.value = UiState.Success(filmeResposta.results)
                            Log.d("TAG-MyListViewModel", "Filmes melhor avaliados carregados com sucesso. ${filmeResposta.results.size} filmes.")
                        } else {
                            _uiState.value = UiState.Error("Nenhum filme melhor avaliado encontrado.")
                            Log.d("TAG-MyListViewModel", "API retornou nenhum filme melhor avaliado.")
                        }
                    } ?: run {
                        _uiState.value = UiState.Error("Resposta vazia ao carregar filmes melhor avaliados.")
                        Log.e("TAG-MyListViewModel", "Corpo da resposta da API nulo ao carregar filmes melhor avaliados.")
                    }
                } else {
                    _uiState.value = UiState.Error("Erro HTTP ao carregar filmes melhor avaliados: ${response.code()}")
                    Log.e("TAG-MyListViewModel", "Erro HTTP ao carregar filmes melhor avaliados: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro de conexão ao carregar filmes melhor avaliados: ${e.message}")
                Log.e("TAG-MyListViewModel", "Erro de conexão ao carregar filmes melhor avaliados: ${e.message}", e)
            }
        }
    }

    // loadMyListMovies agora aceita um listId opcional e uma flag para forçar o carregamento de "now playing"
    fun loadMyListMovies(selectedListId: String? = null, forceLoadNowPlaying: Boolean = false) {
        Log.d("TAG-MyListViewModel", "loadMyListMovies iniciado. selectedListId: $selectedListId, forceLoadNowPlaying: $forceLoadNowPlaying")
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            // Se forçar "now playing", ou se não houver um listId válido e não autenticado, carrega now playing
            val sessionId = userPreferencesRepository.sessionId.first()
            val hasValidSession = !sessionId.isNullOrEmpty()

            val effectiveTargetListId: String? = if (hasValidSession) {
                selectedListId ?: primaryListId ?: userPreferencesRepository.primaryListId.first()
            } else {
                null
            }

            if (forceLoadNowPlaying || effectiveTargetListId == null) {
                Log.d("TAG-MyListViewModel", "Carregando filmes em cartaz devido a forceLoadNowPlaying ou effectiveTargetListId ser nulo.")
                loadNowPlayingMovies()
                return@launch
            }

            Log.d("TAG-MyListViewModel", "targetListId determinado como: $effectiveTargetListId")

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
            sessionId?.let { validSessionId ->
                Log.d("TAG-MyListViewModel", "SessionId obtido: $validSessionId")
                if (effectiveTargetListId != null) {
                    try {
                        Log.d("TAG-MyListViewModel", "Chamando API para getListDetails para listId: $effectiveTargetListId")
                        val response = filmeAPI.getListDetails(effectiveTargetListId, validSessionId)
                        if (response.isSuccessful) {
                            response.body()?.let { listDetails ->
                                Log.d("TAG-MyListViewModel", "API retornou ${listDetails.items.size} itens para listId: $effectiveTargetListId")
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
                                    Log.d("TAG-MyListViewModel", "Lista da API vazia para listId: $effectiveTargetListId. Cache limpo.")
                                }
                            } ?: run {
                                _uiState.value = UiState.Error("Não foi possível carregar os detalhes da lista.")
                                Log.e("TAG-MyListViewModel", "Corpo da resposta da API nulo ao carregar lista para listId: $effectiveTargetListId.")
                            }
                        }
                        else {
                            if (_uiState.value !is UiState.Success) {
                                _uiState.value = UiState.Error("Erro ao carregar lista da API: ${response.code()}")
                            }
                            Log.e("TAG-MyListViewModel", "Erro HTTP ao carregar lista para listId: $effectiveTargetListId: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        if (_uiState.value !is UiState.Success) {
                            _uiState.value = UiState.Error("Erro de conexão ao carregar lista da API: ${e.message}")
                        }
                        Log.e("TAG-MyListViewModel", "Erro de conexão ao carregar lista para listId: $effectiveTargetListId: ${e.message}", e)
                    }
                } else {
                    Log.d("TAG-MyListViewModel", "effectiveTargetListId é nulo, carregando filmes em cartaz como fallback.")
                    loadNowPlayingMovies()
                }
            } ?: run {
                Log.d("TAG-MyListViewModel", "Sessão ID nula. Usuário não autenticado. Carregando filmes em cartaz como fallback.")
                loadNowPlayingMovies()
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