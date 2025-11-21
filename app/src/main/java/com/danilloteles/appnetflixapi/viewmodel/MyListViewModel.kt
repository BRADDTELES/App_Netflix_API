package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.paging.PopularFilmesPagingSource
import com.danilloteles.appnetflixapi.model.Filme
import com.danilloteles.appnetflixapi.model.TmdbList
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.datasource.datastore.MyListPreferencesRepository
import com.danilloteles.appnetflixapi.utils.events.UiState
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.datasource.paging.MyListPagingSource
import com.danilloteles.appnetflixapi.datasource.paging.NowPlayingFilmesPagingSource
import com.danilloteles.appnetflixapi.datasource.paging.TopRatedFilmesPagingSource
import com.danilloteles.appnetflixapi.enums.MovieListFilter
import com.danilloteles.appnetflixapi.repository.FilmeRepository
import com.danilloteles.appnetflixapi.utils.events.MovieListFilterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class MyListViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val myListPreferencesRepository: MyListPreferencesRepository,
    private val filmeRepository: FilmeRepository
) : ViewModel() {

    private val filmeAPI: FilmeAPI = RetrofitHelper.filmeAPI

    private val _userListsUiState = MutableStateFlow<UiState<List<TmdbList>>>(UiState.Idle)
    val userListsUiState: StateFlow<UiState<List<TmdbList>>> = _userListsUiState

    private val _currentFilter = MutableStateFlow<MovieListFilterState>(MovieListFilterState.Popular)
    val currentFilter: StateFlow<MovieListFilterState> = _currentFilter

    private var primaryListId: String? = null
    private var accountId: Int? = null

    val moviesStream: Flow<PagingData<Filme>> = _currentFilter.flatMapLatest { filter ->
        createPagerForFilter(filter).flow
    }.cachedIn(viewModelScope)

    fun applyFilter(filter: MovieListFilterState) {
        _currentFilter.value = filter
    }

    private fun createPagerForFilter(filter: MovieListFilterState): Pager<Int, Filme> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                when (filter) {
                    MovieListFilterState.Popular -> PopularFilmesPagingSource(filmeRepository.filmeAPI)
                    MovieListFilterState.TopRated -> TopRatedFilmesPagingSource(filmeRepository.filmeAPI)
                    MovieListFilterState.NowPlaying -> NowPlayingFilmesPagingSource(filmeRepository.filmeAPI)
                    is MovieListFilterState.MyList -> MyListPagingSource(
                        filmeAPI = filmeRepository.filmeAPI,
                        userPreferencesRepository = userPreferencesRepository,
                        listId = filter.listId
                    )
                }
            }
        )
    }

    init {
        loadUserLists()
        viewModelScope.launch {
            primaryListId = userPreferencesRepository.primaryListId.first()
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
                        val response = filmeAPI.obterListasDeContas(currentAccountId, sessionId)
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
                                }

                                primaryListId?.let {
                                    applyFilter(MovieListFilterState.MyList(it))
                                } ?: run {
                                    applyFilter(MovieListFilterState.Popular)
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
        val accountDetailsResponse = filmeAPI.obterDetalhesDaConta(sessionId)
        if (accountDetailsResponse.isSuccessful) {
            accountDetailsResponse.body()?.let { details ->
                accountId = details.id
                userPreferencesRepository.saveAccountId(details.id.toString())
                return details.id
            }
        }
        return null
    }

    class MyListViewModelFactory(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val myListPreferencesRepository: MyListPreferencesRepository,
        private val filmeRepository: FilmeRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MyListViewModel(userPreferencesRepository, myListPreferencesRepository, filmeRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}