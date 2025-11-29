package com.danilloteles.appnetflixapi.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.model.v3.filme.FilmeDetalhes
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.utils.events.UiState
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.v4.response.TmdbListV4
import com.danilloteles.appnetflixapi.model.video.Video
import com.danilloteles.appnetflixapi.repository.v3.FilmeRepository
import com.danilloteles.appnetflixapi.repository.v4.RepositoryV4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import androidx.core.net.toUri

class MeuFilmeDetalhesViewModel(
    private val movieId: Int,
    private val filmeRepository: FilmeRepository,
    private val repositoryV4: RepositoryV4,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val listId: String?
) : ViewModel() {

    private val _videosUiState = MutableStateFlow<UiState<List<Video>>>(UiState.Idle)
    val videosUiState: StateFlow<UiState<List<Video>>> = _videosUiState

    private val _showVideoPlayer = MutableStateFlow(false)
    val showVideoPlayer: StateFlow<Boolean> = _showVideoPlayer

    private val _selectedVideoKey = MutableStateFlow<String?>(null)
    val selectedVideoKey: StateFlow<String?> = _selectedVideoKey

    private val _showTrailerBottomSheet = MutableStateFlow(false)
    val showTrailerBottomSheet: StateFlow<Boolean> = _showTrailerBottomSheet

    private val _uiState = MutableStateFlow<UiState<FilmeDetalhes>>(UiState.Loading)
    val uiState: StateFlow<UiState<FilmeDetalhes>> = _uiState

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
        loadMovieDetails()
        viewModelScope.launch {
            userPreferencesRepository.accountId.first()?.toIntOrNull()?.let {
                accountId = it
            }
            loadUserLists()
            checkIfMovieInMyList()
        }
    }

    fun buscarVideosFilme() {
        Log.d("TAG-MeuFilmeDetalhesViewModel", "Iniciando busca de vídeos para movieId: $movieId")
        viewModelScope.launch {
            _videosUiState.value = UiState.Loading
            try {
                val response = filmeRepository.recuperarVideosFilme(movieId, "pt-BR")
                if (response.isSuccessful) {
                    response.body()?.let { videoResponse ->
                        Log.d("TAG-MeuFilmeDetalhesViewModel", "Total de vídeos retornados: ${videoResponse.results.size}")

                        videoResponse.results.forEach { video ->
                            Log.d("TAG-MeuFilmeDetalhesViewModel", "Vídeo: ${video.name} | Tipo: ${video.type} | Site: ${video.site} | Oficial: ${video.official}")
                        }
                        val trailersAndTeasers = videoResponse.results.filter { video ->
                            video.site.equals("YouTube", ignoreCase = true) &&
                                    (video.type.equals("Trailer", ignoreCase = true) ||
                                            video.type.equals("Teaser", ignoreCase = true))
                        }.sortedWith(
                            compareByDescending<Video> { it.official }
                                .thenByDescending { it.type.equals("Trailer", ignoreCase = true) }
                        )
                        Log.d("TAG-MeuFilmeDetalhesViewModel", "Vídeos após filtro: ${trailersAndTeasers.size}")
                        if (trailersAndTeasers.isNotEmpty()) {
                            _videosUiState.value = UiState.Success(trailersAndTeasers)

                            // 🎬 MUDANÇA AQUI: Reproduzir o primeiro trailer automaticamente
                            val primeiroTrailer = trailersAndTeasers.first()
                            reproduzirVideo(primeiroTrailer.key)
                            Log.d("TAG-MeuFilmeDetalhesViewModel", "Primeiro trailer selecionado: ${trailersAndTeasers.first().name}")
                            Log.d("TAG-MeuFilmeDetalhesViewModel", "Reproduzindo primeiro trailer: ${primeiroTrailer.name}")
                        } else {
                            _videosUiState.value = UiState.Error("Nenhum trailer disponível para este filme.")
                        }
                    } ?: run {
                        _videosUiState.value = UiState.Error("Resposta vazia da API de vídeos.")
                    }
                } else {
                    _videosUiState.value = UiState.Error("Erro ao buscar vídeos: ${response.code()}")
                    Log.e("TAG-MeuFilmeDetalhesViewModel", "Erro HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                _videosUiState.value = UiState.Error("Erro de conexão ao buscar vídeos.")
                Log.e("TAG-MeuFilmeDetalhesViewModel", "Exceção ao buscar vídeos: ${e.message}", e)
            }
        }
    }

    fun mostrarListaDeTrailers() {
        viewModelScope.launch {
            if (_videosUiState.value is UiState.Success) {
                _showTrailerBottomSheet.value = true
            } else {
                // Se ainda não carregou, buscar primeiro
                buscarListaDeTrailers()
            }
        }
    }

    private fun buscarListaDeTrailers() {
        viewModelScope.launch {
            _videosUiState.value = UiState.Loading
            try {
                val response = filmeRepository.recuperarVideosFilme(movieId, "pt-BR")
                if (response.isSuccessful) {
                    response.body()?.let { videoResponse ->
                        val trailersAndTeasers = videoResponse.results.filter { video ->
                            video.site.equals("YouTube", ignoreCase = true) &&
                                    (video.type.equals("Trailer", ignoreCase = true) ||
                                            video.type.equals("Teaser", ignoreCase = true))
                        }.sortedWith(
                            compareByDescending<Video> { it.official }
                                .thenByDescending { it.type.equals("Trailer", ignoreCase = true) }
                        )

                        if (trailersAndTeasers.isNotEmpty()) {
                            _videosUiState.value = UiState.Success(trailersAndTeasers)
                            _showTrailerBottomSheet.value = true // Mostrar bottom sheet
                            Log.d("TAG-MeuFilmeDetalhesViewModel", "${trailersAndTeasers.size} vídeos encontrados")
                        } else {
                            _videosUiState.value = UiState.Error("Nenhum trailer disponível para este filme.")
                        }
                    } ?: run {
                        _videosUiState.value = UiState.Error("Resposta vazia da API de vídeos.")
                    }
                } else {
                    _videosUiState.value = UiState.Error("Erro ao buscar vídeos: ${response.code()}")
                    Log.e("TAG-MeuFilmeDetalhesViewModel", "Erro HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                _videosUiState.value = UiState.Error("Erro de conexão ao buscar vídeos.")
                Log.e("TAG-MeuFilmeDetalhesViewModel", "Exceção ao buscar vídeos: ${e.message}", e)
            }
        }
    }

    fun reproduzirVideo(videoKey: String) {
        _selectedVideoKey.value = videoKey
        _showVideoPlayer.value = true
        _showTrailerBottomSheet.value = false
        Log.d("TAG-MeuFilmeDetalhesViewModel", "Reproduzindo vídeo com key: $videoKey")
    }

    // ADICIONAR novo método para abrir no YouTube App:
    fun abrirVideoNoYouTube(context: Context, videoKey: String) {
        Log.d("TAG-MeuFilmeDetalhesViewModel", "Tentando abrir vídeo no YouTube: $videoKey")

        try {
            // PRIMEIRO: Tentar abrir no app do YouTube usando scheme específico
            val youtubeAppIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("vnd.youtube:$videoKey")
                setPackage("com.google.android.youtube") // Força usar o app do YouTube
            }

            // Verificar se o app do YouTube está instalado
            if (youtubeAppIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(youtubeAppIntent)
                Log.d("TAG-MeuFilmeDetalhesViewModel", "Vídeo aberto no app YouTube")
            } else {
                throw Exception("App YouTube não instalado")
            }
        } catch (e: Exception) {
            Log.w("TAG-MeuFilmeDetalhesViewModel", "Falha ao abrir no app YouTube: ${e.message}")

            try {
                // FALLBACK: Abrir no navegador
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://www.youtube.com/watch?v=$videoKey")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                Log.d("TAG-MeuFilmeDetalhesViewModel", "Vídeo aberto no navegador")
            } catch (e2: Exception) {
                Log.e("TAG-MeuFilmeDetalhesViewModel", "Falha ao abrir no navegador: ${e2.message}")
            }
        }

        // Fechar o player após abrir
        fecharVideoPlayer()
    }

    fun fecharVideoPlayer() {
        _showVideoPlayer.value = false
        _selectedVideoKey.value = null
        Log.d("TAG-MeuFilmeDetalhesViewModel", "Player de vídeo fechado")
    }

    fun fecharTrailerBottomSheet() {
        _showTrailerBottomSheet.value = false
        Log.d("TAG-MeuFilmeDetalhesViewModel", "Bottom sheet de trailers fechado")
    }

    private fun loadMovieDetails() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = filmeRepository.recuperarDetalhesFilme(movieId, "pt-BR")
                if (response.isSuccessful) {
                    response.body()?.let { details ->
                        _uiState.value = UiState.Success(details)
                    } ?: run {
                        _uiState.value = UiState.Error("Detalhes do filme não encontrados.")
                    }
                } else {
                    _uiState.value =
                        UiState.Error("Erro ao carregar detalhes do filme.")
                    Log.e(
                        "TAG-MyMovieDetailsViewModel",
                        "Erro ao carregar detalhes do filme: ${response.code()}"
                    )
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
                _userListsUiState.value =
                    UiState.Error("Token de acesso V4 ou Account ID não encontrados. Faça o login novamente.")
                Log.e(TAG_DEBUG, "Falha: accessToken ou accountId são nulos.")
                return@launch
            }

            Log.d(TAG_DEBUG, "V4 -> Usando accessToken e accountId para buscar listas.")

            val result = repositoryV4.getAccountLits(accessToken, accountId)
            if (result is Result.Sucesso) {
                val response = result.data
                if (response.isSuccessful) {
                    response.body()?.let { accountListsV4Response ->
                        val data = accountListsV4Response.results
                        _userListsUiState.value = UiState.Success(data = data)
                        Log.d(TAG_DEBUG, "Sucesso V4! ${data.size} listas carregadas.")
                    } ?: run {
                        _userListsUiState.value =
                            UiState.Error("Resposta da API V4 bem-sucedida, mas o corpo é nulo.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Corpo do erro indisponível"
                    _userListsUiState.value =
                        UiState.Error("Erro ao carregar listas do usuário (V4).")
                    Log.e(
                        TAG_DEBUG,
                        "Falha V4: A API respondeu com o código ${response.code()}. Erro: $errorBody"
                    )
                }
            } else {
                when (result) {
                    is Result.HttpError -> {
                        _myListActionV4UiState.value =
                            UiState.Error("Erro HTTP: ${result.mensagem} - Code: ${result.code}")
                    }

                    is Result.NetworkError -> {
                        _myListActionV4UiState.value =
                            UiState.Error("Erro de Network: ${result.mensagem}")
                    }

                    is Result.UnknownError -> {
                        _myListActionV4UiState.value =
                            UiState.Error("Erro desconhecido: ${result.mensagem}")
                    }

                    else -> {
                        val error = result as Result.UnknownError
                        _userListsUiState.value = UiState.Error(error.mensagem)
                        Log.e(
                            TAG_DEBUG,
                            "Falha V4: A chamada para repositoryV4.getAccountLits falhou. Mensagem: ${error.mensagem}"
                        )
                    }
                }
            }
        }
    }

    private fun checkIfMovieInMyList() {
        // Se não houver um listId específico, não podemos verificar. O filme não está "na lista".
        val targetListId = listId ?: return Unit.also {
            _isInMyList.value = false
            Log.d(
                "TAG-MyMovieDetailsViewModel",
                "Nenhum listId fornecido para verificação. Botão de remover não será mostrado."
            )
        }

        Log.d(
            "TAG-MyMovieDetailsViewModel",
            "checkIfMovieInMyList (V4) iniciado para movieId: $movieId em listId: $targetListId"
        )

        viewModelScope.launch {
            val accessToken = userPreferencesRepository.accessTokenV4.first()
            if (accessToken == null) {
                Log.e(
                    "TAG-MyMovieDetailsViewModel",
                    "Access Token V4 nulo. Não é possível verificar a lista."
                )
                _isInMyList.value = false
                return@launch
            }

            when (val result = repositoryV4.getListDetails(accessToken, targetListId)) {
                is Result.Sucesso -> {
                    val listDetails = result.data
                    val containsMovie = listDetails.results.any { it.id == movieId }
                    _isInMyList.value = containsMovie
                    Log.d(
                        "TAG-MyMovieDetailsViewModel",
                        "Verificação V4: Lista $targetListId contém o filme $movieId: $containsMovie"
                    )
                }

                is Result.HttpError, is Result.NetworkError, is Result.UnknownError -> {
                    val errorMessage = when (result) {
                        is Result.HttpError -> "Erro HTTP ${result.code}: ${result.mensagem}"
                        is Result.NetworkError -> "Erro de Rede: ${result.mensagem}"
                        is Result.UnknownError -> "Erro Desconhecido: ${result.mensagem}"
                        else -> "Erro inesperado" // Não deve acontecer
                    }
                    Log.e(
                        "TAG-MyMovieDetailsViewModel",
                        "Falha ao obter detalhes da lista $targetListId para verificação: $errorMessage"
                    )
                    _isInMyList.value = false
                }
            }
        }
    }

    fun addOrRemoveMovieV4(targetListIdForAction: String) {
        Log.d(
            "TAG-MyMovieDetailsViewModel",
            "addOrRemoveMovie iniciado. MovieId: $movieId, targetListIdForAction: $targetListIdForAction"
        )
        viewModelScope.launch {
            _myListActionV4UiState.value = UiState.Loading
            try {
                val accessToken = userPreferencesRepository.accessTokenV4.first()
                if (accessToken.isNullOrEmpty()) {
                    _myListActionV4UiState.value = UiState.Error("Usuário não autenticado")
                    return@launch
                }
                val result = if (_isInMyList.value) {
                    repositoryV4.removeMovie(accessToken, targetListIdForAction, movieId)
                } else {
                    repositoryV4.addMovie(accessToken, targetListIdForAction, movieId)
                }

                if (result is Result.Sucesso) {
                    val message =
                        if (_isInMyList.value) "Filme removido com sucesso!" else "Filme adicionado com sucesso!"
                    _myListActionV4UiState.value = UiState.Success(message)
                    // Inverte o estado localmente para refletir a mudança imediatamente na UI
                    _isInMyList.value = !_isInMyList.value
                } else {
                    val errorMessage = when (result) {
                        is Result.HttpError -> "Erro HTTP: ${result.mensagem} - Code: ${result.code}"
                        is Result.NetworkError -> "Erro de Rede: ${result.mensagem}"
                        is Result.UnknownError -> "Erro desconhecido: ${result.mensagem}"
                        else -> "Erro inesperado ao adicionar/remover"
                    }
                    _myListActionV4UiState.value = UiState.Error(errorMessage)
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
        private val filmeRepository: FilmeRepository,
        private val repositoryV4: RepositoryV4,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val listId: String?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MeuFilmeDetalhesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MeuFilmeDetalhesViewModel(
                    movieId,
                    filmeRepository,
                    repositoryV4,
                    userPreferencesRepository,
                    listId
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}