package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.filme.AddRemoveListItemRequest
import com.danilloteles.appnetflixapi.model.filme.TmdbList
import com.danilloteles.appnetflixapi.model.serie.SerieDetalhes
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.utils.events.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MySerieDetailsViewModel(
    private val serieId: Int,
    private val filmeAPI: FilmeAPI,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val listId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SerieDetalhes>>(UiState.Loading)
    val uiState: StateFlow<UiState<SerieDetalhes>> = _uiState

    private val _isInMyList = MutableStateFlow(false)
    val isInMyList: StateFlow<Boolean> = _isInMyList

    private val _myListActionUiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val myListActionUiState: StateFlow<UiState<Unit>> = _myListActionUiState

    private val _userListsUiState = MutableStateFlow<UiState<List<TmdbList>>>(UiState.Idle)
    val userListsUiState: StateFlow<UiState<List<TmdbList>>> = _userListsUiState

    private var primaryListId: String? = null
    private var accountId: Int? = null

    init {
        loadSerieDetails()
        viewModelScope.launch {
            userPreferencesRepository.accountId.first()?.toIntOrNull()?.let {
                accountId = it
            }
            primaryListId = userPreferencesRepository.primaryListId.first()

            loadUserLists()
            checkIfSerieInMyList()
        }
    }

    private fun loadSerieDetails() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = filmeAPI.recuperarDetalhesSerie(serieId)
                if (response.isSuccessful) {
                    response.body()?.let { details ->
                        _uiState.value = UiState.Success(details)
                    } ?: run {
                        _uiState.value = UiState.Error("Detalhes da série não encontrados.")
                    }
                } else {
                    _uiState.value = UiState.Error("Erro ao carregar detalhes da série.")
                    Log.e("TAG-MySerieDetailsViewModel", "Erro ao carregar detalhes da série: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro de conexão ao carregar detalhes")
                Log.e("TAG-MySerieDetailsViewModel", "Erro: ${e.message}", e)
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
                        val response = filmeAPI.obterListasDeContas(currentAccountId, sessionId)
                        if (response.isSuccessful) {
                            response.body()?.let { accountListsResponse ->
                                _userListsUiState.value = UiState.Success(accountListsResponse.results)
                            } ?: run {
                                _userListsUiState.value = UiState.Error("Não foi possível carregar as listas do usuário.")
                            }
                        } else {
                            _userListsUiState.value = UiState.Error("Erro ao carregar listas do usuário.")
                            Log.e("TAG-MySerieDetailsViewModel", "Erro ao carregar listas do usuário: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        _userListsUiState.value = UiState.Error("Erro de conexão ao carregar listas.")
                        Log.e("TAG-MySerieDetailsViewModel", "Erro: ${e.message}", e)
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

    private fun checkIfSerieInMyList(listIdToCheck: String? = null) {
        Log.d("TAG-MySerieDetailsViewModel", "checkIfSerieInMyList iniciado para serieId: $serieId")
        viewModelScope.launch {
            Log.d("TAG-MySerieDetailsViewModel", "listIdToCheck (param): $listIdToCheck, ViewModel listId (constructor): $listId, ViewModel primaryListId: $primaryListId")
            userPreferencesRepository.sessionId.first()?.let { sessionId ->
                Log.d("TAG-MySerieDetailsViewModel", "SessionId obtido: $sessionId")
                val repoPrimaryListId = userPreferencesRepository.primaryListId.first()
                Log.d("TAG-MySerieDetailsViewModel", "Repo primaryListId: $repoPrimaryListId")

                val finalTargetListId = listIdToCheck ?: listId ?: primaryListId ?: repoPrimaryListId
                Log.d("TAG-MySerieDetailsViewModel", "TargetListId final para verificação: $finalTargetListId")

                finalTargetListId?.let { id ->
                    try {
                        Log.d("TAG-MySerieDetailsViewModel", "Chamando API para obterDetalhesDaLista para listId: $id")
                        val response = filmeAPI.obterDetalhesDaLista(id, sessionId)
                        if (response.isSuccessful) {
                            val listDetails = response.body()
                            val containsSerie = listDetails?.items?.any { it.id == serieId && it.media_type == "tv" } ?: false
                            _isInMyList.value = containsSerie
                            Log.d("TAG-MySerieDetailsViewModel", "API obterDetalhesDaLista para listId $id retornou ${listDetails?.items?.size ?: 0} itens. Contém serie $serieId: $containsSerie. _isInMyList atualizado para: ${_isInMyList.value}")
                        } else {
                            Log.e("TAG-MySerieDetailsViewModel", "Erro ao verificar série na lista: ${response.code()}")
                            _isInMyList.value = false
                        }
                    } catch (e: Exception) {
                        Log.e("TAG-MySerieDetailsViewModel", "Erro de conexão ao verificar série na lista: ${e.message}", e)
                        _isInMyList.value = false
                    }
                } ?: run {
                    Log.d("TAG-MySerieDetailsViewModel", "Nenhum targetListId disponível para verificar série na lista.")
                    _isInMyList.value = false // Não há list_id primário definido
                }
            } ?: run {
                Log.d("TAG-MySerieDetailsViewModel", "Usuário não autenticado. Não foi possível verificar série na lista.")
                _isInMyList.value = false // Usuário não autenticado
            }
        }
    }

    fun addOrRemoveSerie(targetListIdForAction: String? = null) {
        Log.d("TAG-MySerieDetailsViewModel", "addOrRemoveSerie iniciado. SerieId: $serieId, targetListIdForAction: $targetListIdForAction")
        viewModelScope.launch {
            _myListActionUiState.value = UiState.Loading
            userPreferencesRepository.sessionId.first()?.let { sessionId ->
                Log.d("TAG-MySerieDetailsViewModel", "SessionId obtido: $sessionId")

                val finalListToModifyId = targetListIdForAction ?: listId ?: primaryListId ?: userPreferencesRepository.primaryListId.first()

                if (finalListToModifyId == null) {
                    _myListActionUiState.value = UiState.Error("Erro: ID da lista não disponível. Tente novamente.")
                    Log.e("TAG-MySerieDetailsViewModel", "List ID não disponível para adicionar/remover série.")
                    return@launch
                }

                val request = AddRemoveListItemRequest(media_id = serieId)
                Log.d("TAG-MySerieDetailsViewModel", "Enviando requisição para adicionar/remover serie. ID da série: $serieId, List ID: $finalListToModifyId")
                Log.d("TAG-MySerieDetailsViewModel", "Requisição API: ${request} para listId: $finalListToModifyId")
                try {
                    val response = if (_isInMyList.value) {
                        Log.d("TAG-MySerieDetailsViewModel", "Tentando remover série (ID: $serieId) da lista (ID: $finalListToModifyId).")
                        filmeAPI.removerItemDaLista(finalListToModifyId, sessionId, request)
                    } else {
                        Log.d("TAG-MySerieDetailsViewModel", "Tentando adicionar série (ID: $serieId) à lista (ID: $finalListToModifyId).")
                        filmeAPI.adicionarItemALista(finalListToModifyId, sessionId, request)
                    }

                    Log.d("TAG-MySerieDetailsViewModel", "Resposta da API - isSuccessful: ${response.isSuccessful}, Code: ${response.code()}, Body: ${response.body()}")

                    if (response.isSuccessful && (response.body()?.status_code == 1 || response.body()?.status_code == 12 || response.body()?.status_code == 13)) {
                        _isInMyList.value = !_isInMyList.value
                        _myListActionUiState.value = UiState.Success(Unit)
                        checkIfSerieInMyList(finalListToModifyId)
                        Log.d("TAG-MySerieDetailsViewModel", "Cache da lista atualizado. Série ID: $serieId, _isInMyList: ${_isInMyList.value}")
                    } else {
                        val errorMessage = response.body()?.status_message ?: "Falha desconhecida."
                        _myListActionUiState.value = UiState.Error(errorMessage)
                        Log.e("TAG-MySerieDetailsViewModel", "Falha ao adicionar/remover série: ${response.code()} - $errorMessage")
                    }
                } catch (e: Exception) {
                    _myListActionUiState.value = UiState.Error("Erro de conexão ao adicionar/remover série.")
                    Log.e("TAG-MySerieDetailsViewModel", "Erro de conexão ao adicionar/remover série: ${e.message}", e)
                }
            } ?: run {
                _myListActionUiState.value = UiState.Error("Erro: Usuário não autenticado. Faça login para gerenciar sua lista.")
                Log.e("TAG-MySerieDetailsViewModel", "Usuário não autenticado para adicionar/remover série.")
            }
        }
    }

    fun resetMyListActionUiState() {
        _myListActionUiState.value = UiState.Idle
    }

    class Factory(
        private val serieId: Int,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val listId: String?
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MySerieDetailsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MySerieDetailsViewModel(
                    serieId,
                    RetrofitHelper.filmeAPI,
                    userPreferencesRepository,
                    listId
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}