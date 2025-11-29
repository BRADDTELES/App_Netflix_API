package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.v3.serie.SerieDetalhes
import com.danilloteles.appnetflixapi.model.v4.response.TmdbListV4
import com.danilloteles.appnetflixapi.repository.v4.RepositoryV4
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.utils.events.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MinhaSerieDetalhesViewModel(
    private val serieId: Int,
    private val filmeAPI: FilmeAPI,
    private val repositoryV4: RepositoryV4,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val listId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SerieDetalhes>>(UiState.Loading)
    val uiState: StateFlow<UiState<SerieDetalhes>> = _uiState

    private val _isInMyList = MutableStateFlow(false)
    val isInMyList: StateFlow<Boolean> = _isInMyList

    private val _myListActionUiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val myListActionUiState: StateFlow<UiState<Unit>> = _myListActionUiState

    private val _userListsUiState = MutableStateFlow<UiState<List<TmdbListV4>>>(UiState.Idle)
    val userListsUiState: StateFlow<UiState<List<TmdbListV4>>> = _userListsUiState

    private var accountId: Int? = null

    private val _myListActionV4UiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val myListActionV4UiState: StateFlow<UiState<String>> = _myListActionV4UiState

    init {
        loadSerieDetails()
        viewModelScope.launch {
            userPreferencesRepository.accountId.first()?.toIntOrNull()?.let {
                accountId = it
            }
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
                        Log.d("TAG-MySerieDetailsViewModel", "Série carregada: ID=${details.id}, Nome=${details.name}, Original=${details.original_name}, Overview=${details.overview?.take(50)}...")
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

            val accessToken = userPreferencesRepository.accessTokenV4.first()
            val accountId = userPreferencesRepository.accountId.first()
            if (accessToken == null || accountId == null) {
                _userListsUiState.value = UiState.Error("Token de acesso V4 ou Account ID não encontrados. Faça o login novamente.")
                Log.e("TAG-MySerieDetailsViewModel", "Falha: accessToken ou accountId são nulos.")
                return@launch
            }

            Log.d("TAG-MySerieDetailsViewModel", "V4 -> Usando accessToken e accountId para buscar listas.")

            val result = repositoryV4.getAccountLits(accessToken, accountId)
            if (result is Result.Sucesso) {
                val response = result.data
                if (response.isSuccessful){
                    response.body()?.let { accountListsV4Response ->
                        val data = accountListsV4Response.results
                        _userListsUiState.value = UiState.Success(data = data)
                        Log.d("TAG-MySerieDetailsViewModel", "Sucesso V4! ${data.size} listas carregadas.")
                    } ?: run {
                        _userListsUiState.value = UiState.Error("Resposta da API V4 bem-sucedida, mas o corpo é nulo.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Corpo do erro indisponível"
                    _userListsUiState.value = UiState.Error("Erro ao carregar listas do usuário (V4).")
                    Log.e("TAG-MySerieDetailsViewModel", "Falha V4: A API respondeu com o código ${response.code()}. Erro: $errorBody")
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
                        _myListActionV4UiState.value = UiState.Error("Erro desconhecido")
                        Log.e("TAG-MySerieDetailsViewModel","Erro desconhecido: ${result.mensagem}")
                    }
                    else -> {
                        val error = result as Result.UnknownError
                        _userListsUiState.value = UiState.Error(error.mensagem)
                        Log.e("TAG-MySerieDetailsViewModel", "Falha V4: A chamada para repositoryV4.getAccountLits falhou. Mensagem: ${error.mensagem}")
                    }
                }
            }
        }
    }

    private fun checkIfSerieInMyList() {
        // Se não houver um listId específico, não podemos verificar. O filme não está "na lista".
        val targetListId = listId ?: return Unit.also {
            _isInMyList.value = false
            Log.d("TAG-MySerieDetailsViewModel", "Nenhum listId fornecido para verificação. Botão de remover não será mostrado.")
        }

        Log.d("TAG-MySerieDetailsViewModel", "checkIfMovieInMyList (V4) iniciado para serieId: $serieId em listId: $targetListId")

        viewModelScope.launch {
            val accessToken = userPreferencesRepository.accessTokenV4.first()
            if (accessToken == null) {
                Log.e("TAG-MySerieDetailsViewModel", "Access Token V4 nulo. Não é possível verificar a lista.")
                _isInMyList.value = false
                return@launch
            }

            when (val result = repositoryV4.getListDetails(accessToken, targetListId)) {
                is Result.Sucesso -> {
                    val listDetails = result.data
                    val containsSerie = listDetails.results.any { it.id == serieId }
                    _isInMyList.value = containsSerie
                    Log.d("TAG-MySerieDetailsViewModel", "Verificação V4: Lista $targetListId contém o série $serieId: $containsSerie")
                }
                is Result.HttpError, is Result.NetworkError, is Result.UnknownError -> {
                    val errorMessage = when (result) {
                        is Result.HttpError -> "Erro HTTP ${result.code}: ${result.mensagem}"
                        is Result.NetworkError -> "Erro de Rede: ${result.mensagem}"
                        is Result.UnknownError -> "Erro Desconhecido: ${result.mensagem}"
                        else -> "Erro inesperado" // Não deve acontecer
                    }
                    Log.e("TAG-MySerieDetailsViewModel", "Falha ao obter detalhes da lista $targetListId para verificação: $errorMessage")
                    _isInMyList.value = false
                }
            }
        }
    }

    fun addOrRemoveSerieV4(targetListIdForAction: String) {
        Log.d("TAG-MySerieDetailsViewModel", "addOrRemoveSerie iniciado. SerieId: $serieId, targetListIdForAction: $targetListIdForAction")
        viewModelScope.launch {
            _myListActionV4UiState.value = UiState.Loading
            try {
                val accessToken = userPreferencesRepository.accessTokenV4.first()
                if (accessToken.isNullOrEmpty()) {
                    _myListActionV4UiState.value = UiState.Error("Usuário não autenticado")
                    return@launch
                }

                val result = if (_isInMyList.value) {
                    repositoryV4.removeSerie(accessToken, targetListIdForAction, serieId)
                } else {
                    repositoryV4.addSerie(accessToken, targetListIdForAction, serieId)
                }

                if (result is Result.Sucesso) {
                    val message = if (_isInMyList.value) "Série removida com sucesso!" else "Série adicionada com sucesso!"
                    _myListActionV4UiState.value = UiState.Success(message)
                    // Inverte o estado localmente para refletir a mudança imediatamente na UI
                    _isInMyList.value = !_isInMyList.value
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
        private val serieId: Int,
        private val repositoryV4: RepositoryV4,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val listId: String?
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MinhaSerieDetalhesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MinhaSerieDetalhesViewModel(
                    serieId,
                    RetrofitHelper.filmeAPI,
                    repositoryV4,
                    userPreferencesRepository,
                    listId
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}