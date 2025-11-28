package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.model.filme.AddRemoveListItemRequest
import com.danilloteles.appnetflixapi.model.filme.FilmeDetalhes
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.datasource.datastore.MyListPreferencesRepository
import com.danilloteles.appnetflixapi.utils.events.UiState
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.filme.TmdbList
import com.danilloteles.appnetflixapi.model.v4.response.TmdbListV4
import com.danilloteles.appnetflixapi.repository.v4.RepositoryV4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MeuFilmeDetalhesViewModel(
    private val movieId: Int,
    private val filmeAPI: FilmeAPI,
    private val repositoryV4: RepositoryV4,
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

    private val _userListsUiState = MutableStateFlow<UiState<List<TmdbListV4>>>(UiState.Idle)
    val userListsUiState: StateFlow<UiState<List<TmdbListV4>>> = _userListsUiState

    private var primaryListId: String? = null
    private var accountId: Int? = null

    private val _myListActionV4UiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val myListActionV4UiState: StateFlow<UiState<String>> = _myListActionV4UiState

    init {
        loadMovieDetails()
        viewModelScope.launch {
            userPreferencesRepository.accountId.first()?.toIntOrNull()?.let {
                accountId = it
            }
            primaryListId = userPreferencesRepository.primaryListId.first()

            loadUserLists()
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
                        UiState.Error("Erro ao carregar detalhes do filme.")
                    Log.e("TAG-MyMovieDetailsViewModel", "Erro ao carregar detalhes do filme: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro de conexão ao carregar detalhes.")
                Log.e("TAG-MyMovieDetailsViewModel", "Erro: ${e.message}", e)
            }
        }
    }

    private fun loadUserLists() {
        val TAG_DEBUG = "MyMovieDetailsVM-Debug"
        viewModelScope.launch {
            _userListsUiState.value = UiState.Loading
            Log.d(TAG_DEBUG, "Iniciando loadUserLists...")

            val accessToken = userPreferencesRepository.accessTokenV4.first()
            val accountId = userPreferencesRepository.accountId.first()
            if (accessToken == null || accountId == null) {
                _userListsUiState.value = UiState.Error("Token de acesso V4 ou Account ID não encontrados. Faça o login novamente.")
                Log.e(TAG_DEBUG, "Falha: accessToken ou accountId são nulos.")
                return@launch
            }

            Log.d(TAG_DEBUG, "V4 -> Usando accessToken e accountId para buscar listas.")

            val result = repositoryV4.getAccountLits(accessToken, accountId)
            if (result is Result.Sucesso) {
                val response = result.data
                if (response.isSuccessful){
                    response.body()?.let { accountListsV4Response ->
                        val data = accountListsV4Response.results
                        _userListsUiState.value = UiState.Success(data = data)
                        Log.d(TAG_DEBUG, "Sucesso V4! ${data.size} listas carregadas.")
                    } ?: run {
                        _userListsUiState.value = UiState.Error("Resposta da API V4 bem-sucedida, mas o corpo é nulo.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Corpo do erro indisponível"
                    _userListsUiState.value = UiState.Error("Erro ao carregar listas do usuário (V4).")
                    Log.e(TAG_DEBUG, "Falha V4: A API respondeu com o código ${response.code()}. Erro: $errorBody")
                }
            } else {
                when (result){
                    is Result.HttpError -> {
                        _myListActionV4UiState.value = UiState.Error("Erro HTTP: ${result.mensagem} - Code: ${result.code}")
                    }
                    is Result.NetworkError -> {
                        _myListActionV4UiState.value = UiState.Error("Erro de Network: ${result.mensagem}")
                    }
                    is Result.UnknownError -> {
                        _myListActionV4UiState.value = UiState.Error("Erro desconhecido: ${result.mensagem}")
                    }
                    else -> {
                        val error = result as Result.UnknownError
                        _userListsUiState.value = UiState.Error(error.mensagem)
                        Log.e(TAG_DEBUG, "Falha V4: A chamada para repositoryV4.getAccountLits falhou. Mensagem: ${error.mensagem}")
                    }
                }
            }
        }
    }

    // Função auxiliar para obter ou criar o accountId
    private suspend fun getOrCreateAccountId(sessionId: String): Int? {
        // Tentar obter do DataStore
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
                        Log.d("TAG-MyMovieDetailsViewModel", "Chamando API para obterDetalhesDaLista para listId: $id")
                        val response = filmeAPI.obterDetalhesDaLista(id, sessionId)
                        if (response.isSuccessful) {
                            val listDetails = response.body()
                            Log.d("TAG-MyMovieDetailsViewModel", "Detalhes da lista recebidos para listId: $id")
                            listDetails?.items?.forEachIndexed { index, mediaItem ->
                                Log.d("TAG-MyMovieDetailsViewModel", "Item $index: ID=${mediaItem.id}, Type=${mediaItem.media_type}, Title=${mediaItem.title}, Name=${mediaItem.name}, Poster=${mediaItem.poster_path}")
                            } ?: Log.d("TAG-MyMovieDetailsViewModel", "listDetails ou items é nulo para listId: $id")

                            val containsMovie = listDetails?.items?.any { it.id == movieId } ?: false
                            _isInMyList.value = containsMovie
                            Log.d("TAG-MyMovieDetailsViewModel", "API obterDetalhesDaLista para listId $id retornou ${listDetails?.items?.size ?: 0} itens. Contém movie $movieId: $containsMovie. _isInMyList atualizado para: ${_isInMyList.value}")
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

    fun addOrRemoveMovieV4(targetListIdForAction: String) {
        Log.d("TAG-MyMovieDetailsViewModel", "addOrRemoveMovie iniciado. MovieId: $movieId, targetListIdForAction: $targetListIdForAction")
        viewModelScope.launch {
            _myListActionV4UiState.value = UiState.Loading
            try {
                val accessToken = userPreferencesRepository.accessTokenV4.first()
                if (accessToken.isNullOrEmpty()) {
                    _myListActionV4UiState.value = UiState.Error("Usuário não autenticado")
                    return@launch
                }
                val listIdInt = targetListIdForAction.toInt()
                val result = if (_isInMyList.value) {
                    repositoryV4.removeMovie(accessToken, listIdInt.toString(), movieId)
                } else {
                    repositoryV4.addMovie(accessToken, listIdInt.toString(), movieId)
                }

                if (result is Result.Sucesso) {
                    val message = if (_isInMyList.value) "Filme removido com sucesso!" else "Filme adicionado com sucesso!"
                    _myListActionV4UiState.value = UiState.Success(message)
                    _isInMyList.value = !_isInMyList.value
                    checkIfMovieInMyList(targetListIdForAction)
                } else {
                    when (result){
                        is Result.HttpError -> {
                            _myListActionV4UiState.value = UiState.Error("Erro HTTP: ${result.mensagem} - Code: ${result.code}")
                        }
                        is Result.NetworkError -> {
                            _myListActionV4UiState.value = UiState.Error("Erro de Network: ${result.mensagem}")
                        }
                        is Result.UnknownError -> {
                            _myListActionV4UiState.value = UiState.Error("Erro desconhecido: ${result.mensagem}")
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                _myListActionV4UiState.value = UiState.Error("Falha na operação: ${e.message}")
            }
        }
    }

    fun resetMyListActionV4UiState() {
        _myListActionV4UiState.value = UiState.Idle
    }

    class Factory(
        private val movieId: Int,
        private val repositoryV4: RepositoryV4,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val myListPreferencesRepository: MyListPreferencesRepository,
        private val listId: String?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MeuFilmeDetalhesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MeuFilmeDetalhesViewModel(
                    movieId,
                    RetrofitHelper.filmeAPI,
                    repositoryV4,
                    userPreferencesRepository,
                    myListPreferencesRepository,
                    listId
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}